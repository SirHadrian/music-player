package com.sirhadrian.musicplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.documentfile.provider.DocumentFile;

import com.sirhadrian.musicplayer.model.database.SongModel;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Query {

    public Query() {
    }

    public static ArrayList<SongModel> getSongsFromFolder(final Context context, Uri folder) {
        ArrayList<SongModel> songs = new ArrayList<>();

        DocumentFile dir = DocumentFile.fromTreeUri(context, folder);
        assert dir != null;
        DocumentFile[] filesInDir = dir.listFiles();

        for (DocumentFile file : filesInDir) {
            songs.add(new SongModel(file.getName(), "", file.getUri().toString(), 0));
        }

        return songs;
    }

    public static String findFullPath(String path) {
        String actualResult;
        path=path.substring(5);
        int index=0;
        StringBuilder result = new StringBuilder("/storage");
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) != ':') {
                result.append(path.charAt(i));
            } else {
                index = ++i;
                result.append('/');
                break;
            }
        }
        for (int i = index; i < path.length(); i++) {
            result.append(path.charAt(i));
        }
        if (result.substring(9, 16).equalsIgnoreCase("primary")) {
            actualResult = result.substring(0, 8) + "/emulated/0/" + result.substring(17);
        } else {
            actualResult = result.toString();
        }
        return actualResult;
    }

    public static void makeScanRequest(Context context, String folder, ResultCallback<ArrayList<SongModel>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Result<ArrayList<SongModel>> result = new Result.Success<>(Query.getAllAudioFromDevice(context, folder));
                callback.onComplete(result);
            } catch (Exception e) {
                Result<ArrayList<SongModel>> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    public static ArrayList<SongModel> getAllAudioFromDevice(final Context context, String folderName) {
        ArrayList<SongModel> songs = new ArrayList<>();
        //String testFolder = "/storage/44A6-B704/Documents/V_E_M";


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
        String selection = null;
        if (folderName != null) {
            selection = MediaStore.Audio.Media.DATA + " like ? ";
        }

        String[] selectionArgs = null;
        if (folderName != null) {
            selectionArgs = new String[]{String.format("%%%s%%", folderName)};
        }

        try (Cursor cursor = context.getApplicationContext().getContentResolver().query(
                collection,
                projection,
                selection,
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
                songs.add(new SongModel(title, artist, contentUri.toString(), duration));
            }
        }
        return songs;
    }
}
