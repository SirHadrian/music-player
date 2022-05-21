package com.sirhadrian.musicplayer.ui;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongsListBinding;
import com.sirhadrian.musicplayer.model.SongModel;
import com.sirhadrian.musicplayer.settings.Settings;
import com.sirhadrian.musicplayer.settings.SettingsViewModel;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongsListFragment extends Fragment {

    private List<SongModel> mSongsList;

    private SharedDataViewModel mSharedData;
    private SettingsViewModel mSettings;

    private RecyclerView mRecyclerView;
    private SongsAdapter mSongsAdapter;
    private final ViewPager2 viewPager2Activity;

    private String searchFolder;


    public SongsListFragment(ViewPager2 viewPager) {
        this.viewPager2Activity = viewPager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container,
                false);

        View view = binding.getRoot();

        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSettings = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        mSettings.get_mDirPath().observe(getViewLifecycleOwner(), s -> searchFolder = s);

        mRecyclerView = binding.fragmentSongsListRecyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Toolbar toolbar = requireActivity().findViewById(R.id.my_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.settings:
                            makeSettingsRequest();
                            break;

                        case R.id.scan:
                            makeScanRequest(searchFolder, result -> {
                                if (result instanceof Result.Success) {
                                    mSongsList = ((Result.Success<List<SongModel>>) result).get_Data();
                                    if (mSongsList != null) {
                                        mSongsAdapter = new SongsAdapter(mSongsList);
                                        mRecyclerView.setAdapter(mSongsAdapter);
                                    }
                                } else if (result instanceof Result.Error) {
                                    ((Result.Error<List<SongModel>>) result).exception.printStackTrace();
                                }
                            });
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

    private void makeSettingsRequest() {
        Intent intent = new Intent(getContext(), Settings.class);
        startActivity(intent);
    }

    private void makeScanRequest(String folder, ResultCallback<List<SongModel>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Result<List<SongModel>> result = new Result.Success<>(getPlayList(folder));
                callback.onComplete(result);
            } catch (Exception e) {
                Result<List<SongModel>> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

        private final List<SongModel> mSongsTitles;

        public SongsAdapter(List<SongModel> songs) {
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
            holder.get_mSongModel().set_mSongTitle(mSongsList.get(position).get_mSongTitle());
            holder.get_mSongModel().set_mSongUri(mSongsTitles.get(position).get_mSongUri());
            holder.get_mSongTitle().setText(mSongsTitles.get(position).get_mSongTitle());
        }

        @Override
        public int getItemCount() {
            return mSongsTitles.size();
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitleTextView;
            private SongModel mSongModel;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitleTextView = itemView.findViewById(R.id.songTitle);
                mSongTitleTextView.setOnClickListener(this);
            }

            public SongModel get_mSongModel() {
                if (mSongModel == null) {
                    mSongModel = new SongModel();
                }
                return mSongModel;
            }

            public TextView get_mSongTitle() {
                return mSongTitleTextView;
            }

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), mSongModel.get_mSongTitle() + " Clicked!",
                        Toast.LENGTH_SHORT).show();

                mSharedData.select(get_mSongModel());

                viewPager2Activity.setCurrentItem(viewPager2Activity.getCurrentItem() + 1);
            }
        }
    }

    List<SongModel> getPlayList(String rootPath) {
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
