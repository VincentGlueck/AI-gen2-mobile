package org.ww.ai.parcel;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.ww.ai.data.AdditionalSettingsIF;
import org.ww.ai.data.WhatToRenderIF;

public class WhatToRender implements WhatToRenderIF {

    public static final Parcelable.Creator<WhatToRender> CREATOR = new Parcelable.Creator() {
        public WhatToRender createFromParcel(Parcel in) {
            return new WhatToRender(in);
        }

        public WhatToRender[] newArray(int size) {
            return new WhatToRender[size];
        }
    };
    private static final String PREF_DESCRIPTION = "description";
    private static final String PREF_PRESET = "preset";
    private static final String PREF_RANDOM = "random";
    private static final String PREF_USE_CAMERA = "useCamera";
    private static final String PRE_NUM_OF_ARTIST = "numOfArtists";
    private static final String PREF_PHRASE_COUNT = "phraseCount";
    private static final String PREF_RANDOM_COUNT = "randomCount";
    private static final String PREF_ARTIST_TYPE_NAME = "artistTypeName";
    private static final String PREF_USE_NO_ARTIST = "useNoArtists";
    private static final String PREF_USE_RESOLUTION = "useResolution";
    private static final String PREF_INSTANT_COPY_TO_CLIP_BOARD = "instantCopyToClipBoard";

    public WhatToRender() {
    }

    public WhatToRender(Parcel in) {
        String str;
        description = in.readString();
        preset = in.readString();
        str = in.readString();
        random = str != null && Boolean.parseBoolean(str);
        useCamera = str != null && Boolean.parseBoolean(str);
        numOfArtists = in.readInt();
        str = in.readString();
        useNoArtists = str != null && Boolean.parseBoolean(str);
        phraseCount = in.readInt();
        randomCount = in.readInt();
        artistTypeName = in.readString();
    }

    private String description;
    private String preset;
    private boolean random;
    private boolean useCamera;
    private int numOfArtists;

    private boolean useResolution;
    private boolean useNoArtists;
    private int phraseCount;
    private int randomCount = 3;
    private String artistTypeName;

    private boolean instantCopyToClipBoard;
    private AdditionalSettingsIF additionalSettingsIF;
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getPreset() {
        return preset;
    }

    @Override
    public boolean isRandom() {
        return random;
    }

    @Override
    public boolean isUseCamera() {
        return useCamera;
    }

    @Override
    public int getNumOfArtists() {
        return numOfArtists;
    }

    @Override
    public int getPhraseCount() {
        return phraseCount;
    }

    @Override
    public int getRandomCount() {
        return randomCount;
    }

    @Override
    public String getArtistTypeName() {
        return artistTypeName;
    }

    @Override
    public void readCommand(String[] args) throws Exception {
        Log.d("WHATTORENDER", "Method call *not* expected: readCommand(String[] args)");
    }

    @Override
    public AdditionalSettingsIF getAdditionalSettings() {
        return additionalSettingsIF;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    @Override
    public void setRandom(boolean random) {
        this.random = random;
    }

    @Override
    public void setUseCamera(boolean useCamera) {
        this.useCamera = useCamera;
    }

    @Override
    public void setNumOfArtists(int numOfArtists) {
        this.numOfArtists = numOfArtists;
    }

    @Override
    public boolean isUseNoArtists() {
        return useNoArtists;
    }

    @Override
    public void setUseNoArtists(boolean useNoArtists) {
        this.useNoArtists = useNoArtists;
    }

    @Override
    public boolean isInstantCopyToClipBoard() {
        return instantCopyToClipBoard;
    }

    @Override
    public void setInstantCopyToClipBoard(boolean instantCopyToClipBoard) {
        this.instantCopyToClipBoard = instantCopyToClipBoard;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(preset);
        dest.writeString(Boolean.toString(random));
        dest.writeString(Boolean.toString(useCamera));
        dest.writeInt(numOfArtists);
        dest.writeInt(phraseCount);
        dest.writeInt(randomCount);
        dest.writeString(artistTypeName);
        dest.writeString(Boolean.toString(useNoArtists));
        dest.writeString(Boolean.toString(useResolution));
    }

    public void setPhraseCount(int phraseCount) {
        this.phraseCount = phraseCount;
    }

    public void setRandomCount(int randomCount) {
        this.randomCount = randomCount;
    }

    public boolean isUseResolution() {
        return useResolution;
    }
    @Override
    public void setUseResolution(boolean useResolution) {
        this.useResolution = useResolution;
    }

    public void setArtistTypeName(String artistTypeName) {
        this.artistTypeName = artistTypeName;
    }

    public void setAdditionalSettingsIF(AdditionalSettingsIF additionalSettingsIF) {
        this.additionalSettingsIF = additionalSettingsIF;
    }

    @Override
    public void writeToSharedPreferences(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        try {
            editor.putString(PREF_DESCRIPTION, getDescription());
            editor.putString(PREF_PRESET, getPreset());
            editor.putBoolean(PREF_RANDOM, isRandom());
            editor.putBoolean(PREF_USE_CAMERA, isUseCamera());
            editor.putInt(PRE_NUM_OF_ARTIST, getNumOfArtists());
            editor.putInt(PREF_PHRASE_COUNT, getPhraseCount());
            editor.putInt(PREF_RANDOM_COUNT, getRandomCount());
            editor.putString(PREF_ARTIST_TYPE_NAME, getArtistTypeName());
            editor.putBoolean(PREF_USE_NO_ARTIST, isUseNoArtists());
            editor.putBoolean(PREF_USE_RESOLUTION, isUseResolution());
            editor.putBoolean(PREF_INSTANT_COPY_TO_CLIP_BOARD, isInstantCopyToClipBoard());

        } finally {
            editor.apply();
        }
    }

    @Override
    public void getFromPreferences(SharedPreferences preferences) {
        setDescription(preferences.getString(PREF_DESCRIPTION, ""));
        setPreset(preferences.getString(PREF_PRESET, ""));
        setRandom(preferences.getBoolean(PREF_RANDOM, false));
        setUseCamera(preferences.getBoolean(PREF_USE_CAMERA, false));
        setNumOfArtists(preferences.getInt(PRE_NUM_OF_ARTIST, 3));
        setPhraseCount(preferences.getInt(PREF_PHRASE_COUNT, 1));
        setRandomCount(preferences.getInt(PREF_RANDOM_COUNT, 1));
        setArtistTypeName(preferences.getString(PREF_ARTIST_TYPE_NAME, ""));
        setUseNoArtists(preferences.getBoolean(PREF_USE_NO_ARTIST, false));
        setUseResolution(preferences.getBoolean(PREF_USE_RESOLUTION, true));
        setInstantCopyToClipBoard(preferences.getBoolean(PREF_INSTANT_COPY_TO_CLIP_BOARD, false));
    }

    @NonNull
    @Override
    public String toString() {
        return "WhatToRender{" +
                "description='" + description + '\'' +
                ", preset='" + preset + '\'' +
                ", random=" + random +
                ", useCamera=" + useCamera +
                ", numOfArtists=" + numOfArtists +
                ", phraseCount=" + phraseCount +
                ", randomCount=" + randomCount +
                ", artistTypeName='" + artistTypeName + '\'' +
                '}';
    }
}
