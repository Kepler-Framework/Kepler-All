package com.kepler.ack.impl;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import com.kepler.header.impl.TraceContext;
import com.kepler.host.Host;
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

	public static final String TIMEOUT_KEY = AckFuture.class.getName().toLowerCase() + ".timeout";

	/**
	 * 默认最大超时
	 */
	private static final int TIMEOUT_DEF = PropertiesUtils.get(AckFuture.TIMEOUT_KEY, 60000);

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
	 * Ack集合
	 */
	volatile private Acks acks;

	public AckFuture(ChannelInvoker invoker, AckTimeOut timeout, Collector collector, Executor executor, Request request, Profile profile, Quiet quiet) {
		super();
		this.quiet = quiet;
		this.invoker = invoker;
		this.timeout = timeout;
		this.request = request;
		this.executor = executor;
		this.collector = collector;
		// 计算Timeout最终时间
		this.deadline = this.deadline(PropertiesUtils.profile(profile.profile(request.service()), AckFuture.TIMEOUT_KEY, AckFuture.TIMEOUT_DEF));
	}

	/**
	 * 计算Timeout
	 * 
	 * @param deadline
	 * @return
	 */
	private long deadline(long deadline) {
		// 如果超时小于等于0则使用表示不指定超时时间
		return deadline > 0 ? deadline : Long.MAX_VALUE;
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
		return "Ack (" + Arrays.toString(this.request.ack()) + ") for " + this.request.service() + "[method=" + this.request.method() + "] to " + this.invoker.remote().address() + " " + reason + " after: " + this.elapse();
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
			this.stauts = this.quiet.quiet(this.request, this.response.throwable().getClass()) ? Status.DONE : Status.EXCEPTION;
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
			return this.response();
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
		if (this.elapse() > timeout) {
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
		return System.currentTimeMillis() - this.start;
	}

	@Override
	public void run() {
		// 移除ACK
		this.acks.remove(this.request.ack());
	}
}