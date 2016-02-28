package com.marcdejonge.web.core.api;

public interface View {
	public static String detectContentType(String extension) {
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

	int resultCode();

	default String getContentType() {
		return null;
	}

	default String getCacheTag() {
		return null;
	}

	default int getContentLength() {
		return 0;
	}
}
