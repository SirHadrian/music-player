package com.sirhadrian.musicplayer.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
import java.util.concurrent.Executors;

public class SongsListFragment extends Fragment {
    private List<HashMap<String, String>> mSongsList;

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
                            String myFolderTemp = "/storage/44A6-B704/Documents/Music/E_B_M";
                            mSongsList = getPlayList(myFolderTemp);

                            if (mSongsList != null) {

                                mSongsAdapter = new SongsAdapter(mSongsList);
                                mRecyclerView.setAdapter(mSongsAdapter);
                            }
                            /*
                            Executor executor = Executors.newSingleThreadExecutor();
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("scan", "Am access to external storage");
                                    Log.d("scan", "Started Scanning");


                                }
                            });
                            */

                            Log.d("scan", "End Scan");
                            break;
                        default:
                            return false;
                    }

                    return true;
                }
        );
        return view;
    }

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

        private List<HashMap<String, String>> mSongsTitles;

        public SongsAdapter(List<HashMap<String, String>> songs) {
            this.mSongsTitles = songs;
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
            holder.get_mSongTitle().setText(mSongsTitles.get(position).get("file_name"));
            holder.set_mSongPath(mSongsTitles.get(position).get("file_path"));
        }

        @Override
        public int getItemCount() {
            return mSongsTitles.size();
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitle;
            private String mSongPath;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitle = itemView.findViewById(R.id.songTitle);
                mSongTitle.setOnClickListener(this);
            }

            public TextView get_mSongTitle() {
                return mSongTitle;
            }

            public String get_mSongPath() {
                return mSongPath;
            }

            public void set_mSongPath(String mSongPath) {
                this.mSongPath = mSongPath;
            }

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), get_mSongTitle().getText().toString() + " Clicked!",
                        Toast.LENGTH_SHORT).show();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new SongDetailFragment();

                Bundle bundle = new Bundle();
                bundle.putString("path", mSongPath);
                bundle.putString("name", mSongTitle.getText().toString());
                fragment.setArguments(bundle);

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
            File[] files = rootFolder.listFiles();
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
}
