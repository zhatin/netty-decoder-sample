package com.webyun.samples.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("tcpChannelInitializer")
public class TCPChannelInitializer extends ChannelInitializer<SocketChannel> {

	private static final int ALL_IDLE_SECONDS = 5;

	@Autowired
	@Qualifier("tcpServerHandler")
	private ChannelInboundHandlerAdapter tcpServerHandler;

	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline pipeline = socketChannel.pipeline();

		pipeline.addLast(new IdleStateHandler(0, 0, ALL_IDLE_SECONDS, TimeUnit.SECONDS) {
			@Override
			protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
				if (evt.state() == IdleState.ALL_IDLE) {
					ctx.close();
				}
				super.channelIdle(ctx, evt);
			}
		});

		pipeline.addLast(tcpServerHandler);
	}
}
