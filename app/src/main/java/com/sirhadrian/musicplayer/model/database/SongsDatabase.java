package com.sirhadrian.musicplayer.model.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SongModel.class}, version = 1)
public abstract class SongsDatabase extends RoomDatabase {
    public abstract SongDao songDao();
}
