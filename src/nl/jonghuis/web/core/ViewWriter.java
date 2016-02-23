package nl.jonghuis.web.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class ViewWriter implements Appendable, AutoCloseable {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final ChannelHandlerContext ctx;
	private final ByteBuffer buffer;
	private final CharsetEncoder encoder;

	public ViewWriter(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		buffer = ByteBuffer.allocate(8 * 1024);
		encoder = UTF8.newEncoder();
	}

	@Override
	public Appendable append(CharSequence cs) throws IOException {
		return append(cs, 0, cs.length());
	}

	@Override
	public Appendable append(char c) throws IOException {
		if (buffer.remaining() < 16) {
			writeBuffer();
		}
		encoder.encode(CharBuffer.wrap(new char[] { c }), buffer, false);
		return this;
	}

	@Override
	public Appendable append(CharSequence cs, int from, int until) throws IOException {
		CharBuffer charBuffer = CharBuffer.wrap(cs, from, until);

		while (charBuffer.remaining() > 0) {
			encoder.encode(charBuffer, buffer, false);
			if (buffer.remaining() < 16) {
				writeBuffer();
			}
		}
		return this;
	}

	@Override
	public void close() throws Exception {
		if (buffer.remaining() > 8) {
			writeBuffer();
		}

		encoder.flush(buffer);
		writeBuffer();
	}

	private void writeBuffer() {
		if (buffer.position() > 0) {
			buffer.flip();
			ctx.write(Unpooled.copiedBuffer(buffer));
			buffer.clear();
		}
	}
}
