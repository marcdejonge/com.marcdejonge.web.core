package nl.jonghuis.web.core.registration;

import java.util.List;

import com.google.common.base.Splitter;
import com.marcdejonge.codec.MixedMap;
import com.marcdejonge.codec.url.URLDecoder;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

public class Request {
	public static Request parse(HttpRequest req) {
		CharSequence hostname = req.headers().get(HttpHeaderNames.HOST);
		if (hostname == null) {
			hostname = "localhost";
		}

		String uri = req.uri();
		int startQuery = uri.indexOf('?');
		MixedMap params = null;
		if (startQuery >= 0) {
			params = URLDecoder.parse(uri.substring(startQuery + 1));
			uri = uri.substring(0, startQuery);
		} else {
			params = new MixedMap();
		}

		List<String> pathParts = Splitter.on('/').trimResults().omitEmptyStrings().splitToList(uri);

		return new Request(hostname, req.method(), pathParts, params, req.headers(), HttpHeaderUtil.isKeepAlive(req));
	}

	private final CharSequence hostname;
	private final HttpMethod httpMethod;
	private final List<String> pathParts;
	private final MixedMap params;
	private final HttpHeaders headers;
	private final boolean keepAlive;

	private Request(CharSequence hostname,
	                HttpMethod httpMethod,
	                List<String> pathParts,
	                MixedMap params,
	                HttpHeaders headers,
	                boolean keepAlive) {
		this.hostname = hostname;
		this.httpMethod = httpMethod;
		this.pathParts = pathParts;
		this.params = params;
		this.headers = headers;
		this.keepAlive = keepAlive;
	}

	public CharSequence getHostname() {
		return hostname;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public List<String> getPathParts() {
		return pathParts;
	}

	public MixedMap getParams() {
		return params;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	@Override
	public String toString() {
		return "RequestImpl [hostname=" + hostname
		       + ", pathParts="
		       + pathParts
		       + ", params="
		       + params
		       + ", headers="
		       + headers
		       + "]";
	}
}