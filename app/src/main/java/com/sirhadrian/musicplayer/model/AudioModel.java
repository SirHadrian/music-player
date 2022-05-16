package com.sirhadrian.musicplayer.model;

public class AudioModel {
    String mPath;
    String mName;
    String mAlbum;
    String mArtist;

    public String get_mPath() {
        return mPath;
    }

    public void set_mPath(String path) {
        this.mPath = path;
    }

    public String get_mName() {
        return mName;
    }

    public void set_mName(String name) {
        this.mName = name;
    }

    public String get_mAlbum() {
        return mAlbum;
    }

    public void set_mAlbum(String album) {
        this.mAlbum = album;
    }

    public String get_mArtist() {
        return mArtist;
    }

    public void set_mArtist(String artist) {
        this.mArtist = artist;
    }
}
