package com.kepler.host;

import java.io.Serializable;
import java.util.UUID;

import com.kepler.config.PropertiesUtils;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author kim 2015年7月8日
 */
public interface Host extends Serializable {

	public static final String LOOP = "localhost";

	public static final String PRIORITY_KEY = Host.class.getName().toLowerCase() + ".priority";

	/**
	 * 实际主机优先级
	 */
	public static final int PRIORITY_DEF = Math.min(10, PropertiesUtils.get(Host.PRIORITY_KEY, 5));

	public static final String TAG_DEF = "";

	public static final String TAG_KEY = Host.class.getName().toLowerCase() + ".tag";

	/**
	 * 主机Tag
	 */
	public static final String TAG_VAL = PropertiesUtils.get(Host.TAG_KEY, Host.TAG_DEF);

	/**
	 * 主机令牌
	 */
	public static final String TOKEN_KEY = Host.class.getName().toLowerCase() + ".token";

	public static final String TOKEN_VAL = PropertiesUtils.get(Host.TOKEN_KEY, UUID.randomUUID().toString());

	/**
	 * 主机名称, 默认使用unknow(用于Admin)
	 */
	public static final String NAME = PropertiesUtils.get(Host.class.getName().toLowerCase() + ".name", "unknow");

	/**
	 * 物理位置
	 */
	public static final String LOCATION = PropertiesUtils.get(Host.class.getName().toLowerCase() + ".location", "").toLowerCase();

	/**
	 * 主机默认分组
	 */
	public static final String GROUP_DEF = "unknow";

	/**
	 * 主机分组, 默认使用环境变量USER(用于Admin)
	 */
	public static final String GROUP_VAL = PropertiesUtils.get(Host.class.getName().toLowerCase() + ".group", StringUtils.defaultString(System.getenv("USER"), Host.GROUP_DEF));

	public int port();

	public int priority();

	/**
	 * Server id
	 * 
	 * @return
	 */
	public String sid();

	/**
	 * 线程号
	 * 
	 * @return
	 */
	public String pid();

	public String tag();

	public String host();

	public String name();

	public String token();

	public String group();

	/**
	 * IP + Port
	 * 
	 * @return
	 */
	public String address();

	/**
	 * 物理位置
	 * 
	 * @return
	 */
	public String location();

	/**
	 * 是否为本地回路
	 * 
	 * @param host
	 * @return
	 */
	public boolean loop(Host host);

	public boolean loop(String host);
}
