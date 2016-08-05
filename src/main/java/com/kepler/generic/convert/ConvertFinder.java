package com.kepler.generic.convert;

/**
 * @author KimShen
 *
 */
public interface ConvertFinder {

	/**
	 * @param target 目标Convert
	 * @return
	 * @throws Exception
	 */
	public Convert find(String target) throws Exception;
}
