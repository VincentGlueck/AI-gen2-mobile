package org.ww.ai.phrase;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ww.ai.data.AddtionalAttributes;
import org.ww.ai.data.AttributeValue;
import org.ww.ai.data.Setting;
import org.ww.ai.data.SettingAttribute;
import org.ww.ai.data.SettingAttributeType;
import org.ww.ai.data.SettingType;
import org.ww.ai.data.SettingsCollection;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.phrase.PraseGeneratorErrorHandlerIF.Severity;

public class PhraseGenerator {

	// TODO: this might be be a param, but for now...
	private static final int NORMAL_NUM_OF_ARTISTS = 4;
	private final WhatToRenderIF whatToRender;
	private final SettingsCollection settingsCollection;
	private final PraseGeneratorErrorHandlerIF errorHandler;

	public PhraseGenerator(WhatToRenderIF whatToRender, SettingsCollection settingsCollection, PraseGeneratorErrorHandlerIF errorHandler) {
		this.whatToRender = whatToRender;
		this.settingsCollection = settingsCollection;
		this.errorHandler = errorHandler;
	}
	
	public String getAITextAsString() {
		return String.join("\\n", getAITextAsList());
	}
	

	public List<String> getAITextAsList() {
		if(whatToRender == null || settingsCollection == null) {
			throw new RuntimeException("simply don't know what your are planning to do, illegal call with null values...");
		}
		System.out.println(whatToRender);
		int count = whatToRender.getPhraseCount();
		final List<String> result = new ArrayList<>();
		for(int n=0; n<count; n++) {
			final List<AttributeValue> list = getGenerateWordSettings();
			String str = TextUtils.join(", ", list.stream().map(AttributeValue::getValue).collect(Collectors.toList()));
			result.add(whatToRender.getDescription().trim() + ", " + str);
		}
		return result;
	}

	private List<AttributeValue> getGenerateWordSettings() {
		boolean presetFound = false;
		boolean presetWanted = whatToRender.getPreset() != null && !whatToRender.getPreset().isEmpty();
		final List<AttributeValue> result = new ArrayList<>();
		for(Setting setting : settingsCollection.getAll()) {
			if(presetWanted && SettingType.PRESET == setting.getType()) {
				if(!setting.getName().equalsIgnoreCase(whatToRender.getPreset())) {
					continue;
				}
				List<AttributeValue> presetWords = getRandomWords(setting);
				result.addAll(presetWords);
				presetFound = true;
			} 
			AddtionalAttributes attr = getAttributeFromSetting(setting);
			if(attr == null) {
				continue;
			}
			if(AddtionalAttributes.ARTISTS == attr) {
				int numOfArtists = whatToRender.getNumOfArtists();
				if (numOfArtists > NORMAL_NUM_OF_ARTISTS) {
					numOfArtists = NORMAL_NUM_OF_ARTISTS;
				}
				result.addAll(getArtistsWords(setting, numOfArtists, whatToRender.getArtistTypeName()));
			} else if (AddtionalAttributes.CAMERA == attr && whatToRender.isUseCamera()) {
				 result.add(getCamera(setting));
			} else if (AddtionalAttributes.RESOLUTION == attr && whatToRender.isUseCamera()) {
				result.addAll(getResolution(setting));
			} else if (AddtionalAttributes.RANDOM == attr) {
				result.addAll(getRandomWords(setting));
			}
		}
		if(!presetFound && presetWanted) {
			System.out.println(">>> WARN: preset '" + whatToRender.getPreset() + "' is unknown.");
		}
		return result;
	}

	private AddtionalAttributes getAttributeFromSetting(Setting setting) {
		if(setting == null) {
			errorHandler.handleGeneratorError(new PhraseGeneratorException("setting is null: " + whatToRender.getPreset()), Severity.ERROR);
		} else if (setting.getName() == null || setting.getName().isEmpty()) {
			errorHandler.handleGeneratorError(new PhraseGeneratorException("setting without name used!"), Severity.ERROR);
		}
		assert setting != null;
		return AddtionalAttributes.fromName(setting.getName());
	}

	private List<AttributeValue> getArtistsWords(Setting setting, int numOfArtists, String artistTypeName) {
		List<SettingAttribute> attributes = setting.getAttributes();
		List<AttributeValue> result = Collections.emptyList();
		List<SettingAttribute> filteredList = new ArrayList<>();
		if(artistTypeName != null && !artistTypeName.isEmpty()) {
			List<String> filter = Stream.of(artistTypeName.split(",")).map(a -> a.trim()).collect(Collectors.toList());
			for(SettingAttribute attr : attributes) {
				for(AttributeValue attributeValue : attr.getValues()) {
					String artistType = attributeValue.getExtraData().getOrDefault("artisttype", "");
					if(filter.stream().anyMatch(f -> {
						assert artistType != null;
						return artistType.equalsIgnoreCase(f);
					})) {
						filteredList.add(attr);
					}
				}
			}
			if(filteredList.isEmpty()) {
				System.out.println("Unable to find any artist matching artisttype '" + artistTypeName + "'");
			} else {
				attributes.clear();
				attributes.addAll(filteredList);
			}
		}
		for(SettingAttribute attr : attributes) {
			result = reduceToMaxEntriesRandom(attr.getValues(), numOfArtists);
		}
		return result;
	}
	
	private AttributeValue getCamera(Setting setting) {
		List<SettingAttribute> attributes = setting.getAttributes();
		List<AttributeValue> result = new ArrayList<>();
		for(SettingAttribute attr : attributes) {
			result.addAll(attr.getValues());
			List<AttributeValue> values = attr.getValues();
			result.addAll(values);
		}
		Collections.shuffle(result);
		return result.isEmpty() ? AttributeValue.of("") : (AttributeValue.of(result.get(0).getValue() + " lens")) ;
	}

	private List<AttributeValue> getResolution(Setting setting) {
		List<AttributeValue> uniques = new ArrayList<>();
		List<AttributeValue> multiples = new ArrayList<>();
		List<SettingAttribute> attributes = setting.getAttributes();
		List<AttributeValue> result = new ArrayList<>();
		for(SettingAttribute attr : attributes) {
			if(SettingAttributeType.UNIQUE == attr.getType()) {
				uniques.add(getSingleRandomValue(attr.getValues()));
			} else {
				multiples.addAll(reduceToMaxEntriesRandom(attr.getValues()));
			}
		}
		result.addAll(uniques);
		result.addAll(multiples);
		return result;
	}
	
	private AttributeValue getSingleRandomValue(List<AttributeValue> values) {
		List<AttributeValue> result = new ArrayList<>(values);
		Collections.shuffle(result);
		return result.get(0);
	}

	private List<AttributeValue> getRandomWords(Setting setting) {
		List<AttributeValue> values = new ArrayList<>();
		List<SettingAttribute> attributes = setting.getAttributes();
		for(SettingAttribute attr : attributes) {
			values.addAll(attr.getValues());
		}
		return values.size() > 0 ? reduceToMaxEntriesRandom(values) : Collections.emptyList();
	}
	
	private List<AttributeValue> reduceToMaxEntriesRandom(List<AttributeValue> original, int... maxEntries) {
		List<AttributeValue> result = new ArrayList<>(original);
		Random random = new Random();
		Collections.shuffle(result);
		int count = maxEntries.length > 1 ? maxEntries[0] : random.nextInt(original.size());
		do {
			result.remove(0);
		} while (result.size() > count);
		return result;
	}
	

	@NonNull
	@Override
	public String toString() {
		return "what: " + whatToRender + "\n" + "settings: " + settingsCollection;
	}
	
}
