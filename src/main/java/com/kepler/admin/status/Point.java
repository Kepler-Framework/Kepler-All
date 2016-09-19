package com.kepler.admin.status;

import java.io.Serializable;
import java.util.List;

/**
 * 时间点数据
 * 
 * @author KimShen
 *
 */
public interface Point extends Serializable {

	/**
	 * 时间点
	 * 
	 * @return
	 */
	public List<Long> times();

	/**
	 * 关联时间点的数据集
	 * 
	 * @return
	 */
	public List<Long> datas();
}
