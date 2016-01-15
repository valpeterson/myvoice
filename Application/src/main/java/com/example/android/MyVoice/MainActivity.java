/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.MyVoice;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.content.Intent;
import android.util.Log;

import com.example.android.common.activities.MVActivityBase;
// import com.example.android.common.logger.Log; //vbpeters: Online sample used this, but changed to directly use android.util.Log to debug logging problems
//import com.example.android.common.logger.LogWrapper;  //vbpeters: Online sample used this, but changed to directly use android.util.Log to debug logging problems
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * A simple launcher activity containing a summary sample description
 * and a few action bar buttons.
 */
public class MainActivity extends MVActivityBase {

    public static final String TAG = "MainActivity";

    public static final String FRAGTAG = "SuperTabImmersive";
    private enum topMenu {
        MV_MENU_MUSIC_NAV,
        MV_MENU_RESUME_MUSIC,
        MV_MENU_SPEECH_NAV,
        MV_MENU_SETTINGS
    }
    private List<String> topMenuTextList = Arrays.asList("Music Navigation", "Resume Music", "Speech Navigation", "Settings");
    private List<topMenu> menuList;
    private int menuSelection;
    private TextView txtSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: entering");
        setContentView(R.layout.activity_main);
        txtSelection = (TextView) findViewById(R.id.selection);

        menuList = Arrays.asList(topMenu.values());
        menuSelection = 0;
        announceMenuMode();
        announceMenuSelection();

        if (getSupportFragmentManager().findFragmentByTag(FRAGTAG) == null ) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            MyVoice fragment = new MyVoice();
            transaction.add(fragment, FRAGTAG);
            transaction.commit();
        }
      /*  btnMusic = (ImageButton) findViewById(R.id.btn_music);
        btnMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), MusicActivity.class);
                startActivityForResult(i, 100);
                // Changing button image to pause button
                // btnPlay.setImageResource(R.drawable.btn_pause);
            }
        });*/
        Log.i(TAG, "onCreate: exiting");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //vbpeters: uncomment this to get the option menu that allows toggling of immersive mode
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /** Create a chain of targets that will receive log data */
/*vbpeters: commenting out because I want to directly use android.util.Log.  Having problems getting logging to work everywhere with this wrapper.
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        /*vbpeters: commenting out temporarily due to null pointer exception when we call .setNext
        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());
        logFragment.getLogView().setTextAppearance(this, R.style.Log);
        logFragment.getLogView().setBackgroundColor(Color.WHITE);
        */

/*        Log.i(TAG, "Ready");
    }
*/

    private void announceMenuMode() {
        txtSelection.announceForAccessibility("Select from top menu");
    }

    private void announceMenuSelection() {
        txtSelection.setText(topMenuTextList.get(menuSelection));
        if (android.os.Build.VERSION.SDK_INT < 16) {
            //Make TalkBack read the text now (alternatively, in APIs later than 14 we might be able to do this
            //by using methods outlined in http://stackoverflow.com/questions/22046941/send-accessibility-event-not-linked-to-view)
            txtSelection.requestFocus();
            txtSelection.clearFocus();
        } else {
            txtSelection.announceForAccessibility(topMenuTextList.get(menuSelection));
        }
    }

    public void onClickLeftButton(View view) {
        Log.i(TAG, "left button click");
        if (menuSelection > 0) {
            menuSelection--;
        } else {
            menuSelection = menuList.size() - 1;
        }
        announceMenuSelection();
    }
    public void onClickRightButton(View view) {
        Log.i(TAG, "right button click");
        if (menuSelection < menuList.size() - 1) {
            menuSelection++;
        } else {
            menuSelection = 0;
        }
        announceMenuSelection();
    }
    public void onClickCenterButton(View view)
    {
        Log.i(TAG, "center button click");
        switch (menuList.get(menuSelection)) {
            case MV_MENU_MUSIC_NAV:
                Log.i(TAG, "state change: music navigation");
                Intent i = new Intent(this, MusicNavActivity.class);
                startActivity(i);
                break;
            default:
                Log.i(TAG, "state change: not implemented");
        }
    }

    public void play_music(View view) {
        Log.i(TAG, "play_music");
        Intent i = new Intent(this, MusicActivity.class);
        startActivityForResult(i, 100);
    }

    public void createShortCut(){
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcutname));
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.myvoice_icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext(), MainActivity.class));
        sendBroadcast(shortcutintent);
    }
}
