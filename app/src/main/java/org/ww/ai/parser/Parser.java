package org.ww.ai.parser;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.ww.ai.data.AttributeValue;
import org.ww.ai.data.Setting;
import org.ww.ai.data.SettingAttribute;
import org.ww.ai.data.SettingAttributeType;
import org.ww.ai.data.SettingType;
import org.ww.ai.data.SettingsCollection;
import org.ww.ai.tools.ResourceException;
import org.ww.ai.tools.ResourceLoader;
import org.ww.ai.tools.ResourceLoaderIF;
import org.xml.sax.SAXException;

public class Parser implements ResourceLoaderIF {

	private static final String EMPTY_GENERATOR_XML = "ai-generator tag exists but has no settings at all!";
	private static final String ROOT_XML_ELEMENT = "ai-generator";
	private static final String INVALID_ROOT_ELEMENT = "<" + ROOT_XML_ELEMENT
			+ "> has to be the root element in your xml file and it has to be unique!";
	private static final String GENERATOR_DOES_NOT_UNDERSTAND_YOU = "i simply do not have any rules in order to work, like generator.xml or similar...\ncheck your environment!";
	private static final String WHERE_WHEN_WHY_NO_RESOURCE = "you probably added a " + ROOT_XML_ELEMENT
			+ " xml to project, but, too bad, can't read it.";

	public SettingsCollection getSettings(final Context context, final String resourceName)
			throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		InputStream in;
		try {
			in = getResourceInputStream(context, resourceName);
		} catch (ResourceException e) {
			throw new IOException(WHERE_WHEN_WHY_NO_RESOURCE);
		}
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(in);
		document.getDocumentElement().normalize();
		NodeList nodeList = document.getElementsByTagName(ROOT_XML_ELEMENT);
		if (nodeList.getLength() != 1) {
			throw new IllegalArgumentException(INVALID_ROOT_ELEMENT);
		}
		Node root = nodeList.item(0);
		Node settings = findSettings(root);
		return parseSettings(settings);
	}

	private Node findSettings(Node root) {
		NodeList nodeList = root.getChildNodes();
		Node current;
		for (int n = 0; n < nodeList.getLength(); n++) {
			current = nodeList.item(n);
			if (current.getNodeType() == Node.ELEMENT_NODE) {
				if ("settings".equals(current.getNodeName())) {
					return current;
				}
			}
		}
		throw new IllegalArgumentException(EMPTY_GENERATOR_XML);
	}

	private SettingsCollection parseSettings(Node root) {
		SettingsCollection settingsCollection = new SettingsCollection();
		NodeList nodeList = root.getChildNodes();
		Node current;
		for (int n = 0; n < nodeList.getLength(); n++) {
			boolean added = false;
			current = nodeList.item(n);
			if ("setting".equals(current.getNodeName())) {
				Setting setting = new Setting(current);
				NamedNodeMap attrList = current.getAttributes();
				for (int i = 0; i < attrList.getLength(); i++) {
					Node item = attrList.item(i);
					if ("name".equals(item.getNodeName())) {
						setting.setName(item.getNodeValue());
					} else if("type".equals(item.getNodeName())) {
						setting.setType(SettingType.fromString(item.getNodeValue()));
					} else if ("depends".equals(item.getNodeName())) {
						setting.setDependsOn(item.getNodeValue());
					}
					if(setting.getType() == null) {
						setting.setType(SettingType.OPTIONAL);
					} else if (SettingType.SYSTEM == setting.getType()) {
						settingsCollection.addSetting(processSystemSettings(setting.getName(), current));
						added = true;
					}
				}
				if(!added) {
					setting.setAttributes(parseSettingAttributes(current));
					settingsCollection.addSetting(setting);
				}
			}
		}
		if (settingsCollection.size() == 0) {
			throw new IllegalArgumentException(GENERATOR_DOES_NOT_UNDERSTAND_YOU);
		}
		return settingsCollection;
	}

	private Setting processSystemSettings(String name, Node node) {
		List<SettingAttribute> attributes = new ArrayList<>();
		Setting setting = new Setting(node);
		setting.setType(SettingType.SYSTEM);
		setting.setName(name);
		NodeList childNodes = node.getChildNodes();
		for (int n = 0; n < childNodes.getLength(); n++) {
			String str = childNodes.item(n).getTextContent().trim();
			if (!str.isEmpty()) {
				SettingAttribute settingAttribute = new SettingAttribute();
				settingAttribute.setName(str);
				settingAttribute.setType(SettingAttributeType.MIX);
				attributes.add(settingAttribute);
			}
		}
		setting.setAttributes(attributes);
		return setting;
	}

	private List<SettingAttribute> parseSettingAttributes(Node attrNode) {
		List<SettingAttribute> settingAttributes = new ArrayList<>();
		NodeList nodeList = attrNode.getChildNodes();
		Node current;
		for (int n = 0; n < nodeList.getLength(); n++) {
			current = nodeList.item(n);
			if ("attribute".equals(current.getNodeName())) {
				SettingAttribute settingAttribute = new SettingAttribute();
				NamedNodeMap attrList = current.getAttributes();
				for (int i = 0; i < attrList.getLength(); i++) {
					Node item = attrList.item(i);
					if ("name".equals(item.getNodeName())) {
						settingAttribute.setName(item.getNodeValue());
					} else if ("option".equals(item.getNodeName())) {
						settingAttribute.setType(SettingAttributeType.fromName(item.getNodeValue()));
					}
				}
				settingAttribute.setValues(parseSettingAttributeValues(current));
				settingAttributes.add(settingAttribute);
			}
		}
		return settingAttributes;
	}

	private List<AttributeValue> parseSettingAttributeValues(Node valuesNode) {
		List<AttributeValue> values = new ArrayList<>();
		NodeList nodeList = valuesNode.getChildNodes();
		Node current;
		for (int n = 0; n < nodeList.getLength(); n++) {
			current = nodeList.item(n);
			AttributeValue attrValue = null;
			if ("value".equals(current.getNodeName())) {
				attrValue = AttributeValue.of(current.getTextContent().trim());
			}
			NamedNodeMap nodeMap = current.getAttributes();
			if(nodeMap != null && attrValue != null) {
				for (int i = 0; i < nodeMap.getLength(); i++) {
					attrValue.addExtraData(nodeMap.item(i).getNodeName(), nodeMap.item(i).getNodeValue());
				}
			}
			if(attrValue != null) {
				values.add(attrValue);
			}
		}
		return values;
	}

	@Override
	public void initDeviceSpecific(Object... fromRoot) throws ResourceException {
		// this is not required for a win related system
	}

	@Override
	public InputStream getResourceInputStream(Context context, String name) throws ResourceException {
		try {
			return ResourceLoader.RESOURCE_LOADER.getResource(context, name);
		} catch (IOException e) {
			throw new ResourceException(e);
		}
	}

}
