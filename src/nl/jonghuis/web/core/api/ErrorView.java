package nl.jonghuis.web.core.api;

public class ErrorView implements View {

	public static final ErrorView INTERNAL_SERVER_ERROR = new ErrorView(500, "Internal Server Error");

	private final int resultCode;

	private final String message;

	private ErrorView(int resultCode, String message) {
		this.resultCode = resultCode;
		this.message = message;
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}

	@Override
	public void write(Writer writer) {
		writer.write(message);
	}

	@Override
	public int resultCode() {
		return resultCode;
	}
}
