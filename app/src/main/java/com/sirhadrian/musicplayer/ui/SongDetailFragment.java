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
import androidx.documentfile.provider.DocumentFile;
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
        mSharedData.getPlayingNow().observe(getViewLifecycleOwner(), songModel -> {

            mSongDetailTitle.setText(songModel.get_mSongTitle());

            Uri uri = Uri.parse("content://com.android.externalstorage.documents/tree/44A6-B704%3ADocuments%2FC_E_M/document/44A6-B704%3ADocuments%2FC_E_M%2FElven%20Star.mp3");

            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.reset();
            try {
                mPlayer.setDataSource(getContext(), uri);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });



        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPlayer != null) {
            mPlayer.release();
        }
    }
}
