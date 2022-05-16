package com.sirhadrian.musicplayer.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.ExternalStorageStats;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongsListBinding;
import com.sirhadrian.musicplayer.model.AudioModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongsListFragment extends Fragment {
    private ArrayList<HashMap<String, String>> tempSongs;

    private RecyclerView mRecyclerView;
    private SongsAdapter mSongsAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container,
                false);

        View view = binding.getRoot();

        mRecyclerView = binding.fragmentSongsListRecyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Toolbar toolbar = binding.myToolbar;
        toolbar.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.settings:
                            break;

                        case R.id.scan:
                            Log.d("scan", "Am access to external storage");

                            Log.d("scan", "Started Scanning");
                            String myFolderTemp = "/storage/44A6-B704/Documents/Music/E_B_M";
                            tempSongs = getPlayList(myFolderTemp);
                            List<String> mySongs = new ArrayList<>();
                            if (tempSongs != null) {
                                for (int i = 0; i < tempSongs.size(); i++) {
                                    String fileName = tempSongs.get(i).get("file_name");
                                    String filePath = tempSongs.get(i).get("file_path");
                                    Log.d("scan", " name =" + fileName + " path = " + filePath);
                                    mySongs.add(fileName);
                                }
                            }

                            if (mySongs != null) {
                                mSongsAdapter = new SongsAdapter(mySongs);
                            }
                            mRecyclerView.setAdapter(mSongsAdapter);
                        /*
                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });*/


                            Log.d("scan", "End Scan");


                            break;
                        default:
                            return false;
                    }

                    return true;
                }
        );



/*
        // Temp ===========================================================
        tempSongs = new ArrayList<>();
        for (int i = 0; i < 30; ++i) {
            tempSongs.add("Song number #" + (i + 1));
        }

        if (mSongsAdapter == null) {
            mSongsAdapter = new SongsAdapter(tempSongs);
        }

        //================================================================

*/


        return view;
    }

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

        private List<String> mSongsTitles;

        public SongsAdapter(List<String> songs) {
            mSongsTitles = songs;
        }

        @NonNull
        @Override
        public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.song_item,
                    parent, false);
            return new SongHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SongHolder holder, int position) {
            holder.get_mSongTitle().setText(mSongsTitles.get(position));
        }

        @Override
        public int getItemCount() {
            return mSongsTitles.size();
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitle;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitle = itemView.findViewById(R.id.songTitle);
                mSongTitle.setOnClickListener(this);
            }

            public TextView get_mSongTitle() {
                return mSongTitle;
            }

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), get_mSongTitle().getText().toString() + " Clicked!",
                        Toast.LENGTH_SHORT).show();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new SongDetailFragment();

                fragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_holder, fragment)
                        .commit();

            }
        }
    }

    ArrayList<HashMap<String, String>> getPlayList(String rootPath) {
        ArrayList<HashMap<String, String>> fileList = new ArrayList<>();

        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getPlayList(file.getAbsolutePath()) != null) {
                        fileList.addAll(getPlayList(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    HashMap<String, String> song = new HashMap<>();
                    song.put("file_path", file.getAbsolutePath());
                    song.put("file_name", file.getName());
                    fileList.add(song);
                }
            }

            return fileList;
        } catch (Exception e) {
            Log.d("scan", e.toString());
        }
        return null;
    }

    public List<AudioModel> getAllAudioFromDevice(final Context context, String folder) {

        final List<AudioModel> tempAudioList = new ArrayList<>();

        Log.d("scan", "Inside getAudio");

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST,
        };
        Cursor c = context.getContentResolver().query(
                uri,
                projection,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%/storage/emulated/0/C_E_M%"},
                null
        );

        if (c == null) {
            Log.d("scan", "Cursor is null");
        }

        if (c != null) {
            Log.d("scan", "Cursor not null ");
            while (c.moveToNext()) {
                AudioModel audioModel = new AudioModel();
                String path = c.getString(0);
                String album = c.getString(1);
                String artist = c.getString(2);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.set_mName(name);
                audioModel.set_mAlbum(album);
                audioModel.set_mArtist(artist);
                audioModel.set_mPath(path);

                Log.e("Name :" + name, " Album :" + album);
                Log.e("Path :" + path, " Artist :" + artist);

                Log.d("scan", "Name " + name);

                tempAudioList.add(audioModel);
            }
            c.close();
        }
        Log.d("scan", "Scan method EXIT");
        return tempAudioList;
    }
}
