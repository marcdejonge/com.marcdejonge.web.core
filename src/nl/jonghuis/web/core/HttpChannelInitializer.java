package nl.jonghuis.web.core;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final int switchToHttpsPort;

	public HttpChannelInitializer() {
		this(false, 0);
	}

	public HttpChannelInitializer(boolean alwaysSwitchToHttps, int httpsPort) {
		if (alwaysSwitchToHttps && httpsPort > 0) {
			switchToHttpsPort = httpsPort;
		} else {
			switchToHttpsPort = 0;
		}
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline()
		  .addLast(new HttpRequestDecoder())
		  .addLast(new HttpObjectAggregator(1024 * 1024))
		  .addLast(new HttpResponseEncoder());

		if (switchToHttpsPort > 0) {
			ch.pipeline().addLast(new HttpSwitcher(switchToHttpsPort));
		} else {
			ch.pipeline().addLast(new HttpHandler());
		}
	}

}
