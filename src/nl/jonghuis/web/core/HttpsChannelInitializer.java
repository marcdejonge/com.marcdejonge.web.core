package nl.jonghuis.web.core;

import java.io.File;

import javax.net.ssl.SSLException;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

public class HttpsChannelInitializer extends HttpChannelInitializer {

	private final SslContext ssl;

	public HttpsChannelInitializer(File chainFile, File keyFile) throws SSLException {
		ssl = SslContext.newServerContext(chainFile, keyFile);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast("ssl", ssl.newHandler(ch.alloc()));
		super.initChannel(ch);
	}
}
