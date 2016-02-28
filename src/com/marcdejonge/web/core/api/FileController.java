package com.marcdejonge.web.core.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.io.ByteStreams;
import com.marcdejonge.web.core.FileView;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FileController {
	protected final Bundle bundle = FrameworkUtil.getBundle(getClass());
	private final Map<String, FileView> cache = new WeakHashMap<>();

	protected FileView getCachedView(URL fileUrl) throws IOException {
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
			buffer = Unpooled.directBuffer(bs.length);
			buffer.writeBytes(bs);
		}

		synchronized (cache) {
			FileView view = new FileView(buffer, fileUrl.getPath());
			cache.put(key, view);
			return view;
		}
	}

	protected View findFile(String path) throws IOException {
		if (path.isEmpty()) {
			path = "index.html";
		}

		URL url = bundle.getResource("web/" + path);
		if (url != null) {
			return getCachedView(url);
		} else {
			return ErrorView.NOT_FOUND;
		}
	}
}
