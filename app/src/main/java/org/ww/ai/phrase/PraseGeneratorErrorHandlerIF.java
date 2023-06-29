package org.ww.ai.phrase;

public interface PraseGeneratorErrorHandlerIF {
	
	public enum Severity {
		INFO,
		WARN,
		ERROR,
		FATAL
	}
	
	void handleGeneratorError(PhraseGeneratorException exception);
	
	void handleGeneratorError(PhraseGeneratorException exception, Severity severity);

}
