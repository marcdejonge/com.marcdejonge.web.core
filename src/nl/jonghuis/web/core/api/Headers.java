package nl.jonghuis.web.core.api;

public interface Headers {
	CharSequence get(CharSequence name);

	boolean contains(CharSequence name);

	boolean contains(CharSequence name, CharSequence value, boolean ignoreCaseValue);

	String getContentType();
}
