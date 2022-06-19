package com.sirhadrian.musicplayer.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sirhadrian.musicplayer.model.database.SongModel;

import java.util.ArrayList;

public class SharedDataViewModel extends ViewModel {

    // Current playlist
    private final MutableLiveData<ArrayList<SongModel>> mSongsList = new MutableLiveData<>();
    public LiveData<ArrayList<SongModel>> get_mSongsList() {
        return mSongsList;
    }
    public void loadSongs(ArrayList<SongModel> songs) {
        mSongsList.setValue(songs);
    }
    // For use with threads only
    public void set_value_in_worker_thread(ArrayList<SongModel> songs) {
        mSongsList.postValue(songs);
    }

    // Current playing song index
    private final MutableLiveData<Integer> mPlayingNowIndex = new MutableLiveData<>();
    public LiveData<Integer> get_mPlayingNowIndex(){return mPlayingNowIndex;}
    public void set_mPlayingNowIndex(Integer i){mPlayingNowIndex.setValue(i);}
}
