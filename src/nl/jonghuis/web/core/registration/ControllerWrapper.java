package nl.jonghuis.web.core.registration;

import nl.jonghuis.web.core.api.Controller;
import nl.jonghuis.web.core.api.ErrorView;
import nl.jonghuis.web.core.api.JSONResult;
import nl.jonghuis.web.core.api.View;
import nl.jonghuis.web.core.api.annotations.GetParam;
import nl.jonghuis.web.core.api.annotations.Header;
import nl.jonghuis.web.core.api.annotations.Hostname;
import nl.jonghuis.web.core.api.annotations.PathPart;
import nl.jonghuis.web.core.api.annotations.RequestType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpMethod;

public class ControllerWrapper {
	private static final Logger logger = LoggerFactory.getLogger(ControllerWrapper.class);

	private final Controller controller;

	private final Map<String, Invokable<Controller, ?>> methods;

	public ControllerWrapper(Controller controller) {
		this.controller = controller;

		methods = new HashMap<>();

		for (Method method : controller.getClass().getMethods()) {
			@SuppressWarnings("unchecked")
			Invokable<Controller, Object> m = (Invokable<Controller, Object>) Invokable.from(method);
			if (m.isPublic() && !m.isStatic() && !m.isNative()) {
				String name = m.getName().toLowerCase();
				RequestType requestTypeAnno = m.getAnnotation(RequestType.class);
				String requestType = requestTypeAnno == null ? "get" : requestTypeAnno.value().toLowerCase();

				methods.put(requestType + '$' + name, m);
			}
		}
	}

	private static final String parseName(String name, HttpMethod httpMethod) {
		name = name.toLowerCase();
		if (name.endsWith(".html")) {
			name = name.substring(0, name.length() - 5);
		}
		return httpMethod.name().toLowerCase() + name;
	}

	public View invoke(String name, Request req, int pathIndex) {
		Invokable<Controller, ?> method = methods.get(name);
		if (method == null) {
			return ErrorView.NOT_FOUND;
		}

		Object[] parameters = new Object[method.getParameters().size()];
		for (int ix = 0; ix < parameters.length; ix++) {
			Parameter parameter = method.getParameters().get(ix);
			if (parameter.getAnnotation(Hostname.class) != null && parameter.getType().isSupertypeOf(String.class)) {
				parameters[ix] = req.getHostname();
			}

			Header headerAnno = parameter.getAnnotation(Header.class);
			if (headerAnno != null && parameter.getType().isSupertypeOf(String.class)) {
				parameters[ix] = req.getHeaders().getAndConvert(headerAnno.value());
			}

			GetParam getParam = parameter.getAnnotation(GetParam.class);
			if (getParam != null) {
				parameters[ix] = req.getParams().getAs(getParam.value(), parameter.getType().getRawType(), null);
			}

			PathPart pathParam = parameter.getAnnotation(PathPart.class);
			if (req.getPathParts().size() > pathIndex && pathParam != null
			    && parameter.getType().isSubtypeOf(String.class)) {
				parameters[ix] = req.getPathParts().get(pathIndex++);
			}
		}

		try {
			Object result = method.invoke(controller, parameters);
			if (result instanceof View) {
				return (View) result;
			} else {
				return new JSONResult(result);
			}
		} catch (InvocationTargetException
		         | IllegalAccessException e) {
			logger.error("Error calling method " + method, e);
			return ErrorView.INTERNAL_SERVER_ERROR;
		}
	}

	public boolean hasController(Controller controller) {
		return controller == this.controller;
	}

	public String findMethod(String methodName, HttpMethod method) {
		String parsedName = parseName(methodName, method);
		if (methods.containsKey(parsedName)) {
			return parsedName;
		}
		parsedName = parseName("index", method);
		if (methods.containsKey(parsedName)) {
			return parsedName;
		}
		return null;
	}
}
