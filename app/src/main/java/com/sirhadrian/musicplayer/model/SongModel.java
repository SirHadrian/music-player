package com.sirhadrian.musicplayer.model;

import android.net.Uri;

public class SongModel {

    private String mSongTitle;
    private Uri mSongUri;

    public SongModel() {

    }

    public SongModel(String songTitle, Uri songURI) {
        this.mSongTitle=songTitle;
        this.mSongUri = songURI;
    }

    public String get_mSongTitle() {
        return mSongTitle;
    }

    public void set_mSongTitle(String mSongTitle) {
        this.mSongTitle = mSongTitle;
    }

    public Uri get_mSongUri() {
        return mSongUri;
    }

    public void set_mSongUri(Uri mSongUri) {
        this.mSongUri = mSongUri;
    }
}
