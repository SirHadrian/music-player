package com.sirhadrian.musicplayer.settings;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<String> mDirPath = new MutableLiveData<>();
    private final MutableLiveData<DocumentFile[]> listOfFileInDir = new MutableLiveData<>();

    public MutableLiveData<DocumentFile[]> get_ListOfFileInDir() {
        return listOfFileInDir;
    }

    public MutableLiveData<String> get_mDirPath() {
        return mDirPath;
    }
}
