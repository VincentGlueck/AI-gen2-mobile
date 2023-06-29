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
        phraseCount = in.readInt();
        randomCount = in.readInt();
        artistTypeName = in.readString();
    }

    private String description;
    private String preset;
    private boolean random;
    private boolean useCamera;
    private int numOfArtists;
    private int phraseCount;
    private int randomCount = 3;
    private String artistTypeName;
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

    public void setRandom(boolean random) {
        this.random = random;
    }

    public void setUseCamera(boolean useCamera) {
        this.useCamera = useCamera;
    }

    public void setNumOfArtists(int numOfArtists) {
        this.numOfArtists = numOfArtists;
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
    }

    public void setPhraseCount(int phraseCount) {
        this.phraseCount = phraseCount;
    }

    public void setRandomCount(int randomCount) {
        this.randomCount = randomCount;
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
            editor.putString("description", getDescription());
            editor.putString("preset", getPreset());
            editor.putBoolean("random", isRandom());
            editor.putBoolean("useCamera", isUseCamera());
            editor.putInt("numOfArtists", getNumOfArtists());
            editor.putInt("phraseCount", getPhraseCount());
            editor.putInt("randomCount", getRandomCount());
            editor.putString("artistTypeName", getArtistTypeName());
        } finally {
            editor.apply();
        }
    }

    @Override
    public void getFromPreferences(SharedPreferences preferences) {
        setDescription(preferences.getString("description", ""));
        setPreset(preferences.getString("preset", ""));
        setRandom(preferences.getBoolean("random", false));
        setUseCamera(preferences.getBoolean("useCamera", false));
        setNumOfArtists(preferences.getInt("numOfArtists", 3));
        setPhraseCount(preferences.getInt("phraseCount", 1));
        setRandomCount(preferences.getInt("randomCount", 1));
        setArtistTypeName(preferences.getString("artistTypeName", ""));
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
