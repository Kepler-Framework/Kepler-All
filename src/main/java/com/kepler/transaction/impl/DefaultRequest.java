package com.kepler.transaction.impl;

import java.util.UUID;

import com.kepler.transaction.Transcation;
import com.kepler.transaction.TranscationRequest;

/**
 * @author KimShen
 *
 */
public class DefaultRequest implements TranscationRequest {

	private static final long serialVersionUID = 1L;

	/**
	 * 事务号
	 */
	private final String uuid = UUID.randomUUID().toString();

	private final Class<? extends Transcation> rollback;

	private final Class<? extends Transcation> main;

	private final Object[] args;

	public DefaultRequest(Class<? extends Transcation> main, Object... args) {
		super();
		this.rollback = main;
		this.main = main;
		this.args = args;
	}

	public DefaultRequest(Class<? extends Transcation> main, Class<? extends Transcation> rollback, Object... args) {
		super();
		this.rollback = rollback;
		this.main = main;
		this.args = args;
	}

	public String uuid() {
		return this.uuid;
	}

	@Override
	public Object[] args() {
		return this.args;
	}

	@Override
	public Class<? extends Transcation> main() {
		return this.main;
	}

	@Override
	public Class<? extends Transcation> rollback() {
		return this.rollback;
	}
}
