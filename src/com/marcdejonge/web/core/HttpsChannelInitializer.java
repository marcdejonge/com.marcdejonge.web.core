package com.marcdejonge.web.core;

import java.io.File;

import javax.net.ssl.SSLException;

import com.marcdejonge.web.core.registration.ControllerTracker;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

public class HttpsChannelInitializer extends HttpChannelInitializer {

	private final SslContext ssl;

	public HttpsChannelInitializer(File chainFile, File keyFile, ControllerTracker tracker) throws SSLException {
		super(tracker);
		ssl = SslContext.newServerContext(chainFile, keyFile);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast("ssl", ssl.newHandler(ch.alloc()));
		super.initChannel(ch);
	}
}
