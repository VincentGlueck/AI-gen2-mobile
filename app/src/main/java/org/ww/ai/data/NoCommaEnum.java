package org.ww.ai.data;

public enum NoCommaEnum {
	DO_NOT_USE_COMMA ("by");
	
	private final String name;
	
	NoCommaEnum(String name) {
		this.name = "by";
	}
	
	public boolean doNotUseComma(String name) {
		for(int n = 0; n < values().length; n++) {
			if(values()[n].getName().trim().equalsIgnoreCase(name.trim())) {
				return true;
			}
		}
		return false;
	}
	
	public String getName() {
		return name;
	}

}
