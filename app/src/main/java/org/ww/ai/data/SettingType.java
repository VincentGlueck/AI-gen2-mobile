package org.ww.ai.data;

public enum SettingType {

	PRESET ("preset"),
	OPTIONAL ("optional"),
	SYSTEM ("system"),
	REQUIRED ("required");
	
	private final String name;
	
	private SettingType(String name) {
		this.name = name;
	}
	
	public static SettingType fromString(String name) {
        for (SettingType sType : SettingType.values()) {
            if (sType.name.equalsIgnoreCase(name)) {
                return sType;
            }
        }
        throw new IllegalArgumentException("invalid setting type: " + name);
    }
	
}
