package com.webyun.samples.tcp.handler;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class MultiProtocolFrameDecoder extends CustomDelimiterBasedFrameDecoder {

	enum ProtocolType {
		F0(new byte[] { (byte) 0xF0 }, new byte[] { (byte) 0xFD }), 
		F1(new byte[] { (byte) 0xF1 }, new byte[] { (byte) 0xFE }), 
		F2(new byte[] { (byte) 0xF2 }, new byte[] { (byte) 0xFF });

		private ByteBuf protocolTag;
		private ByteBuf frameDelimiter;

		ProtocolType(byte[] tag, byte[] delimiter) {
			this.protocolTag = Unpooled.wrappedBuffer(tag);
			this.frameDelimiter = Unpooled.wrappedBuffer(delimiter);
		};

		public ByteBuf tag() {
			return protocolTag;
		}

		public ByteBuf delimiter() {
			return frameDelimiter;
		}
	}

	private static Logger logger = LogManager.getLogger(MultiProtocolFrameDecoder.class);

	private boolean hasMatached = false;

	public MultiProtocolFrameDecoder(byte[] protocol, byte[] delimiters) {
		this(Integer.MAX_VALUE, false, new ByteBuf[] { Unpooled.wrappedBuffer(delimiters) });
	}

	public MultiProtocolFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf[] delimiters) {
		super(maxFrameLength, stripDelimiter, delimiters);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {

		if (!hasMatached) {
			for (ProtocolType pt : ProtocolType.values()) {
				ByteBuf buf = pt.tag();
				int idx = indexOf(buffer, buf);
				if (buf.capacity() <= buffer.readableBytes()) {
					if (0 == idx) {
						ByteBuf fd = pt.delimiter();
						logger.info(" Protocol Matched, Tag: " + Hex.encodeHexString(buf.slice().array()).toUpperCase()
								+ ", Delimiter: " + Hex.encodeHexString(fd.slice().array()).toUpperCase());
						setDelimiters(new ByteBuf[] { fd });
						hasMatached = true;
					}
				}
			}
		}
		return super.decode(ctx, buffer);
	}

}
