package org.ww.ai.phrase;

public class PhraseGeneratorException extends Exception {

	private static final long serialVersionUID = 1L;

	public PhraseGeneratorException() {
		super();
	}

	public PhraseGeneratorException(String message) {
		super(message);
	}

	public PhraseGeneratorException(Throwable cause) {
		super(cause);
	}

	public PhraseGeneratorException(String message, Throwable cause) {
		super(message, cause);
	}

	public PhraseGeneratorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
