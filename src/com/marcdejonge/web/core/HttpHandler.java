package com.marcdejonge.web.core;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.marcdejonge.web.core.api.View;
import com.marcdejonge.web.core.registration.ControllerTracker;
import com.marcdejonge.web.core.registration.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

public class HttpHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

	private Request request;

	private final ControllerTracker tracker;

	public HttpHandler(ControllerTracker tracker) {
		this.tracker = tracker;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			request = Request.parse(httpRequest);

			if (HttpHeaderUtil.is100ContinueExpected(httpRequest)) {
				ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
			}
		}

		if (msg instanceof LastHttpContent) {
			View view = tracker.invokeController(request);
			logger.debug("Writing view of type {}", view.getClass().getName());

			DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1,
			                                                       HttpResponseStatus.valueOf(view.resultCode()));
			response.headers().set(CONTENT_TYPE, view.getContentType());
			if (view.getContentLength() > 0) {
				response.headers().setInt(CONTENT_LENGTH, view.getContentLength());
			} else {
				response.headers().set(TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
			}
			if (request.isKeepAlive()) {
				response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}
			if (view.getCacheTag() != null) {
				response.headers().set(CACHE_CONTROL, "max-age=300");
				response.headers().set(ETAG, view.getCacheTag());
			}
			response.setDecoderResult(DecoderResult.SUCCESS);

			ChannelFuture f = ctx.write(response);
			f = ctx.write(view);
			if (!request.isKeepAlive()) {
				f.addListener(ChannelFutureListener.CLOSE);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected error during handling of HTTP connection: " + cause.getMessage(), cause);
		ctx.close();
	}
}
