package com.kepler;

/**
 * @author kim 2015年8月28日
 */
public class KeplerRemoteException extends KeplerException {

	private static final long serialVersionUID = 1L;

	public KeplerRemoteException(String e) {
		super(e);
	}

	public KeplerRemoteException(Throwable e) {
		super(e);
	}

	/**
	 * 获取Root Exception(递归)
	 * 
	 * @return
	 */
	public Throwable cause() {
		return super.getCause() != null ? super.getCause() : this;
	}
}
