package nl.jonghuis.web.core.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.marcdejonge.codec.MixedMap;
import com.marcdejonge.codec.url.URLDecoder;

public class Path {
	private static final String DEFAULT_CONTROLLER = "";
	private static final String DEFAULT_METHOD = "index";

	private final String controllerName;
	private final String methodName;
	private final List<String> pathParameters;
	private final MixedMap getParameters;

	public Path(String path) {
		int ixQuery = path.indexOf('?');
		if (ixQuery >= 0) {
			getParameters = URLDecoder.parse(path.substring(ixQuery + 1));
			path = path.substring(0, ixQuery);
		} else {
			getParameters = new MixedMap();
		}

		StringTokenizer tok = new StringTokenizer(path, "/.");

		controllerName = tok.hasMoreTokens() ? tok.nextToken() : DEFAULT_CONTROLLER;
		methodName = tok.hasMoreTokens() ? tok.nextToken() : DEFAULT_METHOD;

		pathParameters = new ArrayList<>();
		while (tok.hasMoreTokens()) {
			String param = tok.nextToken();
			if (!param.equals("html") || tok.hasMoreTokens()) {
				pathParameters.add(param);
			}
		}
	}

	public String getControllerName() {
		return controllerName;
	}

	public String getMethodName() {
		return methodName;
	}

	public Iterable<String> getPathParameters() {
		return pathParameters;
	}

	public MixedMap getGetParameters() {
		return getParameters;
	}
}
