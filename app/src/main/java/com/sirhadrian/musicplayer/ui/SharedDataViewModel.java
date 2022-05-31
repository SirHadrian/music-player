package com.sirhadrian.musicplayer.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sirhadrian.musicplayer.model.database.SongModel;

public class SharedDataViewModel extends ViewModel {

    private final MutableLiveData<SongModel> mPlayingNow = new MutableLiveData<>();

    public void select(SongModel song){
        mPlayingNow.setValue(song);
    }

    public LiveData<SongModel> getPlayingNow() {
        return mPlayingNow;
    }
}
