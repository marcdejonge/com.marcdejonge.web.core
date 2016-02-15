package nl.jonghuis.web.core;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

public class HttpSwitcher extends SimpleChannelInboundHandler<Object> {

	private HttpRequest httpRequest;

	private final int port;

	public HttpSwitcher(int port) {
		this.port = port;
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			httpRequest = (HttpRequest) msg;
		}

		if (msg instanceof LastHttpContent) {
			DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
			                                                               HttpResponseStatus.MOVED_PERMANENTLY);

			String newUrl = "https://" + httpRequest.headers().get("Host")
			                + (port == 443 ? "" : ":" + port)
			                + httpRequest.uri();
			response.headers().add(HttpHeaderNames.LOCATION, newUrl);

			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

}
