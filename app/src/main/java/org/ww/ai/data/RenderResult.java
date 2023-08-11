package org.ww.ai.data;

import androidx.annotation.NonNull;

public class RenderResult {

    private String sentence;
    private int numOfArtists;
    private int numOfRandoms;
    private String preset;
    private String cameraType;
    private int presetWords;
    private String resolution;

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public int getNumOfArtists() {
        return numOfArtists;
    }

    public void setNumOfArtists(int numOfArtists) {
        this.numOfArtists = numOfArtists;
    }

    public int getNumOfRandoms() {
        return numOfRandoms;
    }

    public void setNumOfRandoms(int numOfRandoms) {
        this.numOfRandoms = numOfRandoms;
    }

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public String getCameraType() {
        return cameraType;
    }

    public void setCameraType(String cameraType) {
        this.cameraType = cameraType;
    }

    public int getPresetWords() {
        return presetWords;
    }

    public void setPresetWords(int presetWords) {
        this.presetWords = presetWords;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @NonNull
    @Override
    public String toString() {
        return "RenderResult{" +
                "sentence='" + sentence + '\'' +
                ", numOfArtists=" + numOfArtists +
                ", numOfRandoms=" + numOfRandoms +
                ", preset='" + preset + '\'' +
                ", cameraType='" + cameraType + '\'' +
                ", presetWords=" + presetWords +
                ", resolution='" + resolution + '\'' +
                '}';
    }

    public String toReadableForm() {
        return "What was used for this sentence:\n\n" +
                "preset: " + preset + "\n" +
                "artist count: " + numOfArtists + "\n" +
                "words from preset: " + presetWords + "\n" +
                "random words: " + numOfRandoms + "\n" +
                "camera type: " + (cameraType != null ? cameraType : "<none>") + "\n" +
                "resolution: " + (resolution != null ? resolution : "<none>");
    }

}
