package nl.jonghuis.web.core.api;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class ControllerFactory<T extends Controller> {
	private final Map<String, Method> methods;

	public ControllerFactory(Class<T> controllerType) {
		methods = new HashMap<>();

		for (Method method : controllerType.getMethods()) {
			if (Controller.class.isAssignableFrom(method.getDeclaringClass())
			    && View.class.isAssignableFrom(method.getReturnType())
			    && Modifier.isPublic(method.getModifiers())
			    && !Modifier.isStatic(method.getModifiers())) {
				methods.put(method.getName().toLowerCase(), method);
			}
		}
	}

	public abstract T getController(Headers headers);

	public Method getMethod(String methodName) {
		return methods.get(methodName.toLowerCase());
	}
}
