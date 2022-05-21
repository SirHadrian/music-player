package com.sirhadrian.musicplayer.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<String> mDirPath = new MutableLiveData<>();

    public void set_mDirPath(String path){
        mDirPath.setValue(path);
    }

    public LiveData<String> get_mDirPath() {
        return mDirPath;
    }
}
