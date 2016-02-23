package nl.jonghuis.web.core.registration;

import nl.jonghuis.web.core.api.Controller;
import nl.jonghuis.web.core.api.ErrorView;
import nl.jonghuis.web.core.api.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component
public class ControllerTracker {
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
		controllers.put(name, new ControllerWrapper(controller));
	}

	public void removeController(Controller controller, Map<String, Object> properties) {
		String name = getName(controller, properties);
		ControllerWrapper wrapper = controllers.get(name);
		if (wrapper != null && wrapper.hasController(controller)) {
			controllers.remove(name);
		}
	}

	public View invokeController(Request request) {
		List<String> pathParts = request.getPathParts();
		int pathIndex = 0;
		ControllerWrapper controller = pathParts.size() > pathIndex
		        ? controllers.get(pathParts.get(pathIndex).toLowerCase())
		        : null;
		if (controller == null) {
			controller = controllers.get("root");
			if (controller == null) {
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
			return ErrorView.NOT_FOUND;
		}

		return controller.invoke(methodName, request, pathIndex);
	}
}
