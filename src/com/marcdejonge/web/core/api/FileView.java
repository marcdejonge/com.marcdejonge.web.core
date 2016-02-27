package com.marcdejonge.web.core.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.io.ByteStreams;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultLastHttpContent;

public class FileView implements View {

	private static final PooledByteBufAllocator ALLOCATOR = new PooledByteBufAllocator(true);
	private static final Map<String, FileView> cache = new WeakHashMap<>();

	public static FileView getCachedView(URL fileUrl) throws IOException {
		String key = fileUrl.toString();

		synchronized (cache) {
			FileView view = cache.get(key);
			if (view != null) {
				return view;
			}
		}

		ByteBuf buffer;
		try (InputStream in = fileUrl.openStream()) {
			byte[] bs = ByteStreams.toByteArray(in);
			buffer = ALLOCATOR.directBuffer(bs.length);
			buffer.writeBytes(bs);
		}

		synchronized (cache) {
			FileView view = new FileView(buffer, fileUrl.getPath());
			cache.put(key, view);
			return view;
		}
	}

	private final ByteBuf contents;
	private final String extension;

	public FileView(ByteBuf buffer, String fileName) {
		contents = buffer;
		extension = fileName == null ? "" : fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}

	@Override
	public int resultCode() {
		return 200;
	}

	@Override
	public String getContentType() {
		switch (extension) {
		case "html":
		case "htm":
			return "text/html; charset=utf-8";
		case "css":
			return "text/css; charset=utf-8";
		case "js":
			return "application/javascript; charset=utf-8";
		case "jpg":
		case "jpeg":
			return "image/jpg";
		case "png":
			return "image/png";
		case "gif":
			return "image/gif";
		case "svg":
			return "image/svg+xml; charset=utf-8";
		default:
			return "text/plain; charset=utf-8";
		}
	}

	@Override
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
}
