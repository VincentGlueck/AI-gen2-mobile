package org.ww.ai.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.ww.ai.parcel.SharePreferencesIF;

public interface WhatToRenderIF extends Parcelable, SharePreferencesIF {
	String getDescription();
	String getPreset();
	boolean isRandom();
	boolean isUseCamera();

	boolean isUseNoArtists();

	boolean isInstantCopyToClipBoard();
	int getNumOfArtists();
	int getPhraseCount();
	int getRandomCount();
	String getArtistTypeName();
	void readCommand(String[] args) throws Exception;
	AdditionalSettingsIF getAdditionalSettings();

	void setArtistTypeName(String artistTypeName);

	void setPreset(String preset);

	void setNumOfArtists(int num);

	void setDescription(String description);

	void setPhraseCount(int count);

	void setInstantCopyToClipBoard(boolean onOff);

	void setUseNoArtists(boolean useNoArtists);

	void setUseCamera(boolean useCamera);

	void setRandom(boolean random);

	void setRandomCount(int randomCount);
	void setUseResolution(boolean useResolution);

	@Override
	int describeContents();

	@Override
	void writeToParcel(@NonNull Parcel dest, int flags);
}
