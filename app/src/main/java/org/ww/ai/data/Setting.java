package org.ww.ai.data;

import java.util.List;

import org.w3c.dom.Node;

public class Setting {
	
	private String name;
	private SettingType type;
	private String dependsOn;
	private List<SettingAttribute> attributes;
	private Node node;
	
	public Setting() {
		System.out.println("WARN: use of " + getClass().getCanonicalName() + " without node (null)");
	}
	
	public Setting(Node node) {
		this.setNode(node);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public SettingType getType() {
		return type;
	}
	public void setType(SettingType type) {
		this.type = type;
	}
	
	public String getDependsOn() {
		return dependsOn;
	}
	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}
	
	public List<SettingAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<SettingAttribute> attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public String toString() {
		String str = "Setting [name=" + name + ", type=" + type;
		if(dependsOn != null && !dependsOn.isEmpty()) {
			str += ", depends on " + dependsOn;
		}
		str += "]";
		StringBuilder attrs = new StringBuilder();
		if(attributes != null && !attributes.isEmpty()) {
			boolean first = true;
			for(SettingAttribute attr : attributes) {
				attrs.append(first ? "" : ", ").append(attr);
				first = false;
			}
		} else {
			attrs.append("No Attributes");
		}
		return str + " " + attrs;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	

}
