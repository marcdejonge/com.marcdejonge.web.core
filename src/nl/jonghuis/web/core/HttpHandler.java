package nl.jonghuis.web.core;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class HttpHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

	private final StringBuilder responseBuffer = new StringBuilder();
	private HttpRequest httpRequest;

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			httpRequest = (HttpRequest) msg;

			if (HttpHeaderUtil.is100ContinueExpected(httpRequest)) {
				ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
			}

			responseBuffer.setLength(0); // Reset if this object is reused
			responseBuffer.append("Welcome to my server!\n");
			responseBuffer.append("Path: " + httpRequest.uri() + "\n");
			for (Entry<CharSequence, CharSequence> header : httpRequest.headers()) {
				responseBuffer.append("Header [" + header.getKey() + "] = " + header.getValue() + "\n");
			}
		}

		if (msg instanceof LastHttpContent) {
			writeResponse((LastHttpContent) msg, ctx);
		}
	}

	private void writeResponse(LastHttpContent lastPart, ChannelHandlerContext ctx) {
		boolean keepAlive = HttpHeaderUtil.isKeepAlive(httpRequest);
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
		                                                        OK,
		                                                        Unpooled.copiedBuffer(responseBuffer.toString(),
		                                                                              CharsetUtil.UTF_8));

		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		if (keepAlive) {
			response.headers().set(CONTENT_LENGTH, "" + response.content().readableBytes());
			response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}

		ChannelFuture f = ctx.writeAndFlush(response);
		if (!keepAlive) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected error during handling of HTTP connection: " + cause.getMessage(), cause);
		ctx.close();
	}
}
