package com.marcdejonge.web.core.api;

import java.io.IOException;

public interface TextView extends View {
	void write(Appendable writer) throws IOException;
}
