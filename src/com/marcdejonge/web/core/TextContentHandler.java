package com.marcdejonge.web.core;

import java.nio.charset.Charset;

import io.netty.handler.codec.http.HttpContent;

public class TextContentHandler implements ContentHandler<String> {
	private static final Charset UTF8 = Charset.forName("UTF8");

	private final StringBuilder sb = new StringBuilder();

	@Override
	public void handle(HttpContent msg) {
		sb.append(msg.content().toString(UTF8));
	}

	@Override
	public String getContents() {
		return sb.toString();
	}

}
