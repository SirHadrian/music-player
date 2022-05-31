package com.sirhadrian.musicplayer.model.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SongModel {

    public SongModel(){}

    public SongModel(String title, String songUri) {
        this.mSongTitle = title;
        this.mSongUri = songUri;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "song_title")
    public String mSongTitle;

    @ColumnInfo(name = "song_uri")
    public String mSongUri;

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
