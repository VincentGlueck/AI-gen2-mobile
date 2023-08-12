package org.ww.ai.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.ww.ai.parcel.SharePreferencesIF;

public interface WhatToRenderIF extends Parcelable, SharePreferencesIF {
	String getDescription();
	String getPreset();
	boolean isRandom();
	boolean isRandomCamera();
	String getCamera();
	boolean isUseNoArtists();

	boolean isInstantCopyToClipBoard();
	int getNumOfArtists();
	int getPhraseCount();

	String getResolution();

	boolean isRandomResolution();

	void setResolution(String resolution);

	int getRandomCount();
	String getArtistTypeName();
	void readCommand(String[] args);
	void setArtistTypeName(String artistTypeName);

	void setPreset(String preset);

	void setNumOfArtists(int num);

	void setDescription(String description);

	void setPhraseCount(int count);

	void setInstantCopyToClipBoard(boolean onOff);

	void setUseNoArtists(boolean useNoArtists);

	void setRandomCamera(boolean randomCamera);

	void setRandom(boolean random);

	void setRandomCount(int randomCount);
	void setRandomResolution(boolean randomResolution);

	void setCamera(String camera);

	void setQueryUsed(String queryUsed);

	String getQueryUsed();

	@Override
	int describeContents();

	@Override
	void writeToParcel(@NonNull Parcel dest, int flags);

	String getTranslateToEnglishDescription();

	void setTranslateToEnglishDescription(String englishDescription);

}
