package nl.jonghuis.web.core.registration;

import nl.jonghuis.web.core.api.Headers;

import io.netty.handler.codec.http.HttpHeaders;

public class HeadersImpl implements Headers {
	private final HttpHeaders headers;

	HeadersImpl(HttpHeaders headers) {
		this.headers = headers;
	}

	@Override
	public CharSequence get(CharSequence name) {
		return headers.get(name);
	}

	@Override
	public boolean contains(CharSequence name) {
		return headers.contains(name);
	}

	@Override
	public boolean contains(CharSequence name, CharSequence value, boolean ignoreCaseValue) {
		return headers.contains(name, value, ignoreCaseValue);
	}

	@Override
	public String getContentType() {
		return headers.get("Content-Type").toString();
	}
}
