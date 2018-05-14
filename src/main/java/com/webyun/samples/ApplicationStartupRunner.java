package com.webyun.samples;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.webyun.samples.tcp.ChannelRepository;
import com.webyun.samples.tcp.TCPServer;
import com.webyun.samples.tcp.handler.TCPChannelInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Component
@Configuration
@PropertySource(value = "classpath:/properties/${application.profiles.active:local}/application.properties")
public class ApplicationStartupRunner {

	private static final Logger logger = LogManager.getLogger(ApplicationStartupRunner.class);
	
	@Autowired
    private ApplicationContext appContext;
	
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
		//options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
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
	public InetSocketAddress tcpPort() {
		return new InetSocketAddress(tcpPort);
	}

	@Bean(name = "channelRepository")
	public ChannelRepository channelRepository() {
		return new ChannelRepository();
	}
	
	public void run() throws BeansException, InterruptedException {

		logger.info("ApplicationStartupRunner running.");

		TCPServer tcpServer = appContext.getBean(TCPServer.class);
		tcpServer.start();

	}

}
