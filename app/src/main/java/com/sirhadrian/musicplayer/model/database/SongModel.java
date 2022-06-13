package com.sirhadrian.musicplayer.model.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SongModel {

    public SongModel() {
    }

    public SongModel(String title, String artist, String songUri, int duration) {
        this.mSongTitle = title;
        this.mSongUri = songUri;
        this.mArtistName = artist;
        this.mSongDuration = duration;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "song_title")
    public String mSongTitle;

    @ColumnInfo(name = "song_uri")
    public String mSongUri;

    @ColumnInfo(name = "song_duration")
    public int mSongDuration;

    @ColumnInfo(name = "song_artist")
    public String mArtistName;

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

    public String get_mArtistName() {
        return mArtistName;
    }

    public void set_mArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
    }
}
