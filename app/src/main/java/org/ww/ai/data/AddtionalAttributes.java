package org.ww.ai.data;

import java.util.Optional;
import java.util.stream.Stream;

public enum AddtionalAttributes {

	RESOLUTION ("resolution"),
	PRESET ("preset"),
	ARTISTS ("artists"),
	CAMERA ("camera"),
	RANDOM ("random");
	private final String name;
	
	AddtionalAttributes(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static AddtionalAttributes fromName(String name) {
		Optional<AddtionalAttributes> optional = Stream.of(AddtionalAttributes.values()).filter(attr -> attr.getName().equalsIgnoreCase(name)).findAny();
		return optional.orElse(null);
	}
	
}
