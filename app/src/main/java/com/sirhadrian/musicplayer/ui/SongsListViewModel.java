package com.sirhadrian.musicplayer.ui;

import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import java.lang.ref.WeakReference;

public class SongsListViewModel extends ViewModel {

    // WeakRef the avoid leaks
    private WeakReference<NavController> mNavCtrl;

    public NavController get_mNavCtrl() {
        return mNavCtrl.get();
    }

    public void set_mNavCtrl(NavController mNavCtrl) {
        this.mNavCtrl = new WeakReference<>(mNavCtrl);
    }
}
