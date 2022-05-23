package com.sirhadrian.musicplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.sirhadrian.musicplayer.model.SongModel;

import java.util.ArrayList;
import java.util.List;

public class Query {

    public Query() {
    }

    public static List<SongModel> getSongsFromFolder(final Context context, Uri folder) {
        List<SongModel> songs = new ArrayList<>();

        DocumentFile dir = DocumentFile.fromTreeUri(context, folder);
        assert dir != null;
        DocumentFile[] filesInDir = dir.listFiles();

        for (DocumentFile file : filesInDir) {
            songs.add(new SongModel(file.getName(), file.getUri()));
        }

        Log.d("files", songs.toString());

        return songs;
    }


    public static List<SongModel> getAllAudioFromDevice(final Context context, Uri folderName) {
        List<SongModel> songs = new ArrayList<>();
        //String testFolder="/storage/44A6-B704/Documents/C_E_M";

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME
        };
        String selection = MediaStore.Audio.Media.DATA + " like ? ";

        String[] selectionArgs = null;
        if (folderName != null) {
            selectionArgs = new String[]{String.format("%%%s%%", folderName)};
        }

        try (Cursor cursor = context.getApplicationContext().getContentResolver().query(
                folderName,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                assert contentUri != null;
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songs.add(new SongModel(name, contentUri));
            }
        }
        return songs;
    }


}
