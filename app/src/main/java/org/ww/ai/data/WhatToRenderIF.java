package org.ww.ai.data;

public interface WhatToRenderIF {
	String getDescription();
	String getPreset();
	boolean isRandom();
	boolean isUseCamera();
	int getNumOfArtists();
	int getPhraseCount();
	int getRandomCount();
	String getArtistTypeName();
	void readCommand(String[] args) throws Exception;
	AdditionalSettingsIF getAdditionalSettings();

	void setArtistTypeName(String artistTypeName);

	void setPreset(String preset);

}
