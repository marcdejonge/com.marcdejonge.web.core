package com.marcdejonge.web.core;

import java.io.File;

import com.marcdejonge.web.core.registration.ControllerTracker;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

@Component(immediate = true, configurationPolicy = ConfigurationPolicy.OPTIONAL)
@Designate(factory = false, ocd = HttpConfiguration.class)
public class HttpServer {
	static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private static class CloseLogger implements GenericFutureListener<Future<Void>> {
		private final String what;

		public CloseLogger(String what) {
			this.what = what;
		}

		@Override
		public void operationComplete(Future<Void> future) throws Exception {
			if (future.cause() == null) {
				logger.info("{} closed", what);
			} else {
				logger.error("Could not open {}: {}", what, future.cause().getMessage());
			}
		}
	}

	private ControllerTracker tracker;

	@Reference
	public void setTracker(ControllerTracker tracker) {
		this.tracker = tracker;
	}

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	@Activate
	public void activate(HttpConfiguration config) {
		logger.info("Booting HttpServer");

		bossGroup = new NioEventLoopGroup(config.nrOfConnectionHandlers());
		workerGroup = new NioEventLoopGroup(config.nrOfWorkers());

		boolean hasHttps = false;
		if (config.httpsPort() > 0
		    && !config.sslChainFile().isEmpty()
		    && !config.sslKeyFile().isEmpty()) {

			try {
				ServerBootstrap b = new ServerBootstrap();
				Channel httpsChannel = b.group(bossGroup, workerGroup)
				                        .channel(NioServerSocketChannel.class)
				                        .option(ChannelOption.SO_BACKLOG, 128)
				                        .childOption(ChannelOption.SO_KEEPALIVE, true)
				                        .childHandler(new HttpsChannelInitializer(new File(config.sslChainFile()),
				                                                                  new File(config.sslKeyFile()),
				                                                                  tracker))
				                        .bind(config.httpsPort())
				                        .channel();
				httpsChannel.closeFuture().addListener(new CloseLogger("HTTPS channel"));
				logger.info("HTTPS channel opened on port {}", config.httpsPort());
				hasHttps = true;
			} catch (Exception ex) {
				logger.error("Could not initialize HTTPS channel: " +
				             ex.getMessage());
			}
		}

		ServerBootstrap b = new ServerBootstrap();
		Channel httpChannel = b.group(bossGroup, workerGroup)
		                       .channel(NioServerSocketChannel.class)
		                       .option(ChannelOption.SO_BACKLOG, 128)
		                       .childOption(ChannelOption.SO_KEEPALIVE, true)
		                       .childHandler(new HttpChannelInitializer(config.alwaysSwitchToHttps() && hasHttps,
		                                                                config.httpsPort(),
		                                                                tracker))
		                       .bind(config.httpPort())
		                       .channel();
		httpChannel.closeFuture().addListener(new CloseLogger("HTTP channel"));
		logger.info("HTTP channel opened on port {}", config.httpPort());
	}

	@Deactivate
	public void deactivate() {
		logger.info("Closing down all HTTP channels...");

		try {
			workerGroup.shutdownGracefully().syncUninterruptibly();
		} catch (Exception ex) {
			logger.error("Error during shutdown of the connection handlers", ex);
		}

		try {
			bossGroup.shutdownGracefully().syncUninterruptibly();
		} catch (Exception ex) {
			logger.error("Error during shutdown of the connection handlers", ex);
		}

		logger.info("HTTP server completely shutdown");
	}
}
