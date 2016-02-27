package com.marcdejonge.web.core.api;

import java.io.IOException;
import java.net.URL;

import com.marcdejonge.web.core.FileView;

public interface View {
	public static View getCachedView(URL fileUrl) throws IOException {
		return FileView.getCachedView(fileUrl);
	}

	int resultCode();

	String getContentType();

	default int getContentLength() {
		return 0;
	}
}
