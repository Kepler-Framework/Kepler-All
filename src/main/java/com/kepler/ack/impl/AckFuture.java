package com.kepler.ack.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerLocalException;
import com.kepler.KeplerRemoteException;
import com.kepler.KeplerTimeoutException;
import com.kepler.ack.Ack;
import com.kepler.ack.AckTimeOut;
import com.kepler.ack.Acks;
import com.kepler.ack.Status;
import com.kepler.admin.transfer.Collector;
import com.kepler.admin.transfer.Transfer;
import com.kepler.channel.ChannelInvoker;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.generic.reflect.analyse.Fields;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.header.impl.TraceContext;
import com.kepler.host.Host;
import com.kepler.mock.MockerResponse;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;
import com.kepler.service.Quiet;

/**
 * Warning: 监视器使用this避免创建无用对象
 * 
 * @author kim 2015年7月23日
 */
/**
 * @author KimShen
 *
 */
public class AckFuture implements Future<Object>, Runnable, Ack {

	/**
	 * Response校对
	 */
	private static final boolean CORRECT_ACTIVED = PropertiesUtils.get(AckFuture.class.getName().toLowerCase() + ".correct_actived", false);

	/**
	 * 超时是否传递
	 */
	public static final String TIMEOUT_PROPAGATE_KEY = AckFuture.class.getName().toLowerCase() + ".timeout_propagate";

	private static final boolean TIMEOUT_PROPAGATE_DEF = PropertiesUtils.get(AckFuture.TIMEOUT_PROPAGATE_KEY, false);

	public static final String TIMEOUT_KEY = AckFuture.class.getName().toLowerCase() + ".timeout";

	/**
	 * 默认最大超时
	 */
	private static final int TIMEOUT_DEF = PropertiesUtils.get(AckFuture.TIMEOUT_KEY, 60000);

	private static final Log LOGGER = LogFactory.getLog(AckFuture.class);

	/**
	 * ACK创建时间
	 */
	private final long start = System.currentTimeMillis();

	/**
	 * ACK持有线程
	 */
	private final Thread thread = Thread.currentThread();

	/**
	 * Trace
	 */
	private final String trace = TraceContext.getTrace();

	/**
	 * 泛化分析
	 */
	private final FieldsAnalyser analyser;

	/**
	 * 执行通道
	 */
	private final ChannelInvoker invoker;

	/**
	 * 信息收集
	 */
	private final Collector collector;

	/**
	 * 超时处理
	 */
	private final AckTimeOut timeout;

	/**
	 * EventLoop
	 */
	private final Executor executor;

	/**
	 * 原始请求
	 */
	private final Request request;

	/**
	 * 原始方法(可能为Null)
	 */
	private final Method method;

	/**
	 * 超时上限
	 */
	private final long deadline;

	/**
	 * 静默判断
	 */
	private final Quiet quiet;

	/**
	 * ACK状态, 默认WAITING
	 */
	volatile private Status stauts = Status.WAITING;

	/**
	 * 是否中断
	 */
	volatile private boolean interrupt;

	/**
	 * 服务响应(Callback)
	 */
	volatile private Response response;

	/**
	 * 服务响应时间
	 */
	volatile private long receive;

	/**
	 * Ack集合
	 */
	volatile private Acks acks;

	public AckFuture(FieldsAnalyser analyser, ChannelInvoker invoker, AckTimeOut timeout, Collector collector, Executor executor, Method method, Request request, Profile profile, Quiet quiet) {
		super();
		this.quiet = quiet;
		this.method = method;
		this.invoker = invoker;
		this.timeout = timeout;
		this.request = request;
		this.executor = executor;
		this.analyser = analyser;
		this.collector = collector;
		// 计算Timeout最终时间
		this.deadline = this.deadline(profile, request);
	}

	/**
	 * 计算Timeout
	 * 
	 * @param deadline
	 * @return
	 */
	private long deadline(Profile profile, Request request) {
		long deadline_config = PropertiesUtils.profile(profile.profile(request.service()), AckFuture.TIMEOUT_KEY, AckFuture.TIMEOUT_DEF);
		// 如果超时小于等于0则使用表示不指定超时时间
		long deadline_actual = deadline_config > 0 ? deadline_config : Long.MAX_VALUE;
		// 如果开启了Timeout传递则放入Header供服务端检查
		if (PropertiesUtils.profile(profile.profile(request.service()), AckFuture.TIMEOUT_PROPAGATE_KEY, AckFuture.TIMEOUT_PROPAGATE_DEF)) {
			request.put(AckFuture.TIMEOUT_PROPAGATE_KEY, System.currentTimeMillis() + deadline_actual);
		}
		return deadline_actual;
	}

	/**
	 * 绑定
	 * 
	 * @param acks
	 * @return
	 */
	public AckFuture acks(Acks acks) {
		this.acks = acks;
		return this;
	}

	/**
	 * 过程日志
	 * 
	 * @return
	 */
	private String message4request(String reason) {
		return "Ack (" + Arrays.toString(this.request.ack()) + ") for " + this.request.service() + "[method=" + this.request.method() + "] to " + this.invoker.remote().address() + " " + reason + " after: " + (System.currentTimeMillis() - this.start);
	}

	/**
	 * 是否已中断
	 * 
	 * @return
	 * @throws KeplerLocalException
	 */
	private AckFuture checkInterrupt() throws InterruptedException {
		if (this.interrupt) {
			throw new InterruptedException(this.message4request("interrupt"));
		}
		return this;
	}

