package com.marcdejonge.web.core.api;

public interface View {
	int resultCode();

	String getContentType();

	default int getContentLength() {
		return 0;
	}
}
