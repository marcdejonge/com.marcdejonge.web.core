package nl.jonghuis.web.core.api;

public interface View {
	int resultCode();

	String getContentType();

	void write(Writer writer);
}
