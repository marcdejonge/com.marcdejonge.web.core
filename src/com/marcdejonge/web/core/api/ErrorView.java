package com.marcdejonge.web.core.api;

import java.io.IOException;

public class ErrorView implements TextView {

	public static final ErrorView BAD_REQUEST = new ErrorView(400, "Bad Request");

	public static final ErrorView NOT_FOUND = new ErrorView(404, "Not Found");

	public static final ErrorView INTERNAL_SERVER_ERROR = new ErrorView(500, "Internal Server Error");

	private final int resultCode;

	private final String message;

	private ErrorView(int resultCode, String message) {
		this.resultCode = resultCode;
		this.message = message;
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}

	@Override
	public void write(Appendable writer) throws IOException {
		writer.append(message);
	}

	@Override
	public int getContentLength() {
		return message.length();
	}

	@Override
	public int resultCode() {
		return resultCode;
	}
}
