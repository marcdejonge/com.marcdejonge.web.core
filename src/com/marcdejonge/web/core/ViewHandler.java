package com.marcdejonge.web.core;

import com.marcdejonge.web.core.api.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ViewHandler extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ViewHandler.class);

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		logger.trace("Write( {} )", msg.getClass().getSimpleName());
		if (msg instanceof View) {
			View view = (View) msg;
			view.write(ctx, promise);
		} else {
			ctx.write(msg, promise);
		}
	}
}
