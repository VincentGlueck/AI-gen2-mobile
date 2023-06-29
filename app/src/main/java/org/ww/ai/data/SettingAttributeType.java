package org.ww.ai.data;

import java.util.Optional;
import java.util.stream.Stream;

public enum SettingAttributeType {

	UNIQUE ("unique"),
	MIX ("mix");
	
	private final String name;
	
	private SettingAttributeType(String name) {
		this.name = name;
	}
	
	public static SettingAttributeType fromName(String name) {
		Optional<SettingAttributeType> optional = Stream.of(SettingAttributeType.values()).filter(sat -> sat.getName().equalsIgnoreCase(name)).findAny();
        if(optional.isPresent()) {
        	return optional.get();
        }
        throw new IllegalArgumentException("invalid setting type: " + name);
    }
	
	public String getName() {
		return name;
	}
	
}
