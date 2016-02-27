package com.marcdejonge.web.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class LoggingChannelHandler extends ChannelHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(LoggingChannelHandler.class);

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		logger.debug("Writing out message of type {}", msg.getClass().getSimpleName());
		super.write(ctx, msg, promise);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Uncaught exception: " + cause.getMessage(), cause);
	}

}
