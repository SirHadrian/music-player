package com.sirhadrian.musicplayer.utils;

public interface ResultCallback<T> {
    void onComplete(Result<T> result);
}
