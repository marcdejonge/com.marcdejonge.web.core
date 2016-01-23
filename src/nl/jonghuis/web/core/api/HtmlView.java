package nl.jonghuis.web.core.api;

public abstract class HtmlView implements View {

	@Override
	public int resultCode() {
		return 200;
	}

	@Override
	public String getContentType() {
		return "text/html";
	}

}
