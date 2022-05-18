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

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongDetailBinding;

import java.io.File;
import java.io.IOException;

public class SongDetailFragment extends Fragment {

    private TextView mSongDetailTitle;
    MediaPlayer mediaPlayer;

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

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_holder);

        assert fragment != null;
        assert fragment.getArguments() != null;
        mSongDetailTitle.setText(fragment.getArguments().getString("name"));

        Uri uri = Uri.fromFile(new File(fragment.getArguments().getString("path")));

        Log.d("uri", fragment.getArguments().getString("path"));


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(getContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();


        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayer.release();
    }
}
