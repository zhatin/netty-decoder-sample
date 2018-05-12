package com.webyun.samples.tcp.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.webyun.samples.tcp.ChannelRepository;

@Component
@Qualifier("tcpServerHandler")
@ChannelHandler.Sharable
public class TCPServerHandler extends ChannelInboundHandlerAdapter {

	@Autowired
	private ChannelRepository channelRepository;

	private static Logger logger = LogManager.getLogger(TCPServerHandler.class);
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Assert.notNull(this.channelRepository,
				"[Assertion failed] - ChannelRepository is required; it must not be null");

		ctx.fireChannelActive();
		logger.debug(ctx.channel().remoteAddress());
		String channelKey = ctx.channel().remoteAddress().toString();
		channelRepository.put(channelKey, ctx.channel());

		ctx.writeAndFlush("Your channel key is " + channelKey + "\n\r");

		logger.debug("Binded Channel Count is " + this.channelRepository.size());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
		// ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Assert.notNull(this.channelRepository,
				"[Assertion failed] - ChannelRepository is required; it must not be null");
		Assert.notNull(ctx, "[Assertion failed] - ChannelHandlerContext is required; it must not be null");

		String channelKey = ctx.channel().remoteAddress().toString();
		this.channelRepository.remove(channelKey);

		logger.debug("Binded Channel Count is " + this.channelRepository.size());
	}

	public void setChannelRepository(ChannelRepository channelRepository) {
		this.channelRepository = channelRepository;
	}
}
