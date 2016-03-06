package com.example.android.MyVoice.helpers;

/**
 * Created by Val on 2/12/2016.
 */
/**
 * A class representing some speech to use
 */
public class SpeechItem {
    public String id;
    public String label;
    public String audioFilePath;
    public boolean isCategory;

    public SpeechItem(String id, String label, String path, boolean isCategory) {
        this.id = id;
        this.label = label;
        this.audioFilePath = path;
        this.isCategory = isCategory;
    }

    @Override
    public String toString() {
        return label;
    }
}

