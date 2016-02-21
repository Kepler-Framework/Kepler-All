package com.kepler.connection.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.kepler.config.PropertiesUtils;
import com.kepler.header.HeadersContext;
import com.kepler.host.Host;

/**
 * @author kim 2015年10月19日
 */
public class TagFilter implements Filter {

	private final static String KEY = PropertiesUtils.get(TagFilter.class.getName().toLowerCase() + ".key", "_kepler.tag");

	private HeadersContext context;

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.context = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext()).getBean(HeadersContext.class);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		this.tag(request);
		chain.doFilter(request, response);
	}

	/**
	 * 从Request获取Tag值并放入上下文
	 * 
	 */
	private void tag(ServletRequest request) {
		String tag = HttpServletRequest.class.cast(request).getParameter(TagFilter.KEY);
		if (StringUtils.hasText(tag)) {
			// 用于客户端服务调用时传递Header (In Client Threads, 客户端线程如Tomcat)
			this.context.get().put(Host.TAG_KEY, tag);
		}
	}
}
