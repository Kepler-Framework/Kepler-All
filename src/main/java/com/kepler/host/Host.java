package com.kepler.host;

import java.io.Serializable;
import java.util.UUID;

import com.kepler.config.PropertiesUtils;

/**
 * @author kim 2015年7月8日
 */
public interface Host extends Serializable {

	public final static String LOOP = "localhost";

	public final static String PRIORITY_KEY = Host.class.getName().toLowerCase() + ".priority";

	/**
	 * 实际主机优先级
	 */
	public final static int PRIORITY_DEF = Math.min(10, PropertiesUtils.get(Host.PRIORITY_KEY, 5));

	public final static String TAG_DEF = "";

	public final static String TAG_KEY = Host.class.getName().toLowerCase() + ".tag";

	/**
	 * 主机Tag
	 */
	public final static String TAG_VAL = PropertiesUtils.get(Host.TAG_KEY, Host.TAG_DEF);

	/**
	 * 主机令牌
	 */
	public final static String TOKEN_KEY = Host.class.getName().toLowerCase() + ".token";

	public final static String TOKEN_VAL = PropertiesUtils.get(Host.TOKEN_KEY, UUID.randomUUID().toString());

	/**
	 * 主机分组, 默认使用环境变量USER
	 */
	public final static String GROUP = PropertiesUtils.get(Host.class.getName().toLowerCase() + ".group", System.getenv("USER") != null ? System.getenv("USER") : "unkonw");

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

	public String token();

	public String group();

	/**
	 * IP + Port
	 * 
	 * @return
	 */
	public String address();

	/**
	 * 是否为本地回路
	 * 
	 * @param host
	 * @return
	 */
	public boolean loop(Host host);

	public boolean loop(String host);
}
