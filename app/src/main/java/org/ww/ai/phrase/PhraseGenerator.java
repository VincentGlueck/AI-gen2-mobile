package org.ww.ai.phrase;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ww.ai.data.AddtionalAttributes;
import org.ww.ai.data.AttributeValue;
import org.ww.ai.data.RenderResult;
import org.ww.ai.data.Setting;
import org.ww.ai.data.SettingAttribute;
import org.ww.ai.data.SettingAttributeType;
import org.ww.ai.data.SettingType;
import org.ww.ai.data.SettingsCollection;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.phrase.PhraseGeneratorErrorHandlerIF.Severity;

public class PhraseGenerator {

	// TODO: this might be be a param, but for now...
	private static final int MAX_NUM_OF_ARTISTS = 3;
	private final WhatToRenderIF whatToRender;
	private final SettingsCollection settingsCollection;
	private final PhraseGeneratorErrorHandlerIF errorHandler;

	public PhraseGenerator(WhatToRenderIF whatToRender, SettingsCollection settingsCollection,
						   PhraseGeneratorErrorHandlerIF errorHandler) {
		this.whatToRender = whatToRender;
		this.settingsCollection = settingsCollection;
		this.errorHandler = errorHandler;
	}

	public List<RenderResult> getAITextsAsRenderResultList() {
		if(whatToRender == null || settingsCollection == null) {
			throw new RuntimeException("simply don't know what your are planning to do, illegal call with null values...");
		}
		int count = whatToRender.getPhraseCount();
		final List<RenderResult> result = new ArrayList<>();
		for(int n=0; n<count; n++) {
			RenderResult renderResult = new RenderResult();
			final List<AttributeValue> list = getGenerateWordSettings(renderResult);
			String str = TextUtils.join(", ", list.stream().map(AttributeValue::getValue).collect(Collectors.toList()));
			renderResult.setSentence(whatToRender.getTranslateToEnglishDescription() + ", " + str);
			result.add(renderResult);
		}
		return result;
	}

	private List<AttributeValue> getGenerateWordSettings(RenderResult renderResult) {
		boolean presetWanted = whatToRender.getPreset() != null && !whatToRender.getPreset().isEmpty();
		final List<AttributeValue> result = new ArrayList<>();
		for(Setting setting : settingsCollection.getAll()) {
			if(presetWanted && SettingType.PRESET == setting.getType()) {
				if(!setting.getName().equalsIgnoreCase(whatToRender.getPreset())) {
					continue;
				}
				List<AttributeValue> presetWords = getRandomWords(setting);
				renderResult.setPresetWords(presetWords.size());
				renderResult.setPreset(setting.getName());
				result.addAll(presetWords);
			}
			AddtionalAttributes attr = getAttributeFromSetting(setting);
			if(attr == null) {
				continue;
			}
			if(AddtionalAttributes.ARTISTS == attr) {
				processArtists(renderResult, result, setting);
			} else if (AddtionalAttributes.CAMERA == attr) {
				processCamera(renderResult, result, setting);
			} else if (AddtionalAttributes.RESOLUTION == attr) {
				if(whatToRender.isRandomResolution()) {
					processRandomResolution(renderResult, result, setting);
				} else if (whatToRender.getCamera() != null && !whatToRender.getCamera().isEmpty()) {
					AttributeValue resolution = new AttributeValue(whatToRender.getResolution());
					result.add(resolution);
					renderResult.setResolution(resolution.getValue());
				}
			} else if (AddtionalAttributes.RANDOM == attr) {
				result.addAll(getRandomAttributes(renderResult, setting));
			}
		}
		return result;
	}

	private void processRandomResolution(RenderResult renderResult, List<AttributeValue> result, Setting setting) {
		List<AttributeValue> resolutions = getResolution(setting);
		Collections.shuffle(resolutions);
		result.add(resolutions.get(0));
		renderResult.setResolution(resolutions.get(0).getValue());
	}

	private void processCamera(RenderResult renderResult, List<AttributeValue> result, Setting setting) {
		AttributeValue camera = getAttributeValueCamera(setting);
		if(!camera.getValue().isEmpty()) {
			camera.setValue(camera.getValue());
		}
		result.add(camera);
		renderResult.setCameraType(camera.getValue());
	}

