package com.sirhadrian.musicplayer.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.sirhadrian.musicplayer.services.NotificationActionService;
import com.sirhadrian.musicplayer.services.OnClearFromRecentService;
import com.sirhadrian.musicplayer.services.PlaySongs;
import com.sirhadrian.musicplayer.utils.Playable;

import java.util.ArrayList;
import java.util.List;

public class SongDetailFragment extends Fragment implements ServiceConnection, Playable, View.OnClickListener {

    private TextView mSongDetailTitle;

    private PlaySongs mService;
    private boolean mBound = false;

    private ArrayList<SongModel> mSongs;
    private Integer mPlayingNowIndex = null;
    boolean isPlaying = false;

    public static final String CHANNEL_ID = "CHANNEL_1";
    public static final String ACTION_PREVIOUS = "action-previous";
    public static final String ACTION_PLAY = "action-play";
    public static final String ACTION_NEXT = "action-next";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongDetailBinding binding = FragmentSongDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getExtras().getString("action-name");
                    switch (action) {
                        case ACTION_PREVIOUS:
                            onTrackPrevious();
                            break;
                        case ACTION_PLAY:
                            if (isPlaying) {
                                onTrackPause();
                            } else {
                                onTrackPlay();
                            }
                            break;
                        case ACTION_NEXT:
                            onTrackNext();
                            break;
                    }
                }
            };

            requireActivity().registerReceiver(broadcastReceiver, new IntentFilter("SONG"));
            requireActivity().startService(new Intent(requireActivity().getBaseContext(), OnClearFromRecentService.class));
        }

        mSongDetailTitle = binding.songDetailTextView;

        ImageView mArtImageView = binding.songArt;
        SeekBar mSongSeekBar = binding.seekBar;

        ImageButton mPlayPauseButton = binding.play;
        ImageButton mPrevButton = binding.prev;
        ImageButton mNextButton = binding.next;

        mPlayPauseButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);


        SharedDataViewModel mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSharedData.get_mSongsList().observe(getViewLifecycleOwner(), songs -> mSongs = songs);
        mSharedData.get_mPlayingNowIndex().observe(getViewLifecycleOwner(), position -> {
            mPlayingNowIndex = position;
            if (mPlayingNowIndex != null) {
                playSong(mSongs.get(mPlayingNowIndex));
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();

        if (buttonId == R.id.prev) {
            prev();
        } else if (buttonId == R.id.next) {
            next();
        } else if (buttonId == R.id.play) {
            playOrPause();
        }
    }

    public void prev() {
        if (mPlayingNowIndex - 1 >= 0) {
            mPlayingNowIndex -= 1;
            playSong(mSongs.get(mPlayingNowIndex));
        }
    }

    public void next() {
        if (mPlayingNowIndex + 1 < mSongs.size()) {
            mPlayingNowIndex += 1;
            playSong(mSongs.get(mPlayingNowIndex));
        }
    }

    public void playOrPause() {
        if (isPlaying) {
            mService.pause();
            isPlaying = false;
        } else {
            mService.start();
            isPlaying = true;
        }
    }

    @Override
    public void onTrackPrevious() {
        Log.e("buttons", "prev pressed");
        prev();
    }

    @Override
    public void onTrackPlay() {
        Log.e("buttons", "play pressed");
        playOrPause();
    }

    @Override
    public void onTrackPause() {
        Log.e("buttons", "pause pressed");
        playOrPause();
    }

    @Override
    public void onTrackNext() {
        Log.e("buttons", "next pressed");
        next();
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

    private void createNotification(Context context, String title, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //MediaSessionCompat mediaSession = new MediaSessionCompat(context, "tag");

            PendingIntent pendingIntentPrevious;
            int draw_prev;
            if (mPlayingNowIndex - 1 == 0) {
                pendingIntentPrevious = null;
                draw_prev = 0;
            } else {
                Intent intentPrevious = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                        intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
                draw_prev = R.drawable.ic_skip_previous_black_24dp;
            }


            int draw_playPause;
            if (isPlaying) {
                draw_playPause = R.drawable.ic_play_arrow_black_24dp;
            } else {
                draw_playPause = R.drawable.ic_pause_black_24dp;
            }
            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                    intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pendingIntentNext;
            int draw_next;
            if (mPlayingNowIndex + 1 == mSongs.size()) {
                pendingIntentNext = null;
                draw_next = 0;
            } else {
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                        intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                draw_next = R.drawable.ic_skip_next_black_24dp;
            }


            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireActivity(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                    .addAction(draw_prev, "Previous", pendingIntentPrevious) // #0
                    .addAction(draw_playPause, "Pause", pendingIntentPlay)  // #1
                    .addAction(draw_next, "Next", pendingIntentNext)     // #2

                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0, 1, 2)
                            //.setMediaSession(mediaSession.getSessionToken())
                    )

                    .setContentTitle(title)
                    .setContentText(content)

                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity());

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1234, builder.build());
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

        if (mPlayingNowIndex != null) {
            playSong(mSongs.get(mPlayingNowIndex));
        }
    }

    private void playSong(SongModel play) {
        if (mBound) {
            mSongDetailTitle.setText(play.get_mSongTitle());
            createNotification(requireContext(), play.get_mSongTitle(), play.get_mSongUri());
            isPlaying = true;

            if (mService.isPlaying()) {
                mService.stop();
            }
            mService.playSong(requireContext(), play.get_mSongUri());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
    }
}
