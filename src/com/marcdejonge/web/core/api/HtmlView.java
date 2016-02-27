package com.marcdejonge.web.core.api;

public abstract class HtmlView implements TextView {

	@Override
	public int resultCode() {
		return 200;
	}

	@Override
	public String getContentType() {
		return "text/html; charset=utf-8";
	}

}
