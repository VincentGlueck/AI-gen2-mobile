package org.ww.ai.data;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SettingsCollection implements AdditionalSettingsIF, Serializable {

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

	public List<Setting> getOthers() {
		return settings.stream().filter(setting -> SettingType.PRESET != setting.getType()).collect(Collectors.toList());
	}

	public String getAllPresetNames() {
		final StringBuilder sb = new StringBuilder();
		getPresets().forEach(p -> sb.append("\n").append(p.getName()));
		return sb.toString();
	}
	
	@Override
	public Map<String, String> getAdditonalSettingsMap() {
		return additionalSettings;
	}

	@Override
	public void addAdditionalSetting(String key, String value) {
		additionalSettings.put(key,  value);
	}

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
