package org.ww.ai.data;

import java.util.HashMap;
import java.util.Map;

public class AttributeValue {

	private String value;
	private Map<String, String> extraData;

	public AttributeValue(String value) {
		this.value = value;
	}
	
	public AttributeValue(String value, Map<String, String> extraData) {
		this(value);
		this.extraData = extraData;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
	
	public Map<String, String> getExtraData() {
		return extraData;
	}

	public void setExtraData(Map<String, String> extraData) {
		this.extraData = extraData;
	}
	
	public void addExtraData(String key, String value) {
		if(extraData == null) {
			extraData = new HashMap<>();
		}
		extraData.put(key, value);
	}
	
	public static AttributeValue of(String str) {
		return new AttributeValue(str);
	}

	@Override
	public String toString() {
		return "AttributeValue [value=" + value + ", extraData=" + extraData + "]";
	}
	
	

}
