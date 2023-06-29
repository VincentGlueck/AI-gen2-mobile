package org.ww.ai.parcel;

import android.util.Log;

import org.ww.ai.data.AdditionalSettingsIF;
import org.ww.ai.data.WhatToRenderIF;

public class WhatToRender implements WhatToRenderIF {

    private String description;
    private String preset;
    private boolean random;
    private boolean useCamera;
    private int numOfArtists;
    private int phraseCount;
    private int randomCount;
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
}