	private void processArtists(RenderResult renderResult, List<AttributeValue> result, Setting setting) {
		int numOfArtists = whatToRender.getNumOfArtists();
		if (numOfArtists > MAX_NUM_OF_ARTISTS) {
			numOfArtists = MAX_NUM_OF_ARTISTS;
		}
		List<AttributeValue> list = getArtistsWords(setting, numOfArtists, whatToRender.getArtistTypeName());
		result.addAll(list);
		renderResult.setNumOfArtists(list.size());
	}

	private AttributeValue getAttributeValueCamera(Setting setting) {
		AttributeValue camera;
		if (!whatToRender.isRandomCamera()) {
			camera = new AttributeValue(whatToRender.getCamera());
		} else {
			camera = getSingleRandomValue(setting.getAttributes().stream()
					.flatMap(a -> a.getValues().stream()).collect(Collectors.toList()));
		}
		return camera;
	}

	@NonNull
	private List<AttributeValue> getRandomAttributes(RenderResult renderResult, Setting setting) {
		List<AttributeValue> attributeValues = setting.getAttributes().stream().flatMap(a -> a.getValues().stream()).collect(Collectors.toList());
		int limitTo = getLimitToForRandoms(attributeValues.size(), whatToRender.getRandomCount());
		while (attributeValues.size() > limitTo) {
			Collections.shuffle(attributeValues);
			attributeValues.remove(0);
		}
		renderResult.setNumOfRandoms(attributeValues.size());
		return attributeValues;
	}

	private int getLimitToForRandoms(int size, int randomPercent) {
		if(randomPercent == 0) {
			return 0;
		}
		int limitTo = randomPercent * size / 100;
		return limitTo > 0 ? limitTo : 1;
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
		List<AttributeValue> attributeValues = setting.getAttributes().stream().flatMap(a -> a.getValues().stream()).collect(Collectors.toList());
		if(artistTypeName != null && !artistTypeName.isEmpty()) {
			List<AttributeValue> filteredList = getArtistsForType(artistTypeName, attributeValues);
			if(filteredList.isEmpty()) {
				Log.d("GENERATOR", "Unable to find any artist matching artisttype '" + artistTypeName + "'");
			} else {
				attributeValues.clear();
				attributeValues.addAll(filteredList);
			}
		}
		return reduceToMaxEntriesRandom(attributeValues, numOfArtists);
	}

	private List<AttributeValue> getArtistsForType(String artistTypeName, List<AttributeValue> attributeValues) {
		List<AttributeValue> filteredList = new ArrayList<>();
		List<String> filter = Stream.of(artistTypeName.split(",")).map(String::trim).collect(Collectors.toList());
		for(AttributeValue attributeValue : attributeValues) {
			String artistType = attributeValue.getExtraData().getOrDefault("artisttype", "");
			if(filter.contains(artistType)) {
				filteredList.add(attributeValue);
			}
		}
		return filteredList;
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

	private List<AttributeValue> getRandomWords(Setting setting, int... limitTo) {
		List<AttributeValue> values = new ArrayList<>();
		List<SettingAttribute> attributes = setting.getAttributes();
		for(SettingAttribute attr : attributes) {
			values.addAll(attr.getValues());
		}
		return values.size() > 0 ? (limitTo.length > 0 ? reduceToMaxEntriesRandom(values, limitTo[0])
				: reduceToMaxEntriesRandom(values)) : Collections.emptyList();
	}
	
	private List<AttributeValue> reduceToMaxEntriesRandom(List<AttributeValue> original, int... maxEntries) {
		List<AttributeValue> result = new ArrayList<>(original);
		if(original.size() > 0) {
			int count = maxEntries.length == 1 ? maxEntries[0] :
					ThreadLocalRandom.current().nextInt(1, original.size());
			while (result.size() > count) {
				Collections.shuffle(result);
				result.remove(0);
			}
		}
		return result;
	}
	

	@NonNull
	@Override
	public String toString() {
		return "what: " + whatToRender + "\n" + "settings: " + settingsCollection;
	}
	
}
