package nl.jonghuis.web.core.api;

import java.io.IOException;

import com.marcdejonge.codec.json.JSONEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONResult implements View {
	private static final Logger logger = LoggerFactory.getLogger(JSONResult.class);

	private final Object object;

	public JSONResult(Object object) {
		this.object = object;
	}

	@Override
	public int resultCode() {
		return 200;
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public void write(Appendable writer) {
		try {
			JSONEncoder.encode(object, writer);
		} catch (IOException e) {
			logger.error("Could not write result as JSON", e);
		}
	}
}
