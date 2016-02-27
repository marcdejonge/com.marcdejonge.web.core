package com.marcdejonge.web.core.registration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marcdejonge.web.core.api.Controller;
import com.marcdejonge.web.core.api.ErrorView;
import com.marcdejonge.web.core.api.View;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ControllerTracker.class)
public class ControllerTracker {
	private static final Logger logger = LoggerFactory.getLogger(ControllerTracker.class);

	private final Map<String, ControllerWrapper> controllers = new HashMap<>();

	private String getName(Controller controller, Map<String, Object> properties) {
		if (properties.containsKey("ControllerName")) {
			return properties.get("ControllerName").toString().toLowerCase();
		}

		String name = controller.getClass().getSimpleName().toLowerCase();
		if (name.endsWith("controller")) {
			name = name.substring(0, name.length() - "controller".length());
		}
		return name.toLowerCase();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addController(Controller controller, Map<String, Object> properties) {
		String name = getName(controller, properties);
		if (controllers.containsKey(name)) {
			throw new IllegalArgumentException("Second controller on the nome " + name + " detected");
		}
		logger.debug("Adding controller for {}", name);
		controllers.put(name, new ControllerWrapper(controller));
	}

	public void removeController(Controller controller, Map<String, Object> properties) {
		String name = getName(controller, properties);
		ControllerWrapper wrapper = controllers.get(name);
		if (wrapper != null && wrapper.hasController(controller)) {
			logger.debug("Removing controller for {}", name);
			controllers.remove(name);
		}
	}

	public View invokeController(Request request) {
		logger.debug("Invoking for request: " + request);

		List<String> pathParts = request.getPathParts();
		int pathIndex = 0;
		ControllerWrapper controller = pathParts.size() > pathIndex
		        ? controllers.get(pathParts.get(pathIndex).toLowerCase())
		        : null;
		if (controller == null) {
			controller = controllers.get("root");
			if (controller == null) {
				logger.debug("Controller not found");
				return ErrorView.NOT_FOUND;
			}
		} else {
			pathIndex++;
		}

		String methodName = null;
		if (pathParts.size() > pathIndex) {
			methodName = controller.findMethod(pathParts.get(pathIndex), request.getHttpMethod());
		} else {
			methodName = controller.findMethod("index", request.getHttpMethod());
		}
		if (methodName == null) {
			logger.debug("Method not found");
			return ErrorView.NOT_FOUND;
		}

		return controller.invoke(methodName, request, pathIndex);
	}
}
