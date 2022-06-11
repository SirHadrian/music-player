package com.sirhadrian.musicplayer.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sirhadrian.musicplayer.services.PlaySongs;

import java.lang.ref.WeakReference;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<PlaySongs> songService = new MutableLiveData<>();

    public LiveData<PlaySongs> get_songService() {
        return songService;
    }

    public void set_songService(PlaySongs service) {
        songService.setValue(service);
    }

    private final MutableLiveData<Boolean> boundValue = new MutableLiveData<>();

    public LiveData<Boolean> get_boundValue() {
        return boundValue;
    }

    public void set_boundValue(Boolean bound) {
        boundValue.setValue(bound);
    }

    private boolean boundValueRaw;

    public boolean isBoundValueRaw() {
        return boundValueRaw;
    }

    public void set_BoundValueRaw(boolean boundValueRaw) {
        this.boundValueRaw = boundValueRaw;
    }

    private WeakReference<PlaySongs> serviceBound;

    public PlaySongs getServiceBound() {
        if (serviceBound != null) {
            return serviceBound.get();
        } else {
            return null;
        }
    }

    public void setServiceBound(PlaySongs service) {
        serviceBound = new WeakReference<>(service);
    }
}
