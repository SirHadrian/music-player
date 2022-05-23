package com.sirhadrian.musicplayer.model;

import android.net.Uri;

public class Song {

    private final Uri uri;
    private final String name;

    public Song(Uri uri, String name) {
        this.uri = uri;
        this.name = name;
    }

}
