package com.sirhadrian.musicplayer.ui.main;

import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private boolean hasOrientationChanged = false;

    public void setHasOrientationChanged(boolean changed) {
        this.hasOrientationChanged = changed;
    }

    public boolean isHasOrientationChanged() {
        return hasOrientationChanged;
    }
}
