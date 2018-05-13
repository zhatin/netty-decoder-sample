package com.webyun.samples.tcp.handler;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class MultiProtocolFrameDecoder extends CustomDelimiterBasedFrameDecoder {

	enum ProtocolType {
		F0(new byte[] { (byte) 0xF0 }, new byte[] { (byte) 0xFD }), F1(new byte[] { (byte) 0xF1 },
				new byte[] { (byte) 0xFE }), F2(new byte[] { (byte) 0xF2 }, new byte[] { (byte) 0xFF });

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

	enum DecoderState {
		UNKNOWN, MATCHED, UNMATCHED
	};

	private static final Logger logger = LoggerFactory.getLogger(MultiProtocolFrameDecoder.class);

	private DecoderState decoderState;

	public MultiProtocolFrameDecoder(byte[] protocol, byte[] delimiters) {
		this(Integer.MAX_VALUE, false, new ByteBuf[] { Unpooled.wrappedBuffer(delimiters) });
		this.decoderState = DecoderState.UNKNOWN;
	}

	public MultiProtocolFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf[] delimiters) {
		super(maxFrameLength, stripDelimiter, delimiters);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {

		if (this.decoderState == DecoderState.UNMATCHED) {
			return null;
		}

		if (this.decoderState == DecoderState.UNKNOWN) {
			for (ProtocolType pt : ProtocolType.values()) {
				ByteBuf buf = pt.tag();
				int idx = indexOf(buffer, buf);
				if (buf.capacity() <= buffer.readableBytes()) {
					if (0 == idx) {
						ByteBuf fd = pt.delimiter();
						logger.info(" Protocol Matched, Tag: " + Hex.encodeHexString(buf.slice().array()).toUpperCase()
								+ ", Delimiter: " + Hex.encodeHexString(fd.slice().array()).toUpperCase());
						setDelimiters(new ByteBuf[] { fd });
						this.decoderState = DecoderState.MATCHED;
					}
				}
			}
			if (this.decoderState == DecoderState.UNKNOWN) {
				this.decoderState = DecoderState.UNMATCHED;
				ctx.close();
				logger.info("Close connection when protocol NOT supported.");
			}
		}
		return super.decode(ctx, buffer);
	}

}
