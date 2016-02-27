package com.marcdejonge.web.core;

import java.io.IOException;

import com.marcdejonge.web.core.api.TextView;

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
		if (msg instanceof FileView) {
			FileView view = (FileView) msg;
			view.write(ctx, promise);
		} else if (msg instanceof TextView) {
			TextView view = (TextView) msg;

			try {
				try (TextViewWriter w = new TextViewWriter(ctx, promise)) {
					view.write(w);
				}
			} catch (IOException e) {
				ctx.fireExceptionCaught(e);
			}

		} else {
			ctx.write(msg, promise);
		}
	}
}
