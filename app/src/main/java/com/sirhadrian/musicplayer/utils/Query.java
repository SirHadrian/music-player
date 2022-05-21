package com.sirhadrian.musicplayer.utils;

import android.util.Log;

import com.sirhadrian.musicplayer.model.SongModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Query {

    public Query() {
    }

    public static List<SongModel> getPlayList(String rootPath) {
        List<SongModel> fileList = new ArrayList<>();

        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getPlayList(file.getAbsolutePath()) != null) {
                        fileList.addAll(getPlayList(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    SongModel song = new SongModel(file.getName(), file.getAbsolutePath());
                    fileList.add(song);
                }
            }

            return fileList;
        } catch (Exception e) {
            Log.d("scan", e.toString());
        }
        return null;
    }
}
