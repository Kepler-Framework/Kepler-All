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
 * HTTP工具, 自动填充Tag
 * 
 * @author kim 2015年10月19日
 */
public class TagFilter implements Filter {

	private static final String KEY = PropertiesUtils.get(TagFilter.class.getName().toLowerCase() + ".key", "_kepler.tag");

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

	private void tag(ServletRequest request) {
		String tag = HttpServletRequest.class.cast(request).getParameter(TagFilter.KEY);
		// 每次请求均重置TAG
		this.context.get().put(Host.TAG_KEY, StringUtils.hasText(tag) ? tag : Host.TAG_DEF);
	}
}
