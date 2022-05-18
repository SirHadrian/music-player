package com.sirhadrian.musicplayer.model;

public class SongModel {

    private String mSongTitle, mSongUri;

    public SongModel() {

    }

    public SongModel(String songTitle, String songURI) {
        this.mSongTitle=songTitle;
        this.mSongUri = songURI;
    }

    public String get_mSongTitle() {
        return mSongTitle;
    }

    public void set_mSongTitle(String mSongTitle) {
        this.mSongTitle = mSongTitle;
    }

    public String get_mSongUri() {
        return mSongUri;
    }

    public void set_mSongUri(String mSongUri) {
        this.mSongUri = mSongUri;
    }
}
