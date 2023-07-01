package org.ww.ai.parcel;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

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
    private static final String PRE_NUM_OF_ARTIST = "numOfArtists";
    private static final String PREF_PHRASE_COUNT = "phraseCount";
    private static final String PREF_RANDOM_COUNT = "randomCount";
    private static final String PREF_ARTIST_TYPE_NAME = "artistTypeName";
    private static final String PREF_USE_NO_ARTIST = "useNoArtists";
    private static final String PREF_INSTANT_COPY_TO_CLIP_BOARD = "instantCopyToClipBoard";
    private static final String PREF_CAMERA = "camera";
    private static final String PREF_RANDOM_CAMERA = "useRandomCamera";
    private static final String PREF_RANDOM_RESOLUTION = "useRandomResolution";
    private static final String PREF_RESOLUTION = "resolution";

    public WhatToRender() {
    }

    public WhatToRender(Parcel in) {
        String str;
        description = in.readString();
        preset = in.readString();
        str = in.readString();
        random = str != null && Boolean.parseBoolean(str);
        randomCamera = str != null && Boolean.parseBoolean(str);
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
    private boolean randomCamera;
    private int numOfArtists;

    private boolean randomResolution;
    private boolean useNoArtists;
    private int phraseCount;
    private int randomCount;
    private String artistTypeName;
    private String camera;

    private String resolution;

    private boolean instantCopyToClipBoard;
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
    public boolean isRandomCamera() {
        return randomCamera;
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
    public void setRandomCamera(boolean randomCamera) {
        this.randomCamera = randomCamera;
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
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @Override
    public String getResolution() {
        return resolution;
    }

    @Override
    public String getCamera() {
        return camera;
    }

    @Override
    public void setCamera(String camera) {
        this.camera = camera;
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
        dest.writeString(Boolean.toString(randomCamera));
        dest.writeInt(numOfArtists);
        dest.writeInt(phraseCount);
        dest.writeInt(randomCount);
        dest.writeString(artistTypeName);
        dest.writeString(Boolean.toString(useNoArtists));
        dest.writeString(Boolean.toString(randomResolution));
        dest.writeString(camera);
    }

    public void setPhraseCount(int phraseCount) {
        this.phraseCount = phraseCount;
    }

    public void setRandomCount(int randomCount) {
        this.randomCount = randomCount;
    }

    public boolean isRandomResolution() {
        return randomResolution;
    }
    @Override
    public void setRandomResolution(boolean randomResolution) {
        this.randomResolution = randomResolution;
    }

    public void setArtistTypeName(String artistTypeName) {
        this.artistTypeName = artistTypeName;
    }

    @Override
    public void writeToSharedPreferences(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        try {
            editor.putString(PREF_DESCRIPTION, getDescription());
            editor.putString(PREF_PRESET, getPreset());
            editor.putBoolean(PREF_RANDOM, isRandom());
            editor.putInt(PRE_NUM_OF_ARTIST, getNumOfArtists());
            editor.putInt(PREF_PHRASE_COUNT, getPhraseCount());
            editor.putInt(PREF_RANDOM_COUNT, getRandomCount());
            editor.putString(PREF_ARTIST_TYPE_NAME, getArtistTypeName());
            editor.putBoolean(PREF_USE_NO_ARTIST, isUseNoArtists());
            editor.putBoolean(PREF_INSTANT_COPY_TO_CLIP_BOARD, isInstantCopyToClipBoard());
            editor.putString(PREF_CAMERA, getCamera());
            editor.putBoolean(PREF_RANDOM_CAMERA, isRandomCamera());
            editor.putBoolean(PREF_RANDOM_RESOLUTION, isRandomResolution());
            editor.putString(PREF_RESOLUTION, getResolution());
        } finally {
            editor.apply();
        }
    }

    @Override
    public void getFromPreferences(SharedPreferences preferences) {
        setDescription(preferences.getString(PREF_DESCRIPTION, ""));
        setPreset(preferences.getString(PREF_PRESET, ""));
        setRandom(preferences.getBoolean(PREF_RANDOM, false));
        setNumOfArtists(preferences.getInt(PRE_NUM_OF_ARTIST, 3));
        setPhraseCount(preferences.getInt(PREF_PHRASE_COUNT, 1));
        setRandomCount(preferences.getInt(PREF_RANDOM_COUNT, 50));
        setArtistTypeName(preferences.getString(PREF_ARTIST_TYPE_NAME, ""));
        setUseNoArtists(preferences.getBoolean(PREF_USE_NO_ARTIST, false));
        setInstantCopyToClipBoard(preferences.getBoolean(PREF_INSTANT_COPY_TO_CLIP_BOARD, false));
        setCamera(preferences.getString(PREF_CAMERA, ""));
        setRandomCamera(preferences.getBoolean(PREF_RANDOM_CAMERA, false));
        setResolution(preferences.getString(PREF_RESOLUTION, ""));
        setRandomResolution(preferences.getBoolean(PREF_RANDOM_RESOLUTION, false));
    }

    @NonNull

    @Override
    public String toString() {
        return "WhatToRender{" +
                "description='" + description + '\'' +
                ", preset='" + preset + '\'' +
                ", random=" + random +
                ", randomCamera=" + randomCamera +
                ", numOfArtists=" + numOfArtists +
                ", useResolution=" + randomResolution +
                ", useNoArtists=" + useNoArtists +
                ", phraseCount=" + phraseCount +
                ", randomCount=" + randomCount +
                ", artistTypeName='" + artistTypeName + '\'' +
                ", camera='" + camera + '\'' +
                ", instantCopyToClipBoard=" + instantCopyToClipBoard +
                '}';
    }
}
