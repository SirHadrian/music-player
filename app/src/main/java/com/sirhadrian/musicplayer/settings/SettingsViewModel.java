package com.sirhadrian.musicplayer.settings;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Uri> mDirPath = new MutableLiveData<>();

    public void set_mDirPath(Uri path) {
        mDirPath.setValue(path);
    }

    public LiveData<Uri> get_mDirPath() {
        return mDirPath;
    }
}
