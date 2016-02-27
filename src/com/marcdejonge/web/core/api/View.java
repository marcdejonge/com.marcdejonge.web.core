package com.marcdejonge.web.core.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public interface View {
	int resultCode();

	String getContentType();

	default int getContentLength() {
		return 0;
	}

	void write(ChannelHandlerContext ctx, ChannelPromise promise);
}
