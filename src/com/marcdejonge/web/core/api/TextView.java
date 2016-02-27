package com.marcdejonge.web.core.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;

public abstract class TextView implements View {

	@Override
	public final void write(ChannelHandlerContext ctx, ChannelPromise promise) {
		try {
			try (TextWriter w = new TextWriter(ctx, promise)) {
				write(w);
			}
		} catch (IOException e) {
			ctx.fireExceptionCaught(e);
		}
	}

	public abstract void write(Appendable writer) throws IOException;

	private static class TextWriter implements Appendable, AutoCloseable {
		private static final Logger logger = LoggerFactory.getLogger(TextWriter.class);

		private static final Charset UTF8 = Charset.forName("UTF-8");

		private final ChannelHandlerContext ctx;
		private final ChannelPromise promise;

		private final ByteBuffer buffer;
		private final CharsetEncoder encoder;

		public TextWriter(ChannelHandlerContext ctx, ChannelPromise promise) {
			this.ctx = ctx;
			this.promise = promise;
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
				writeBuffer(ctx.voidPromise());
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
					writeBuffer(ctx.voidPromise());
				}
			}
			return this;
		}

		@Override
		public void close() {
			writeBuffer(promise);
			ctx.flush();
		}

		private void writeBuffer(ChannelPromise channelPromise) {
			buffer.flip();
			logger.trace("Writing buffer with {} bytes (void: {})", buffer.remaining(), channelPromise.isVoid());

			HttpContent msg;

			if (channelPromise.isVoid()) {
				msg = new DefaultHttpContent(Unpooled.copiedBuffer(buffer));
			} else {
				msg = new DefaultLastHttpContent(Unpooled.copiedBuffer(buffer));
			}

			ctx.write(msg, channelPromise);
			buffer.clear();
		}
	}

}
