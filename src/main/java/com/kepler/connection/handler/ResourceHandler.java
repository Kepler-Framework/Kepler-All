package com.kepler.connection.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.impl.ExceptionListener;

/**
 * @author kim
 *
 * 2016年2月17日
 */
@Sharable
public class ResourceHandler extends ChannelInboundHandlerAdapter {

	private static final String RESOURCE_KEY = ResourceHandler.class.getName().toLowerCase() + ".resources";

	private static final int RESOURCE_DEF = PropertiesUtils.get(ResourceHandler.RESOURCE_KEY, Integer.MAX_VALUE);

	private static final Log LOGGER = LogFactory.getLog(ResourceHandler.class);

	private final AtomicInteger resources = new AtomicInteger();

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (this.resources.incrementAndGet() < PropertiesUtils.get(ResourceHandler.RESOURCE_KEY, ResourceHandler.RESOURCE_DEF)) {
			ctx.fireChannelActive();
		} else {
			ResourceHandler.LOGGER.warn("Too many open connection ... (" + ctx.channel().remoteAddress() + ") (" + this.resources + ")");
			ctx.close().addListener(ExceptionListener.TRACE);
		}
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 钝化时释放
		this.resources.decrementAndGet();
		ctx.fireChannelInactive();
	}

}
