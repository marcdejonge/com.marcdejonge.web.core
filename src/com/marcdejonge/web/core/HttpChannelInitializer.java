package com.marcdejonge.web.core;

import com.marcdejonge.web.core.registration.ControllerTracker;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final int switchToHttpsPort;
	private final ControllerTracker tracker;

	public HttpChannelInitializer(ControllerTracker tracker) {
		this(false, 0, tracker);
	}

	public HttpChannelInitializer(boolean alwaysSwitchToHttps, int httpsPort, ControllerTracker tracker) {
		this.tracker = tracker;
		if (alwaysSwitchToHttps && httpsPort > 0) {
			switchToHttpsPort = httpsPort;
		} else {
			switchToHttpsPort = 0;
		}
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline()
		  // .addLast(new LoggingChannelHandler())
		  .addLast(new HttpResponseEncoder())
		  .addLast(new HttpRequestDecoder())
		  .addLast(new HttpObjectAggregator(1024 * 1024))
		  .addLast(new ViewHandler());

		if (switchToHttpsPort > 0) {
			ch.pipeline().addLast(new HttpSwitcher(switchToHttpsPort));
		} else {
			ch.pipeline().addLast(new HttpHandler(tracker));
		}
	}

}
