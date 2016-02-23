package nl.jonghuis.web.core.api;

import java.io.IOException;

public interface View {
	int resultCode();

	String getContentType();

	default int getContentLength() {
		return 0;
	}

	void write(Appendable writer) throws IOException;
}
