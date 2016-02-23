package nl.jonghuis.web.core;

import nl.jonghuis.web.core.api.View;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ViewHandler extends ChannelHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof View) {
			View view = (View) msg;

			try (ViewWriter w = new ViewWriter(ctx)) {
				view.write(w);
			}
		}
	}
}
