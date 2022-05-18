package com.sirhadrian.musicplayer.ui;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongDetailBinding;
import com.sirhadrian.musicplayer.model.SongModel;

import java.io.File;
import java.io.IOException;

public class SongDetailFragment extends Fragment {

    private SharedDataViewModel mSharedData;
    private TextView mSongDetailTitle;

    public SongDetailFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongDetailBinding binding = FragmentSongDetailBinding.inflate(inflater, container,
                false);
        View view = binding.getRoot();
        mSongDetailTitle = binding.songDetailTextView;

        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSharedData.getPlayingNow().observe(getViewLifecycleOwner(), new Observer<SongModel>() {
            @Override
            public void onChanged(SongModel songModel) {
                mSongDetailTitle.setText(songModel.get_mSongTitle());
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
