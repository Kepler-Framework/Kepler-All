package com.kepler.mock.impl;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.mock.MockerResponse;

/**
 * @author KimShen
 *
 */
public class DefaultMockerResponse implements MockerResponse, Serializable {

	private static final boolean DETAIL = PropertiesUtils.get(DefaultMockerResponse.class.getName().toLowerCase() + ".detail", false);

	private static final Log LOGGER = LogFactory.getLog(DefaultMockerResponse.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Object response;

	private final String message;

	public DefaultMockerResponse(Object response, String message) {
		super();
		this.response = response;
		this.message = message;
	}

	public Object response() {
		if (DefaultMockerResponse.DETAIL) {
			DefaultMockerResponse.LOGGER.info("[message=" + this.message + "]");
		}
		return this.response;
	}

	public String toString() {
		return "[message=" + this.message + "][response=" + this.response + "]";
	}
}
