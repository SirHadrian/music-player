package com.sirhadrian.musicplayer.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sirhadrian.musicplayer.model.SongModel;

import java.util.List;

public class SongsListViewModel extends ViewModel {

    private MutableLiveData<List<SongModel>> mSongsList = new MutableLiveData<>();

    public LiveData<List<SongModel>> get_mSongsList() {
        return mSongsList;
    }

    public void set_mSongList(List<SongModel> songs){
        mSongsList.setValue(songs);
    }
}
