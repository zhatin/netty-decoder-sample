package com.webyun.samples.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.webyun.samples.tcp.handler.TCPChannelInitializer;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Configuration
@PropertySource(value = "classpath:/properties/${application.profiles.active:local}/application.properties")
public class TCPServer {

	private static final Logger logger = LogManager.getLogger(TCPServer.class);

	private Channel serverChannel;

	@Value("${tcp.port}")
	private int tcpPort;

	@Value("${boss.thread.count}")
	private int bossCount;

	@Value("${worker.thread.count}")
	private int workerCount;

	@Value("${so.keepalive}")
	private boolean keepAlive;

	@Value("${so.backlog}")
	private int backlog;

	@SuppressWarnings("unchecked")
	@Bean(name = "serverBootstrap")
	public ServerBootstrap bootstrap() {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup(), workerGroup()).channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(tcpChannelInitializer);
		Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
		Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
		for (@SuppressWarnings("rawtypes")
		ChannelOption option : keySet) {
			b.option(option, tcpChannelOptions.get(option));
		}
		b.childOption(ChannelOption.SO_KEEPALIVE, keepAlive);
		return b;
	}

	@Autowired
	@Qualifier("tcpChannelInitializer")
	private TCPChannelInitializer tcpChannelInitializer;

	@Bean(name = "tcpChannelOptions")
	public Map<ChannelOption<?>, Object> tcpChannelOptions() {
		Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
		// options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
		options.put(ChannelOption.SO_BACKLOG, backlog);
		return options;
	}

	@Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup bossGroup() {
		return new NioEventLoopGroup(bossCount);
	}

	@Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup workerGroup() {
		return new NioEventLoopGroup(workerCount);
	}

	@Bean(name = "tcpSocketAddress")
	public InetSocketAddress tcpAddr() {
		return new InetSocketAddress(tcpPort);
	}

	@Bean(name = "channelRepository")
	public ChannelRepository channelRepository() {
		return new ChannelRepository();
	}

	public void start() throws InterruptedException {
		serverChannel = bootstrap().bind(tcpPort).sync().channel().closeFuture().sync().channel();
	}

	@PreDestroy
	public void stop() {
		if (serverChannel != null) {
			serverChannel.close();
			if (serverChannel.parent() != null)
				serverChannel.parent().close();
		}
		logger.debug("TCPServer stopped.");
	}

}
