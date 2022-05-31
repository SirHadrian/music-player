package com.sirhadrian.musicplayer.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sirhadrian.musicplayer.model.database.SongModel;

import java.util.List;

public class SongsListViewModel extends ViewModel {

    private final MutableLiveData<List<SongModel>> mSongsList = new MutableLiveData<>();

    public LiveData<List<SongModel>> get_mSongsList() {
        return mSongsList;
    }

    private void loadSongs(List<SongModel> songs) {
        mSongsList.setValue(songs);

    }

    public void set_mSongList(List<SongModel> songs) {
        mSongsList.setValue(songs);
    }

    public void set_value_in_worker_thread(List<SongModel> songs) {
        mSongsList.postValue(songs);
    }
}
