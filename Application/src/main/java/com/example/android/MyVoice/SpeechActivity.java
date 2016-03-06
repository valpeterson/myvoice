package com.example.android.MyVoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;

import android.util.Log;
import android.widget.Toast;

import com.example.android.MyVoice.helpers.SpeechFiler;


public class SpeechActivity extends Activity {

    public static final String TAG = "SpeechActivity";
    private SpeechFiler filer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        try {
            filer = new SpeechFiler();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage() + ".  Returning to top menu.",
                    Toast.LENGTH_LONG).show();
            finish();   //return to main activity
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speech, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
