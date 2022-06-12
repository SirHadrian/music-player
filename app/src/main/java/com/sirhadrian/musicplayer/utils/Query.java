package com.sirhadrian.musicplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.sirhadrian.musicplayer.model.database.SongModel;

import java.util.ArrayList;
import java.util.List;

public class Query {

    public Query() {
    }

    public static ArrayList<SongModel> getSongsFromFolder(final Context context, Uri folder) {
        ArrayList<SongModel> songs = new ArrayList<>();

        DocumentFile dir = DocumentFile.fromTreeUri(context, folder);
        assert dir != null;
        DocumentFile[] filesInDir = dir.listFiles();

        for (DocumentFile file : filesInDir) {
            songs.add(new SongModel(file.getName(),"", file.getUri().toString(), 0));
        }

        return songs;
    }

    private static String getRealPathFromURI(final Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = context.getApplicationContext().getContentResolver().query(contentUri, proj, null, null, null)) {
            if (cursor == null) {
                return contentUri.getPath();
            }
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }

    public static ArrayList<SongModel> getAllAudioFromDevice(final Context context, Uri folderName) {
        ArrayList<SongModel> songs = new ArrayList<>();
        String testFolder = "/storage/44A6-B704/Documents/C_E_M";

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.DATA + " like ? ";

        String[] selectionArgs = null;
        if (folderName != null) {
            selectionArgs = new String[]{String.format("%%%s%%", folderName)};
        }

        try (Cursor cursor = context.getApplicationContext().getContentResolver().query(
                collection,
                projection,
                null,
                selectionArgs,
                null
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                int duration = cursor.getInt(durationColumn);

                assert contentUri != null;
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songs.add(new SongModel(title, artist,  contentUri.toString(), duration));
            }
        }
        return songs;
    }
}
