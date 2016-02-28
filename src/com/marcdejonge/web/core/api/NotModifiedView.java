package com.marcdejonge.web.core.api;

public class NotModifiedView implements View {

	private final String extension;
	private final String tag;

	public NotModifiedView(String fileName, String tag) {
		extension = fileName == null ? "" : fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		this.tag = tag;
	}

	@Override
	public int resultCode() {
		return 304;
	}

	@Override
	public String getContentType() {
		return View.detectContentType(extension);
	}

	@Override
	public String getCacheTag() {
		return tag;
	}
}
