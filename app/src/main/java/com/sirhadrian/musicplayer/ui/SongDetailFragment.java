package com.sirhadrian.musicplayer.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.databinding.FragmentSongDetailBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.services.PlaySongs;

import java.io.IOException;

public class SongDetailFragment extends Fragment implements ServiceConnection {

    private TextView mSongDetailTitle;
    private PlaySongs mService;
    private boolean mBound = false;
    private SharedDataViewModel mSharedData;

    private SongModel mPlayingNow;

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

        mSharedData.getPlayingNow().observe(getViewLifecycleOwner(), songModel -> {
            mPlayingNow = songModel;
            playSong();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(requireContext(), PlaySongs.class);
        requireActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mService.release();
        requireActivity().unbindService(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBound = false;
    }

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        PlaySongs.LocalBinder binder = (PlaySongs.LocalBinder) service;
        mService = binder.getService();
        mBound = true;

        playSong();
    }

    private void playSong() {
        if (mBound && mPlayingNow != null) {
            mSongDetailTitle.setText(mPlayingNow.get_mSongTitle());

            if (mService.isPlaying()) {
                mService.stop();
            }
            mService.playSong(requireContext(), mPlayingNow.get_mSongUri());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
    }
}
