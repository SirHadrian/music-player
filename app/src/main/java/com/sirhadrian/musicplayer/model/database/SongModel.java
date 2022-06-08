package com.sirhadrian.musicplayer.model.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SongModel {

    public SongModel() {
    }

    public SongModel(String title, String songUri, int duration) {
        this.mSongTitle = title;
        this.mSongUri = songUri;
        this.mSongDuration = duration;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "song_title")
    public String mSongTitle;

    @ColumnInfo(name = "song_uri")
    public String mSongUri;

    public int mSongDuration;

    public int get_mSongDuration() {
        return mSongDuration;
    }

    public void set_mSongDuration(int mSongDuration) {
        this.mSongDuration = mSongDuration;
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
