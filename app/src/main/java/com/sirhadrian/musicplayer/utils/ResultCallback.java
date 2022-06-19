package com.sirhadrian.musicplayer.utils;

/**
 * Used as a callback function
 * @param <T>
 */
public interface ResultCallback<T> {
    void onComplete(Result<T> result);
}
