package com.sirhadrian.musicplayer.model.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {
    @Query("SELECT * FROM SongModel")
    List<SongModel> getAll();

    @Query("SELECT * FROM SongModel WHERE uid IN (:userIds)")
    List<SongModel> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM SongModel WHERE song_title LIKE :first AND " +
            "song_uri LIKE :last LIMIT 1")
    SongModel findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SongModel> songs);

    @Delete
    void delete(SongModel song);

}
