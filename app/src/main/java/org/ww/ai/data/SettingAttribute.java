package org.ww.ai.data;

import java.util.List;

public class SettingAttribute {

	private String name;
	private SettingAttributeType type = SettingAttributeType.MIX;
	private List<AttributeValue> values;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SettingAttributeType getType() {
		return type;
	}
	public void setType(SettingAttributeType type) {
		this.type = type;
	}
	public List<AttributeValue> getValues() {
		return values;
	}
	public void setValues(List<AttributeValue> values) {
		this.values = values;
	}
	
	@Override
	public String toString() {
		return "SettingAttribute [name=" + name + ", type=" + type + ", values=" + values + "]";
	}
	
}
