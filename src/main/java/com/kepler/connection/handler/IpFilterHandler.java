package com.kepler.connection.handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.impl.ExceptionListener;
import com.kepler.org.apache.commons.lang.StringUtils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author longyaokun
 * @date 2016年6月29日
 */
@Sharable
public class IpFilterHandler extends ChannelInboundHandlerAdapter {

	private static final String BLACK_LIST_KEY = IpFilterHandler.class.getName().toLowerCase() + ".blacklist";

	private static final String BLACK_LIST_DEF = PropertiesUtils.get(IpFilterHandler.BLACK_LIST_KEY, "");

	private static final String IP_SEPARATOR = ",";

	private static final Log LOGGER = LogFactory.getLog(IpFilterHandler.class);

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String address = ctx.channel().remoteAddress().toString();
		if (StringUtils.isEmpty(address)) {
			IpFilterHandler.LOGGER.warn("Reject the connection because the remote adress is null... ("
					+ ctx.channel().remoteAddress().toString() + ")");
			ctx.close().addListener(ExceptionListener.TRACE);
		}
		String host = address.substring(address.indexOf("/") + 1, address.indexOf(":"));
		if (this.getBlackList().contains(host)) {
			IpFilterHandler.LOGGER.warn("Reject the connection because the remote address is in the black list... ("
					+ ctx.channel().remoteAddress().toString() + ")");
			ctx.close().addListener(ExceptionListener.TRACE);
		} else {
			ctx.fireChannelActive();
		}
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelInactive();
	}

	private List<String> getBlackList() {
		String blackListConfig = PropertiesUtils.get(IpFilterHandler.BLACK_LIST_KEY, IpFilterHandler.BLACK_LIST_DEF);
		if (StringUtils.isEmpty(blackListConfig)) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(blackListConfig.split(IpFilterHandler.IP_SEPARATOR));
		}
	}

}
