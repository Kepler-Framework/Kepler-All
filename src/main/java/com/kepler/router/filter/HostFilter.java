package com.kepler.router.filter;

import java.util.List;

import com.kepler.host.Host;
import com.kepler.protocol.Request;

/**
 * 过滤器
 * 
 * @author zhangjiehao 2015年9月7日
 */
public interface HostFilter {

	public List<Host> filter(Request request, List<Host> hosts);
}
