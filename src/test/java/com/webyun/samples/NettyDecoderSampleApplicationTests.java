package com.webyun.samples;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.SocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.webyun.samples.tcp.ChannelRepository;
import com.webyun.samples.tcp.handler.TCPServerHandler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class NettyDecoderSampleApplicationTests {

	private TCPServerHandler tcpServerHandler;

	private ChannelHandlerContext channelHandlerContext;

	private Channel channel;

	private SocketAddress remoteAddress;

	@Before
	public void setUp() throws Exception {
		tcpServerHandler = new TCPServerHandler();
		tcpServerHandler.setChannelRepository(new ChannelRepository());

		channelHandlerContext = mock(ChannelHandlerContext.class);
		channel = mock(Channel.class);
		remoteAddress = mock(SocketAddress.class);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testChannelActive() throws Exception {
		when(channelHandlerContext.channel()).thenReturn(channel);
		when(channelHandlerContext.channel().remoteAddress()).thenReturn(remoteAddress);
		tcpServerHandler.channelActive(channelHandlerContext);
	}

	@Test
	public void testChannelRead() throws Exception {
		when(channelHandlerContext.channel()).thenReturn(channel);
		tcpServerHandler.channelRead(channelHandlerContext,
				Unpooled.wrappedBuffer(new byte[] { (byte) 0xF3, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0xF3 }));
	}

	@Test
	public void testExceptionCaught() throws Exception {

	}

}
