package com.webyun.samples.tcp.handler;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

public class CustomDelimiterBasedFrameDecoder extends ByteToMessageDecoder {

	private volatile ByteBuf[] delimiters;
	private int maxFrameLength;
	private boolean stripDelimiter;
	private boolean failFast;
	private boolean discardingTooLongFrame;
	private int tooLongFrameLength;

	public CustomDelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter) {
		this(maxFrameLength, true, delimiter);
	}

	public CustomDelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter) {
		this(maxFrameLength, stripDelimiter, true, delimiter);
	}

	public CustomDelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast,
			ByteBuf delimiter) {
		this(maxFrameLength, stripDelimiter, failFast,
				new ByteBuf[] { delimiter.slice(delimiter.readerIndex(), delimiter.readableBytes()) });
	}

	public CustomDelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters) {
		this(maxFrameLength, true, delimiters);
	}

	public CustomDelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf... delimiters) {
		this(maxFrameLength, stripDelimiter, true, delimiters);
	}

	public CustomDelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast,
			ByteBuf... delimiters) {
		validateMaxFrameLength(maxFrameLength);
		setDelimiters(delimiters);
		this.maxFrameLength = maxFrameLength;
		this.stripDelimiter = stripDelimiter;
		this.failFast = failFast;
	}

	@Override
	protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Object decoded = decode(ctx, in);
		if (decoded != null) {
			out.add(decoded);
		}
	}

	protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		// Try all delimiters and choose the delimiter which yields the shortest frame.
		int minFrameLength = Integer.MAX_VALUE;
		ByteBuf minDelim = null;
		for (ByteBuf delim : delimiters) {
			int frameLength = indexOf(buffer, delim);
			if (frameLength >= 0 && frameLength < minFrameLength) {
				minFrameLength = frameLength;
				minDelim = delim;
			}
		}

		if (minDelim != null) {
			int minDelimLength = minDelim.capacity();
			ByteBuf frame;

			if (discardingTooLongFrame) {
				// We've just finished discarding a very large frame.
				// Go back to the initial state.
				discardingTooLongFrame = false;
				buffer.skipBytes(minFrameLength + minDelimLength);

				int tooLongFrameLength = this.tooLongFrameLength;
				this.tooLongFrameLength = 0;
				if (!failFast) {
					fail(tooLongFrameLength);
				}
				return null;
			}

			if (minFrameLength > maxFrameLength) {
				// Discard read frame.
				buffer.skipBytes(minFrameLength + minDelimLength);
				fail(minFrameLength);
				return null;
			}

			if (stripDelimiter) {
				frame = buffer.readRetainedSlice(minFrameLength);
				buffer.skipBytes(minDelimLength);
			} else {
				frame = buffer.readRetainedSlice(minFrameLength + minDelimLength);
			}

			return frame;
		} else {
			if (!discardingTooLongFrame) {
				if (buffer.readableBytes() > maxFrameLength) {
					// Discard the content of the buffer until a delimiter is found.
					tooLongFrameLength = buffer.readableBytes();
					buffer.skipBytes(buffer.readableBytes());
					discardingTooLongFrame = true;
					if (failFast) {
						fail(tooLongFrameLength);
					}
				}
			} else {
				// Still discarding the buffer since a delimiter is not found.
				tooLongFrameLength += buffer.readableBytes();
				buffer.skipBytes(buffer.readableBytes());
			}
			return null;
		}
	}

	private void fail(long frameLength) {
		if (frameLength > 0) {
			throw new TooLongFrameException(
					"frame length exceeds " + maxFrameLength + ": " + frameLength + " - discarded");
		} else {
			throw new TooLongFrameException("frame length exceeds " + maxFrameLength + " - discarding");
		}
	}

	public static int indexOf(ByteBuf haystack, ByteBuf needle) {
		for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
			int haystackIndex = i;
			int needleIndex;
			for (needleIndex = 0; needleIndex < needle.capacity(); needleIndex++) {
				if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
					break;
				} else {
					haystackIndex++;
					if (haystackIndex == haystack.writerIndex() && needleIndex != needle.capacity() - 1) {
						return -1;
					}
				}
			}

			if (needleIndex == needle.capacity()) {
				// Found the needle from the haystack!
				return i - haystack.readerIndex();
			}
		}
		return -1;
	}

	private static void validateDelimiter(ByteBuf delimiter) {
		if (delimiter == null) {
			throw new NullPointerException("delimiter");
		}
		if (!delimiter.isReadable()) {
			throw new IllegalArgumentException("empty delimiter");
		}
	}

	private static void validateMaxFrameLength(int maxFrameLength) {
		if (maxFrameLength <= 0) {
			throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength);
		}
	}

	public ByteBuf[] getDelimiters() {
		return delimiters;
	}

	public synchronized void setDelimiters(ByteBuf[] delimiters) {
		if (delimiters == null) {
			throw new NullPointerException("delimiters");
		}
		if (delimiters.length == 0) {
			throw new IllegalArgumentException("empty delimiters");
		}

		this.delimiters = new ByteBuf[delimiters.length];
		for (int i = 0; i < delimiters.length; i++) {
			ByteBuf d = delimiters[i];
			validateDelimiter(d);
			this.delimiters[i] = d.slice(d.readerIndex(), d.readableBytes());
		}
	}

}
