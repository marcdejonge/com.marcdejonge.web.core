package com.marcdejonge.web.core;

import com.marcdejonge.web.core.api.View;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultLastHttpContent;

public class FileView implements View {

	private final ByteBuf contents;
	private final String extension;
	private final String tag;

	public FileView(ByteBuf buffer, String fileName, String tag) {
		contents = buffer;
		extension = fileName == null ? "" : fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		this.tag = tag;
	}

	@Override
	public int resultCode() {
		return 200;
	}

	public String getExtension() {
		return extension;
	}

	@Override
	public String getContentType() {
		return View.detectContentType(extension);
	}

	public void write(ChannelHandlerContext ctx, ChannelPromise promise) {
		contents.retain(); // To make sure that sending a duplicate doesn't destroy the contents buffer
		ctx.write(new DefaultLastHttpContent(contents.duplicate()), promise);
	}

	@Override
	protected void finalize() throws Throwable {
		contents.release();
		super.finalize();
	}

	@Override
	public int getContentLength() {
		return contents.readableBytes();
	}

	@Override
	public String getCacheTag() {
		return tag;
	}
}
