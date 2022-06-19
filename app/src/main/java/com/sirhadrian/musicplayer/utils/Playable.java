package com.sirhadrian.musicplayer.utils;

/**
 * Interface for the BroadCast actions in notification
 */
public interface Playable {
    void onTrackPrevious();
    void onTrackPlay();
    void onTrackPause();
    void onTrackNext();
}