	/**
	 * 是否已取消
	 * 
	 * @return
	 * @throws KeplerLocalException
	 */
	private AckFuture checkCancel() throws KeplerLocalException {
		if (Status.CANCEL.equals(this.stauts)) {
			throw new KeplerLocalException(this.message4request("cancel"));
		}
		return this;
	}

	/**
	 * 是否已超时
	 * 
	 * @return
	 */
	private AckFuture checkTimeout() throws KeplerLocalException {
		if (Status.TIMEOUT.equals(this.stauts)) {
			throw new KeplerTimeoutException(this.message4request("timeout"));
		}
		return this;
	}

	/**
	 * 是否有异常
	 * 
	 * @return
	 */
	private AckFuture checkException() throws KeplerRemoteException {
		if (!this.response.valid()) {
			// 如果静默则保持Done状态
			this.stauts = this.quiet.quiet(this.request, this.method, this.response.throwable().getClass()) ? Status.DONE : Status.EXCEPTION;
			// 非KeplerRemoteException需包装
			throw KeplerRemoteException.class.isAssignableFrom(this.response.throwable().getClass()) ? KeplerRemoteException.class.cast(this.response.throwable()) : new KeplerRemoteException(this.response.throwable());
		}
		return this;
	}

	public Host local() {
		return this.invoker.local();
	}

	public Host remote() {
		return this.invoker.remote();
	}

	public Status status() {
		return this.stauts;
	}

	public Request request() {
		return this.request;
	}

	/**
	 * Response callback
	 * 
	 * @param
	 */
	public void response(Response response) {
		synchronized (this) {
			this.receive = System.currentTimeMillis();
			this.response = response;
			// 如果状态为等待则进行唤醒
			if (this.stauts == Status.WAITING) {
				this.stauts = Status.DONE;
				this.notifyAll();
			}
		}
	}

	public boolean cancel(boolean interrupt) {
		// Guard case1, 已完成则立即返回
		if (this.isDone()) {
			return false;
		}
		// Guard case2, 已取消则立即返回
		if (this.isCancelled()) {
			return true;
		}
		synchronized (this) {
			// Guard case1, 同步校验
			if (this.isDone()) {
				return false;
			}
			// Guard case2, 同步校验
			if (this.isCancelled()) {
				return true;
			}
			// 切换状态
			this.stauts = Status.CANCEL;
			// 标记是否中断
			this.interrupt = interrupt;
			if (this.interrupt) {
				this.thread.interrupt();
			} else {
				this.notifyAll();
			}
			return true;
		}
	}

	@Override
	public boolean isCancelled() {
		return Status.CANCEL.equals(this.stauts);
	}

	@Override
	public boolean isDone() {
		// 完成, 超时或异常均表示为Done状态
		return Status.DONE.equals(this.stauts) || Status.TIMEOUT.equals(this.stauts) || Status.EXCEPTION.equals(this.stauts);
	}

	@Override
	public Object get() throws InterruptedException {
		// 使用默认超时
		return this.get(this.deadline, TimeUnit.MILLISECONDS);
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException {
		try {
			// 取Timeout最小值
			this.waiting(Math.min(timeout, this.deadline));
			Object response_source = this.response();
			// Guard case1, 无需校对
			if (!AckFuture.CORRECT_ACTIVED) {
				return response_source;
			}
			// 获取实际响应
			Object response_actual = MockerResponse.class.isAssignableFrom(response_source.getClass()) ? MockerResponse.class.cast(response_source).response() : response_source;
			// Guard case2, 返回为空
			if (response_actual == null) {
				return response_actual;
			}
			try {
				Fields[] fields = this.analyser.get(this.method);
				// Guard case4, 无法转换
				if (fields == null || fields.length == 0) {
					AckFuture.LOGGER.warn("[generic-failed][service=" + this.request.service() + "][method=" + this.method + "]");
					return response_actual;
				}
				return fields[0].actual(response_actual);
			} catch (Throwable e) {
				// 转换, 如果失败则返回原始类型
				AckFuture.LOGGER.error(e.getMessage(), e);
				return response_source;
			}
		} finally {
			this.completed();
		}
	}

	/**
	 * 后续工作
	 */
	private void completed() {
		// ACK移除
		this.executor.execute(this);
		// 收集信息
		this.collector.collect(this);
		// 超时处理
		if (this.stauts.equals(Status.TIMEOUT)) {
			// 首次访问即超时Transfer = null
			Transfer transfer = this.collector.peek(this);
			this.timeout.timeout(this.invoker, this, transfer != null ? transfer.timeout() : 1);
		}
	}

	/**
	 * 已耗时大于指定超时则标记状态为超时
	 * 
	 * @param timeout
	 * @return
	 */
	private void timeout(long timeout) {
		if ((System.currentTimeMillis() - this.start) > timeout) {
			this.stauts = Status.TIMEOUT;
		}
	}

	private void waiting(long timeout) throws InterruptedException {
		synchronized (this) {
			// 堵塞条件: 未完成且未取消
			while (!this.isDone() && !this.isCancelled()) {
				try {
					this.wait(timeout);
				} finally {
					// 计算是否超时
					this.timeout(timeout);
				}
			}
		}
	}

	private Object response() throws InterruptedException {
		// 是否中断, 是否超时, 是否取消, 是否抛出异常
		return this.checkInterrupt().checkTimeout().checkCancel().checkException().response.response();
	}

	public String trace() {
		return this.trace;
	}

	/**
	 * 当前耗时
	 * 
	 * @return
	 */
	public long elapse() {
		return this.receive - this.start;
	}

	@Override
	public void run() {
		// 移除ACK
		this.acks.remove(this.request.ack());
	}
}