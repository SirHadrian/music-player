package com.sirhadrian.musicplayer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.sirhadrian.musicplayer.model.database.SongModel;

import java.util.Random;

public class Utils {

    @SuppressLint("DefaultLocale")
    public static String convertToMMSS(String duration) {
        long durationInMillis = Long.parseLong(duration);

        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        if (minute == 0) return String.format("%02d", second);
        if (hour == 0) return String.format("%02d:%02d", minute, second);

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public static int getRandomNumberUsingNextInt(int exclude, int min, int max) {
        Random random = new Random();
        int next = random.nextInt(max - min) + min;
        if (next == exclude) {
            next = getRandomNumberUsingNextInt(exclude, min, max);
        }
        return next;
    }

    public static byte[] getByteArrayFrom(Context context, SongModel song) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;

        mmr.setDataSource(context, Uri.parse(song.get_mSongUri()));
        rawArt = mmr.getEmbeddedPicture();

        return rawArt;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(byte[] img, int reqWidth, int reqHeight) {
        if (img == null) {
            return null;
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(img, 0, img.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(img, 0, img.length, options);
    }
}
