package com.sirhadrian.musicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

public class PlaySongsService extends Service {
    // Interface to interact with the service public methods
    public final IBinder binder = new PlaySongBinder();
    // Player instance;
    private MediaPlayer mPlayer;

    // Binder to allow access to the public methods of this service
    public class PlaySongBinder extends Binder {
        public PlaySongsService getService() {
            return PlaySongsService.this;
        }
    }

    // Creates one instance of the player object
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

    // region Lifecycle Methods
    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
    }

    // Code to execute when the service is started as a foreground service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    // When bindService is called one instance of binder is created and distributed to all clients
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    // Cleanup when there is no longer any client bound to the service
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    // Cleanup when the service is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean player
        //this.release();

        // Kill the service
        //stopForeground(true);
        //stopSelf();
    }
    // endregion

    // region Controlling Interface for bound client
    public void start() {
        if (!mPlayer.isPlaying())
            mPlayer.start();
    }

    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void stop() {
        if (isPlaying()) {
            mPlayer.stop();
        }
    }

    public boolean isPlaying() {
        if (!isPlayerAlive())
            return false;
        return mPlayer.isPlaying();
    }

    public int getDuration() {
        if (isPlayerAlive()) return mPlayer.getDuration();
        return 0;
    }

    public int getCurrentPosition() {
        if (isPlayerAlive()) return mPlayer.getCurrentPosition();
        return 0;
    }

    // Loading song from resource and play
    public void playSong(String uri, boolean playNow) {
        if (!isPlayerAlive()) initMediaPlayer();
        else mPlayer.reset();

        try {
            mPlayer.setDataSource(this, Uri.parse(uri));
            mPlayer.prepare();

            if (playNow) {
                mPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Change song play position
    public void seekTo(int position) {
        if (isPlayerAlive()) mPlayer.seekTo(position);
    }
    // endregion

    // region Inner player controls
    private boolean isPlayerAlive() {
        return mPlayer != null;
    }

    // Clean media player
    private void release() {
        if (isPlayerAlive()) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    // Bound activity supplies this listener in order to change to the next song on completion
    public void setCompletionListener(MediaPlayer.OnCompletionListener listener) {
        if (isPlayerAlive()) mPlayer.setOnCompletionListener(listener);
    }
    // endregion
}