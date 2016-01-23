package nl.jonghuis.web.core.registration;

import nl.jonghuis.web.core.api.Controller;
import nl.jonghuis.web.core.api.ControllerFactory;
import nl.jonghuis.web.core.api.ErrorView;
import nl.jonghuis.web.core.api.GetParam;
import nl.jonghuis.web.core.api.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders;

public class ControllerTracker {
	private final Map<String, ControllerFactory<?>> controllers = new HashMap<>();

	private String getName(ControllerFactory<?> factory) {
		String name = factory.getClass().getSimpleName().toLowerCase();
		if (name.endsWith("controller")) {
			name = name.substring(0, name.length() - "controller".length());
		}
		return name;
	}

	public void addControllerFactory(ControllerFactory<?> factory) {
		controllers.put(getName(factory), factory);
	}

	public void removeControllerFactory(ControllerFactory<?> factory) {
		controllers.remove(getName(factory));
	}

	public View invokeController(String path, HttpHeaders headers) {
		Path parsedPath = new Path(path);

		ControllerFactory<?> factory = controllers.get(parsedPath.getControllerName());
		Controller controller = factory.getController(new HeadersImpl(headers));
		Method method = factory.getMethod(parsedPath.getMethodName());

		Parameter[] params = method.getParameters();
		Object[] args = new Object[params.length];
		Iterator<String> it = parsedPath.getPathParameters().iterator();

		for (int ix = 0; ix < params.length; ix++) {
			Parameter param = params[ix];

			if (param.getAnnotation(GetParam.class) != null) {
				args[ix] = parsedPath.getGetParameters();
			} else if (param.getType() == String.class) {
				if (it.hasNext()) {
					args[ix] = it.next();
				}
			} else if (param.getType() == int.class) {
				if (it.hasNext()) {
					try {
						args[ix] = Integer.parseInt(it.next());
					} catch (NumberFormatException ex) {
						args[ix] = 0;
					}
				}
			} else if (param.getType() == long.class || param.getType() == Long.class) {
				if (it.hasNext()) {
					try {
						args[ix] = Long.parseLong(it.next());
					} catch (NumberFormatException ex) {
						args[ix] = 0L;
					}
				}
			}
		}

		try {
			return (View) method.invoke(controller, args);
		} catch (IllegalAccessException e) {
			return ErrorView.INTERNAL_SERVER_ERROR;
		} catch (IllegalArgumentException e) {
			return ErrorView.INTERNAL_SERVER_ERROR;
		} catch (InvocationTargetException e) {
			return ErrorView.INTERNAL_SERVER_ERROR;
		}
	}
}
