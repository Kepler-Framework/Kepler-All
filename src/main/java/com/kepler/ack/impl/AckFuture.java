package com.kepler.ack.impl;

import com.kepler.KeplerLocalException;
import com.kepler.KeplerRemoteException;
import com.kepler.KeplerTimeoutException;
import com.kepler.ack.Ack;
import com.kepler.ack.Status;
import com.kepler.admin.transfer.Collector;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;
import com.kepler.service.Quiet;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Warning: 监视器使用this避免创建无用对象(协商)
 * 
 * @author kim 2015年7月23日
 */
public class AckFuture implements Future<Object>, Ack {

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
	 * ACK所有者线程
	 */
	private final Thread thread = Thread.currentThread();

	private final Collector collector;

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
	 * 目标主机
	 */
	private final Host target;

	/**
	 * 当前主机
	 */
	private final Host local;

	/**
	 * ACK状态, 默认WAITING
	 */
	volatile private Status stauts = Status.WAITING;

	/**
	 * 服务响应(Callback)
	 */
	volatile private Response response;

	/**
	 * 是否中断
	 */
	volatile private boolean interrupt;

	public AckFuture(Collector collector, Host local, Host target, Request request, Profile profile, Quiet quiet) {
		super();
		this.local = local;
		this.quiet = quiet;
		this.target = target;
		this.request = request;
		this.collector = collector;
		// 计算Timeout
		this.deadline = PropertiesUtils.profile(profile.profile(request.service()), AckFuture.TIMEOUT_KEY, AckFuture.TIMEOUT_DEF);
	}

	/**
	 * 构建当前Request状态消息(For exception or log)
	 * 
	 * @return
	 */
	private String message4request(String reason) {
		return "ACK(" + Arrays.toString(this.request.ack()) + " for " + this.request.service() + " to " + this.target.address() + " " + reason + " after: " + this.elapse();
	}

	/**
	 * 是否为静默异常(不会被Collect判断为异常)
	 * 
	 * @param throwable
	 * @return
	 */
	private boolean quiet(Class<? extends Throwable> throwable) {
		// 已注册静默的Service, Method或Exception标记为@QuietThrowable
		return this.quiet.quiet(this.request, throwable);
	}

	/**
	 * 是否已终端
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
	 * 是否超时
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
	 * 服务抛出异常(远程服务异常)
	 * 
	 * @return
	 */
	private AckFuture checkException() throws KeplerRemoteException {
		Assert.notNull(this.response, "Response must not be null ... ");
		if (!this.response.valid()) {
			// 如果静默则保持Done状态
			this.stauts = this.quiet(this.response.throwable().getClass()) ? Status.DONE : Status.EXCEPTION;
			// 非KeplerRemoteException表示异常为声明异常, 需包装
			throw KeplerRemoteException.class.isAssignableFrom(this.response.throwable().getClass()) ? KeplerRemoteException.class.cast(this.response.throwable()) : new KeplerRemoteException(this.response.throwable());
		}
		return this;
	}

	public Host local() {
		return this.local;
	}

	public Host target() {
		return this.target;
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
			this.stauts = Status.DONE;
			this.notifyAll();
		}
	}

	public boolean cancel(boolean interrupt) {
		// 没有Done且没有Cancel则允许
		if (!this.isDone() && this.isCancelled()) {
			synchronized (this) {
				// Guard condition
				if (this.isCancelled()) {
					return false;
				}
				this.stauts = Status.CANCEL;
				// 赋值并获取this.interrupt
				this.interrupt = interrupt;
				if (this.interrupt) {
					this.thread.interrupt();
				} else {
					this.notifyAll();
				}
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean isCancelled() {
		return Status.CANCEL.equals(this.stauts);
	}

	@Override
	public boolean isDone() {
		// 完成, 超时或异常
		return Status.DONE.equals(this.stauts) || Status.TIMEOUT.equals(this.stauts) || Status.EXCEPTION.equals(this.stauts);
	}

	@Override
	public Object get() throws InterruptedException {
		// 使用默认超时
		return this.get(this.deadline, TimeUnit.MILLISECONDS);
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException {
		// Math.min(timeout, this.deadline), 取Timeout最小值(堵塞)
		this.get4wait(Math.min(timeout, this.deadline));
		return this.response();
	}

	private void get4wait(long timeout) throws InterruptedException {
		synchronized (this) {
			while (this.continued()) {
				try {
					this.wait(timeout);
				} finally {
					// 任意跳出Wait均计算超时
					this.timeout(timeout);
				}
			}
		}
	}

	private Object response() throws InterruptedException {
		try {
			// 是否中断, 是否超时, 是否取消, 是否抛出异常
			return this.checkInterrupt().checkTimeout().checkCancel().checkException().response.response();
		} finally {
			// 收集Response信息
			this.collector.collect(this);
		}
	}

	/**
	 * Response尚未回调并且等待尚未取消
	 * 
	 * @return
	 */
	private boolean continued() {
		return !this.isDone() && !this.isCancelled();
	}

	/**
	 * 当前已消耗时间是否大于超时则标记
	 * 
	 * @param timeout
	 * @return
	 */
	private void timeout(long timeout) {
		if (this.elapse() > timeout) {
			this.stauts = Status.TIMEOUT;
		}
	}

	/**
	 * 当前耗时
	 * 
	 * @return
	 */
	public long elapse() {
		return System.currentTimeMillis() - this.start;
	}
}