package com.sirhadrian.musicplayer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.sirhadrian.musicplayer.model.database.SongModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaySongs extends Service {
    public final IBinder binder = new LocalBinder();
    MediaPlayer mPlayer;
    //List<SongModel> songs = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);

    }

    public void stop() {
        if (isPlaying()) {
            mPlayer.stop();
        }
    }

    public void start(){
        if (!mPlayer.isPlaying())
            mPlayer.start();
    }

    public void pause(){
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public boolean isPlaying() {
        if (mPlayer == null)
            return false;
        return mPlayer.isPlaying();
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public int getDuration() {
        return 0;
    }

    public void seekTo(int position) {

    }

    public void playSong(Context context, String uri) {
        if (mPlayer == null) initMediaPlayer();
        else mPlayer.reset();

        try {
            mPlayer.setDataSource(this, Uri.parse(uri));
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public PlaySongs getService() {
            return PlaySongs.this;
        }
    }
}