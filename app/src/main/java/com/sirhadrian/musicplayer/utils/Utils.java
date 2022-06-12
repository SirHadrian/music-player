package com.sirhadrian.musicplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.sirhadrian.musicplayer.model.database.SongModel;

import java.util.concurrent.TimeUnit;

import jp.wasabeef.blurry.Blurry;

public class Utils {

    public static String convertToMMSS(String duration) {
        long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static Bitmap loadImageFromUri(Context context, SongModel song) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        mmr.setDataSource(context, Uri.parse(song.get_mSongUri()));
        rawArt = mmr.getEmbeddedPicture();

        if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
            return art;
        }
        return null;
    }
}
