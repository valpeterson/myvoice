package com.example.android.MyVoice;

import android.media.MediaPlayer;
import android.content.Intent;
import android.os.Bundle;

import com.example.android.common.activities.MVActivityBase;

import android.util.Log;

import java.io.IOException;

/**
 * Created by Val on 1/3/2015.
 */
public class MusicActivity extends MVActivityBase {
    private MediaPlayer mp;
    private int current_song_index;
    public static final String TAG = "MusicActivity";

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: entering");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        play(1);
        Log.i(TAG, "onCreate: exiting");
    }

        /**
         * Receiving song index from playlist view
         * and play the song
         * */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult -- entering");
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            current_song_index = data.getExtras().getInt("songIndex");
            // play selected song
            play(current_song_index);
        }

    }

    public void play(int song_index) {
        Log.i(TAG, "play -- entering");
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource("/sdcard/Music/Mini Mozart/Klassical Kids/01 Overture- The Magic Flute.wma");
            mp.prepare();
            mp.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
