package com.sirhadrian.musicplayer.ui;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.databinding.FragmentSongDetailBinding;
import com.sirhadrian.musicplayer.model.SongModel;

import java.io.File;
import java.io.IOException;

public class SongDetailFragment extends Fragment {

    private SharedDataViewModel mSharedData;
    private TextView mSongDetailTitle;
    private MediaPlayer mPlayer;

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

        mPlayer = new MediaPlayer();
        mPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSharedData.getPlayingNow().observe(getViewLifecycleOwner(), new Observer<SongModel>() {
            @Override
            public void onChanged(SongModel songModel) {
                mSongDetailTitle.setText(songModel.get_mSongTitle());

                Uri uri = Uri.fromFile(new File(songModel.get_mSongUri()));

                try {
                    mPlayer.setDataSource(getContext(), uri);
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mPlayer.start();
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mPlayer.release();
    }
}
