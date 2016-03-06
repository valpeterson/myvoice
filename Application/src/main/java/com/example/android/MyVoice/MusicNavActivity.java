package com.example.android.MyVoice;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.database.Cursor;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.common.activities.MVActivityBase;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.provider.MediaStore.*;
import static android.widget.ImageButton.*;
import static java.lang.String.format;


public class MusicNavActivity extends MVActivityBase implements MediaPlayer.OnCompletionListener {
    private MediaStore store;
    private enum levels {
        MV_MUSIC_TOP_MENU,
        MV_NAV_ALBUM,
        MV_NAV_GENRE,
        MV_NAV_PLAYLISTS,
        MV_NAV_ARTISTS, //not used yet
        MV_NAV_SONGS,
        MV_PLAY_SONG,
        MV_PLAY_ALL_SONGS
    }
    private enum topMenu {
        MV_MENU_ALBUM,
        MV_MENU_PLAYLIST,
        MV_MENU_GENRE,
    }
    private List<String> topMenuTextList = Arrays.asList("Browse Albums", "Browse Playlists", "Browse Genres");
    private List<topMenu> menuList;
    private int menuSelection;
    private levels navLevel;
    private levels prevLevel;
    private Cursor listCursor;
    private Cursor songCursor;
    private TextView txtSelection;
    private ImageButton btnLeft;
    private ImageButton btnCenter;
    private ImageButton btnRight;
    private MediaPlayer mp;
    private int seekBackwardTime = 5000;   //milliseconds
    private int seekForwardTime = 5000;     //milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navLevel = levels.MV_MUSIC_TOP_MENU;
        menuList = Arrays.asList(topMenu.values());
        menuSelection = 0;

        txtSelection = (TextView) findViewById(R.id.selection);
        btnLeft = (ImageButton) findViewById(R.id.btn_left);
        btnCenter = (ImageButton) findViewById(R.id.btn_center);
        btnRight = (ImageButton) findViewById(R.id.btn_right);
        txtSelection.setText(topMenuTextList.get(menuSelection));

        btnCenter.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickCenterButton();
                return true;
            }
        });
        btnLeft.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickLeftButton();
                return true;
            }
        });
        btnRight.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickRightButton();
                return true;
            }
        });
        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        txtSelection.requestFocus();
        announceLevelChange();
    }
