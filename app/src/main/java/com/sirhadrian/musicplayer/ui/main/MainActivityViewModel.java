package com.sirhadrian.musicplayer.ui.main;

import androidx.lifecycle.ViewModel;

import com.sirhadrian.musicplayer.services.PlaySongs;

import java.lang.ref.WeakReference;

public class MainActivityViewModel extends ViewModel {

    private boolean boundValueRaw;

    public boolean isBoundValueRaw() {
        return boundValueRaw;
    }

    public void set_BoundValueRaw(boolean boundValueRaw) {
        this.boundValueRaw = boundValueRaw;
    }

    private WeakReference<PlaySongs> serviceBound;

    public PlaySongs get_mService() {
        if (serviceBound != null) {
            return serviceBound.get();
        } else {
            return null;
        }
    }

    public void setServiceBound(PlaySongs service) {
        serviceBound = new WeakReference<>(service);
    }

    private boolean hasOrientationChanged = false;

    public void setHasOrientationChanged(boolean changed) {
        this.hasOrientationChanged = changed;
    }

    public boolean isHasOrientationChanged() {
        return hasOrientationChanged;
    }
}
