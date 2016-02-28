package com.marcdejonge.web.core;

import io.netty.handler.codec.http.HttpContent;

public interface ContentHandler<T> {

	void handle(HttpContent msg);

	T getContents();

}
