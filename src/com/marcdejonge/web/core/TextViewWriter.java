package com.marcdejonge.web.core;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;

public class TextViewWriter implements Appendable, AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(TextViewWriter.class);

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final ChannelHandlerContext ctx;
	private final ChannelPromise promise;

	private final CharBuffer buffer;

	public TextViewWriter(ChannelHandlerContext ctx, ChannelPromise promise) {
		this.ctx = ctx;
		this.promise = promise;
		buffer = CharBuffer.allocate(8 * 1024);
	}

	@Override
	public Appendable append(CharSequence cs) throws IOException {
		return append(cs, 0, cs.length());
	}

	@Override
	public Appendable append(char c) throws IOException {
		if (buffer.remaining() < 1) {
			writeBuffer(ctx.voidPromise());
		}

		buffer.append(c);
		return this;
	}

	@Override
	public Appendable append(CharSequence cs, int from, int until) throws IOException {
		if (buffer.remaining() < cs.length()) {
			writeBuffer(ctx.voidPromise());
		}
		buffer.append(cs, from, until);
		return this;
	}

	@Override
	public void close() {
		writeBuffer(promise);
	}

	private void writeBuffer(ChannelPromise channelPromise) {
		logger.trace("Writing buffer with {} characters (void: {})", buffer.position(), channelPromise.isVoid());

		buffer.flip();
		ByteBuf bytes = ByteBufUtil.encodeString(ctx.alloc(), buffer, UTF8);
		HttpContent msg;

		if (channelPromise.isVoid()) {
			msg = new DefaultHttpContent(bytes);
		} else {
			msg = new DefaultLastHttpContent(bytes);
		}

		ctx.write(msg, channelPromise);
		buffer.clear();
	}
}
