package org.ww.ai.data;

import static java.util.stream.Collectors.toSet;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettingsCollection implements Serializable {

	private final List<Setting> settings = new ArrayList<>();
	private final Map<String, String> additionalSettings = new HashMap<>();

	public void addSetting(Setting setting) {
		if(hasSetting(setting.getName())) {
			settings.removeIf(s -> s.getName().equals(setting.getName()));
		}
		settings.add(setting);
	}
	
	public Setting getSetting(String name) {
		Optional<Setting> optional = settings.parallelStream().filter(setting -> name.equals(setting.getName())).findFirst();
		return optional.orElse(null);
	}
	
	public boolean hasSetting(String name) {
		return settings.parallelStream().anyMatch(setting -> name.equals(setting.getName()));
	}
	
	public int size() {
		return settings.size();
	}
	
	public List<Setting> getAll() {
		return new ArrayList<>(settings);
	}
	
	public List<Setting> getPresets() {
		return settings.stream().filter(setting -> SettingType.PRESET == setting.getType()).collect(Collectors.toList());
	}

	public List<AttributeValue> getAttributesMatchingExtraData(String key, String extraDataStr, Setting setting) {
		List<AttributeValue> attributeValues = setting.getAttributes().stream().flatMap(a -> a.getValues().stream()).collect(Collectors.toList());
		List<AttributeValue> filteredList = new ArrayList<>();
		List<String> filter = Stream.of(extraDataStr.split(",")).map(String::trim).collect(Collectors.toList());
		for(AttributeValue attributeValue : attributeValues) {
			String artistType = attributeValue.getExtraData().getOrDefault(key, "");
			assert artistType != null;
			List<String> split = Arrays.asList(artistType.split(","));
			if(split.stream().anyMatch(new HashSet<>(filter)::contains)) {
				filteredList.add(attributeValue);
			}
		}
		return filteredList;
	}

	@NonNull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(settings.isEmpty()) {
			sb.append("setting are empty\n");
		} else {
			sb.append("Setting in this collection:\n");
			settings.forEach(s -> sb.append(s.toString()).append("\n"));
		}
		if(additionalSettings.isEmpty()) {
			sb.append("no addtional settings\n");
		}
		return sb.toString();
	}

}
