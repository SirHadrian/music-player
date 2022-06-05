package com.sirhadrian.musicplayer.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongDetailBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.services.PlaySongs;

public class SongDetailFragment extends Fragment implements ServiceConnection {

    private TextView mSongDetailTitle;
    private PlaySongs mService;
    private boolean mBound = false;
    private SharedDataViewModel mSharedData;

    private SongModel mPlayingNow;

    public static final String CHANNEL_ID = "CHANNEL_1";

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

        mSongDetailTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNotificationChannel();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(requireActivity(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_music_note)
                        .setContentTitle(mPlayingNow.get_mSongTitle())
                        .setContentText(mPlayingNow.get_mSongUri())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity());

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(1234, builder.build());
            }
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
