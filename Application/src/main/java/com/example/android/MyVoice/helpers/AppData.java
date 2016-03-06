package com.example.android.MyVoice.helpers;

/**
 * Singleton class to hold data that we want to share between activities and fragments
 * Created by Val on 2/19/2016.
 */
public class AppData {
    private SpeechContent content;
    public SpeechContent getContent() {return content;}
    public void setContent(SpeechContent newContent) {this.content = newContent;}
    private static AppData ourInstance = new AppData();

    public static AppData getInstance() {
        return ourInstance;
    }

    private AppData() {
    }
}