*/
    //override this so it doesn't announce our activity name when it's started.  That's annoying and unhelpful.
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add("Select music navigation method");
        event.getText().add(topMenuTextList.get(menuSelection));

        return true;
    }

    private void announceMenuMode() {
        txtSelection.announceForAccessibility("Select music navigation method");
        txtSelection.announceForAccessibility(topMenuTextList.get(menuSelection));
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
    public void getAlbumCursor()
    {
        String where = null;    //could use this to add selection criteria
        ContentResolver cr = getContentResolver();
        final Uri uri = Audio.Albums.EXTERNAL_CONTENT_URI;
        final String _id = Audio.Albums._ID;
        final String album_name = Audio.Albums.ALBUM;
        final String artist = Audio.Albums.ARTIST;
        final String[]columns={_id,album_name, artist};
        listCursor = cr.query(uri,columns,where,null, null);
        listCursor.moveToFirst();
    }

    public void getPlaylistCursor()
    {
        String where = null;    //could use this to add selection criteria
        ContentResolver cr = getContentResolver();
        final Uri uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String _id = Audio.Playlists._ID;
        final String playlist_name = Audio.Playlists.NAME;
        final String[]columns={_id,playlist_name};
        listCursor = cr.query(uri,columns,where,null, null);
        listCursor.moveToFirst();
    }

    public void getGenreCursor()
    {
        String where = null;    //could use this to add selection criteria
        ContentResolver cr = getContentResolver();
        final Uri uri = Audio.Genres.EXTERNAL_CONTENT_URI;
        final String _id = Audio.Genres._ID;
        final String genre_name = Audio.Genres.NAME;
        final String[]columns={_id,genre_name};
        listCursor = cr.query(uri,columns,where,null, null);
        listCursor.moveToFirst();
    }

    public void getAlbumContents() {
        int colIndex = listCursor.getColumnIndex(Audio.Albums.ALBUM);
        String albumName = listCursor.getString(colIndex);
        String[] columns = { MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.MIME_TYPE, };

        String where = android.provider.MediaStore.Audio.Media.ALBUM
                + "=?";
        String whereVal[] = { albumName };
        //String orderBy = android.provider.MediaStore.Audio.Media.TITLE;
        ContentResolver cr = getContentResolver();
        songCursor = cr.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns,
                where, whereVal, null);
        songCursor.moveToFirst();
    }

    public void getPlaylistContents() {
        int colIndex = listCursor.getColumnIndex(Audio.Playlists._ID);
        int listID = listCursor.getInt(colIndex);
        String[] columns = { Audio.Playlists.Members.DATA,
                Audio.Playlists.Members._ID,
                Audio.Playlists.Members.TITLE,
                Audio.Playlists.Members.DISPLAY_NAME,
                Audio.Playlists.Members.MIME_TYPE, };

        ContentResolver cr = getContentResolver();
        songCursor = cr.query(
                Audio.Playlists.Members.getContentUri("external", listID), columns,
                null, null, null);
        songCursor.moveToFirst();
    }

    public void getGenreContents() {
        int colIndex = listCursor.getColumnIndex(Audio.Genres._ID);
        int genreID = listCursor.getInt(colIndex);
        Uri uri = Audio.Genres.Members.getContentUri("external", genreID);
        String[] columns = { MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.MIME_TYPE, };

        //String orderBy = android.provider.MediaStore.Audio.Media.TITLE;
        ContentResolver cr = getContentResolver();
        songCursor = cr.query(
                uri, columns,
                null, null, null);
        songCursor.moveToFirst();
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        switch (navLevel) {
            case MV_PLAY_ALL_SONGS:
                if (songCursor.moveToNext()) {
                    changeSong();
                } else {
                    navLevel = prevLevel;
                    stopMusic();
                    announceLevelChange();
                }
                break;
            case MV_PLAY_SONG:
                navLevel = levels.MV_NAV_SONGS;
                stopMusic();
                announceLevelChange();
        }
    }

    private void setupSeekButtons() {
        btnLeft.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 500);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: //in case user moves pointer off the button before lifting
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Rewinding...");
                    rewind();
                    /*Make sure we're still in one of the playback levels.  If we reached end of
                    song or list then completion handler will move us back to a navigation level and
                    remove the touch listener, but can't remove the handler, so we need to do that here.*/
                    if (navLevel == levels.MV_PLAY_ALL_SONGS || navLevel == levels.MV_PLAY_SONG) {
                        mHandler.postDelayed(this, 500);
                    }
                }
            };
        });
        btnRight.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 500);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: //in case user moves pointer off the button before lifting
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Fast forwarding...");
                    fastForward();
                    /*Make sure we're still in one of the playback levels.  If we reached end of
                    song or list then completion handler will move us back to a navigation level and
                    remove the touch listener, but can't remove the handler, so we need to do that here.*/
                    if (navLevel == levels.MV_PLAY_ALL_SONGS || navLevel == levels.MV_PLAY_SONG) {
                        mHandler.postDelayed(this, 500);
                    }
                }
            };
        });
    }

    private void disableSeekButtons() {
        btnLeft.setOnTouchListener(null);
        btnRight.setOnTouchListener(null);
    }

    private void changeButtonPicsForPlayback() {
        //btnLeft.setImageResource(R.drawable.rewind);
        btnCenter.setImageResource(R.drawable.pause);
        //btnRight.setImageResource(R.drawable.fastforward);
    }

    private void changeButtonPicsForNavigation() {
        //btnLeft.setImageResource(R.drawable.previous);
        btnCenter.setImageResource(R.drawable.select);
        //btnRight.setImageResource(R.drawable.next);
    }

    private void startPlayingMusic() {
        setupSeekButtons();
        changeButtonPicsForPlayback();
        int colIndex = songCursor.getColumnIndex(Audio.Media.DATA);
        String songPath = songCursor.getString(colIndex);
        try {
            mp.reset();
            mp.setDataSource(songPath);
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

    private void changeSong() {
        int colIndex = songCursor.getColumnIndex(Audio.Media.DATA);
        String songPath = songCursor.getString(colIndex);
        if (mp.isPlaying()) mp.stop();
        try {
            mp.reset();
            mp.setDataSource(songPath);
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

    private void stopMusic() {
        mp.stop();
        disableSeekButtons();
        changeButtonPicsForNavigation();
    }

    private void rewind() {
        // get current song position
        int currentPosition = mp.getCurrentPosition();
        int duration = mp.getDuration();
        // check if seekBackward time is greater than 0 sec
        if(currentPosition - seekBackwardTime >= 0){
            // forward song
            mp.seekTo(currentPosition - seekBackwardTime);
        }else{
            // backward to starting position
            mp.seekTo(0);
        }
        float relativePosition = 100*((float)(currentPosition - seekBackwardTime)/(float)duration);
        Log.i(TAG, format("Song position: %.1f%%", relativePosition));
    }

    private void fastForward() {
        // get current song position
        int currentPosition = mp.getCurrentPosition();
        int duration = mp.getDuration();
        // check if seekForward time is lesser than song duration
        if(currentPosition + seekForwardTime <= mp.getDuration()){
            // forward song
            mp.seekTo(currentPosition + seekForwardTime);
        }else{
            // forward to end position
            mp.seekTo(duration);
        }
        float relativePosition = 100*((float)(currentPosition + seekForwardTime)/(float)duration);
        Log.i(TAG, format("Song position: %.1f%%", relativePosition));
    }

    private void announceAlbumSelection() {
        int colIndex = listCursor.getColumnIndex(Audio.Albums.ALBUM);
        String albumName = listCursor.getString(colIndex);
        txtSelection.setText(albumName);
        txtSelection.announceForAccessibility(albumName);
    }

    private void announcePlaylistSelection() {
        int colIndex = listCursor.getColumnIndex(Audio.Playlists.NAME);
        String playlistName = listCursor.getString(colIndex);
        txtSelection.setText(playlistName);
        txtSelection.announceForAccessibility(playlistName);
    }

    private void announceGenreSelection() {
        int colIndex = listCursor.getColumnIndex(Audio.Genres.NAME);
        String genreName = listCursor.getString(colIndex);
        txtSelection.setText(genreName);
        txtSelection.announceForAccessibility(genreName);
    }

    private void announceSongSelection() {
        int colIndex = songCursor.getColumnIndex(Audio.Media.TITLE);
        String songName = songCursor.getString(colIndex);
        txtSelection.setText(songName);
        txtSelection.announceForAccessibility(songName);
    }

    private void announceListSelection(levels selected) {
        switch (selected) {
            case MV_NAV_ALBUM:
                announceAlbumSelection();
                break;
            case MV_NAV_PLAYLISTS:
                announcePlaylistSelection();
                break;
            case MV_NAV_GENRE:
                announceGenreSelection();
                break;
        }
    }
    private void announceLevelChange() {
        switch (navLevel) {
            case MV_MUSIC_TOP_MENU:
                txtSelection.setText(topMenuTextList.get(menuSelection));
                announceMenuMode();
                break;
            case MV_NAV_ALBUM:
                txtSelection.announceForAccessibility("Select album");
                announceAlbumSelection();
                break;
            case MV_NAV_PLAYLISTS:
                txtSelection.announceForAccessibility("Select playlist");
                announcePlaylistSelection();
                break;
            case MV_NAV_GENRE:
                txtSelection.announceForAccessibility("Select genre");
                announceGenreSelection();
                break;
            case MV_NAV_SONGS:
                txtSelection.announceForAccessibility("Select song");
                announceSongSelection();
                break;
            case MV_PLAY_ALL_SONGS:
                txtSelection.announceForAccessibility("Play all songs");
                announceListSelection(prevLevel);
                break;
        }
    }
    public void onClickLeftButton(View view) {
        Log.i(TAG, "left button click");
        switch (navLevel) {
            case MV_MUSIC_TOP_MENU:
                if (menuSelection > 0) {
                    menuSelection--;
                } else {
                    menuSelection = menuList.size() - 1;
                }
                announceMenuSelection();
                break;
            case MV_NAV_ALBUM:
            case MV_NAV_PLAYLISTS:
            case MV_NAV_GENRE:
                if (!listCursor.moveToPrevious()) {
                    //If not successful we tried going past the beginning, so wrap around to the end.
                    listCursor.moveToLast();
                }
                announceListSelection(navLevel);
                break;
            case MV_NAV_SONGS:
                if (!songCursor.moveToPrevious()) {
                    //If not successful we tried going past the beginning, so wrap around to the end.
                    songCursor.moveToLast();
                }
                announceSongSelection();
                break;
            case MV_PLAY_SONG:
            case MV_PLAY_ALL_SONGS:
                if (!songCursor.moveToPrevious()) {
                    //If not successful we tried going past the beginning, so wrap around to the end.
                    songCursor.moveToLast();
                }
                announceSongSelection();
                changeSong();
                break;
        }

    }
    public void onClickRightButton(View view) {
        Log.i(TAG, "right button click");
        switch (navLevel) {
            case MV_MUSIC_TOP_MENU:
                if (menuSelection < menuList.size() - 1) {
                    menuSelection++;
                } else {
                    menuSelection = 0;
                }
                announceMenuSelection();
                break;
            case MV_NAV_ALBUM:
            case MV_NAV_PLAYLISTS:
            case MV_NAV_GENRE:
                if (!listCursor.moveToNext()) {
                    //If not successful we tried going past the end, so wrap around to the beginning.
                    listCursor.moveToFirst();
                }
                announceListSelection(navLevel);
                break;
            case MV_NAV_SONGS:
                if (!songCursor.moveToNext()) {
                    //If not successful we tried going past the end, so wrap around to the beginning.
                    songCursor.moveToFirst();
                }
                announceSongSelection();
                break;
            case MV_PLAY_SONG:
            case MV_PLAY_ALL_SONGS:
                if (!songCursor.moveToNext()) {
                    //If not successful we tried going past the end, so wrap around to the beginning.
                    songCursor.moveToFirst();
                }
                announceSongSelection();
                changeSong();
                break;
        }
    }
    public void onClickCenterButton(View view) {
        Log.i(TAG, "center button click");
        switch (navLevel) {
            case MV_MUSIC_TOP_MENU:
                Log.i(TAG, "Exiting music top menu");
                switch (menuList.get(menuSelection)) {
                    case MV_MENU_ALBUM:
                        navLevel = levels.MV_NAV_ALBUM;
                        getAlbumCursor();
                        break;
                    case MV_MENU_GENRE:
                        navLevel = levels.MV_NAV_GENRE;
                        getGenreCursor();
                        break;
                    case MV_MENU_PLAYLIST:
                        navLevel = levels.MV_NAV_PLAYLISTS;
                        getPlaylistCursor();
                        break;
                }
                announceLevelChange();
                break;
            case MV_NAV_ALBUM:
                Log.i(TAG, "Select song");
                prevLevel = navLevel;   //we use this when moving back up from song selection level
                navLevel = levels.MV_NAV_SONGS;
                getAlbumContents();
                btnCenter.setImageResource(R.drawable.play);
                announceLevelChange();
                break;
            case MV_NAV_PLAYLISTS:
                Log.i(TAG, "Select song");
                prevLevel = navLevel;   //we use this when moving back up from song selection level
                navLevel = levels.MV_NAV_SONGS;
                getPlaylistContents();
                btnCenter.setImageResource(R.drawable.play);
                announceLevelChange();
                break;
            case MV_NAV_GENRE:
                Log.i(TAG, "Select song");
                prevLevel = navLevel;   //we use this when moving back up from song selection level
                navLevel = levels.MV_NAV_SONGS;
                getGenreContents();
                btnCenter.setImageResource(R.drawable.play);
                announceLevelChange();
                break;
            case MV_NAV_SONGS:
                Log.i(TAG, "Play song");
                navLevel = levels.MV_PLAY_SONG;
                startPlayingMusic();
                break;
            case MV_PLAY_SONG:
            case MV_PLAY_ALL_SONGS:
                if (mp.isPlaying()) {
                    Log.i(TAG, "Pause");
                    btnCenter.setImageResource(R.drawable.play);
                    mp.pause();
                } else{
                    Log.i(TAG, "Resume");
                    btnCenter.setImageResource(R.drawable.pause);
                    mp.start();
                }
                break;
        }
    }

    public void longClickCenterButton() {
        Log.i(TAG, "long center button click");
        switch (navLevel) {
            case MV_NAV_ALBUM:
                Log.i(TAG, "Play all songs");
                prevLevel = navLevel;
                navLevel = levels.MV_PLAY_ALL_SONGS;
                getAlbumContents();
                announceLevelChange();
                startPlayingMusic();
                break;
            case MV_NAV_PLAYLISTS:
                Log.i(TAG, "Play all songs");
                prevLevel = navLevel;
                navLevel = levels.MV_PLAY_ALL_SONGS;
                getPlaylistContents();
                announceLevelChange();
                startPlayingMusic();
                break;
            case MV_NAV_GENRE:
                Log.i(TAG, "Play all songs");
                prevLevel = navLevel;
                navLevel = levels.MV_PLAY_ALL_SONGS;
                getGenreContents();
                announceLevelChange();
                startPlayingMusic();
                break;
            case MV_PLAY_SONG:
                Log.i(TAG, "Return to song navigation");
                navLevel = levels.MV_NAV_SONGS;
                stopMusic();
                btnCenter.setImageResource(R.drawable.select);
                announceLevelChange();
                break;
            case MV_PLAY_ALL_SONGS:
                Log.i(TAG, "Return to album, playlist or genre navigation");
                navLevel = prevLevel;
                stopMusic();
                btnCenter.setImageResource(R.drawable.select);
                announceLevelChange();
                break;
        }
    }
    public void longClickLeftButton() {
        Log.i(TAG, "long left button click");
        switch (navLevel) {
            case MV_MUSIC_TOP_MENU:
                Log.i(TAG, "Return to main menu");
                //Intent i = new Intent(this, MainActivity.class);
                //startActivity(i);
                finish();
                break;
            case MV_NAV_ALBUM:
            case MV_NAV_PLAYLISTS:
            case MV_NAV_GENRE:
                Log.i(TAG, "Return to selection of music navigation mode");
                navLevel = levels.MV_MUSIC_TOP_MENU;
                announceLevelChange();
                break;
            case MV_NAV_SONGS:
                Log.i(TAG, "Return to selection of album, genre, or playlist");
                navLevel = prevLevel;
                announceLevelChange();
                break;
            case MV_PLAY_SONG:
                Log.i(TAG, "Rewind?  Should not get here.  Expected onTouch to handle it.");
                break;
        }
    }
    public void longClickRightButton() {
        Log.i(TAG, "long right button click");
        switch (navLevel) {
            case MV_NAV_ALBUM:
            case MV_NAV_PLAYLISTS:
            case MV_NAV_GENRE:
                prevLevel = navLevel;
                Log.i(TAG, "Bookmarks");
                break;
            case MV_PLAY_SONG:
                Log.i(TAG, "Fast forward?  Should not get here.  Expected onTouch to handle it.");
                break;
        }
    }
}
