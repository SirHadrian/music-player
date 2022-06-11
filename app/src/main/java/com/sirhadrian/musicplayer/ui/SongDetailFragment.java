package com.sirhadrian.musicplayer.ui;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.sirhadrian.musicplayer.utils.Utils;

import java.util.ArrayList;

public class SongDetailFragment extends Fragment implements ServiceConnection, Playable, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    private TextView mSongTitle;
    private ImageView mArtImageView;

    private PlaySongs mService;
    private boolean mBound = false;
    private boolean initSong = true;

    private ArrayList<SongModel> mSongs;
    private Integer mPlayingNowIndex = 0;
    boolean isPlaying = false;
    private ImageView mPlayPauseButton;
    private SeekBar mSongSeekBar;

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

        mSongTitle = binding.songTitle;
        mSongTitle.setSelected(true);

        mArtImageView = binding.songArt;
        mSongSeekBar = binding.seekBar;
        mSongSeekBar.setOnSeekBarChangeListener(this);

        TextView startPosition = binding.currentTime;
        TextView endPosition = binding.totalTime;

        mPlayPauseButton = binding.pausePlay;
        ImageView mPrevButton = binding.prev;
        ImageView mNextButton = binding.next;

        mPlayPauseButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);


        SharedDataViewModel mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSharedData.get_mSongsList().observe(getViewLifecycleOwner(), songs -> mSongs = songs);
        mSharedData.get_mPlayingNowIndex().observe(getViewLifecycleOwner(), position -> {
            mPlayingNowIndex = position;
            if (mPlayingNowIndex != null && mBound) {
                playSong(mSongs.get(mPlayingNowIndex));
            }
        });

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPlaying && mBound) {
                    mSongSeekBar.setProgress(mService.getCurrentPosition());
                    endPosition.setText(Utils.convertToMMSS(mService.getCurrentPosition() + ""));
                }
                new Handler().postDelayed(this, 100);
            }
        });

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (isPlaying && mService.isCreated()) {
            if (b) {
                mService.seekTo(i);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();

        if (buttonId == R.id.prev) {
            prev();
        } else if (buttonId == R.id.next) {
            next();
        } else if (buttonId == R.id.pause_play) {
            playOrPause();
            redrawPlayPauseButton();
            createNotification(requireContext(), mSongs.get(mPlayingNowIndex).get_mSongTitle());
        }
    }

    private void redrawPlayPauseButton() {
        if (isPlaying) {
            mPlayPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
        } else {
            mPlayPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
        }
    }

    public void prev() {
        if (mPlayingNowIndex - 1 >= 0) {
            mPlayingNowIndex -= 1;
            playSong(mSongs.get(mPlayingNowIndex));
            redrawPlayPauseButton();
        }
    }

    public void next() {
        if (mPlayingNowIndex + 1 < mSongs.size()) {
            mPlayingNowIndex += 1;
            playSong(mSongs.get(mPlayingNowIndex));
            redrawPlayPauseButton();
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

    private void loadImageFromUri(SongModel song) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        mmr.setDataSource(requireContext(), Uri.parse(song.get_mSongUri()));
        rawArt = mmr.getEmbeddedPicture();

        // if rawArt is null then no cover art is embedded in the file or is not
        // recognized as such.
        if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
            mArtImageView.setImageBitmap(art);
        }
    }

    @Override
    public void onTrackPrevious() {
        Log.e("buttons", "prev pressed");
        prev();
        createNotification(requireContext(), mSongs.get(mPlayingNowIndex).get_mSongTitle());
    }

    @Override
    public void onTrackPlay() {
        Log.e("buttons", "play pressed");
        playOrPause();
        createNotification(requireContext(), mSongs.get(mPlayingNowIndex).get_mSongTitle());
        redrawPlayPauseButton();
    }

    @Override
    public void onTrackPause() {
        Log.e("buttons", "pause pressed");
        playOrPause();
        createNotification(requireContext(), mSongs.get(mPlayingNowIndex).get_mSongTitle());
        redrawPlayPauseButton();
    }

    @Override
    public void onTrackNext() {
        Log.e("buttons", "next pressed");
        next();
        createNotification(requireContext(), mSongs.get(mPlayingNowIndex).get_mSongTitle());
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

    @SuppressLint("UnspecifiedImmutableFlag")
    private void createNotification(Context context, String title) {
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
                draw_playPause = R.drawable.ic_pause_black_24dp;
            } else {
                draw_playPause = R.drawable.ic_play_arrow_black_24dp;
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
                    .setPriority(NotificationCompat.PRIORITY_LOW); // No sound

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
        NotificationManagerCompat.from(requireContext()).cancelAll();
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
        Log.d("myserv", "Service connected can modify player");
    }

    private void displayCurrentSong(SongModel play) {
        mSongTitle.setText(play.get_mSongTitle());
        createNotification(requireContext(), play.get_mSongTitle());
    }

    private void playSong(SongModel play) {
        if (mBound) {
            displayCurrentSong(play);
            loadImageFromUri(play);
            if (initSong) {
                mService.playSong(requireContext(), play.get_mSongUri(), false);
                mSongSeekBar.setProgress(0);
                mSongSeekBar.setMax(mService.getDuration());
                mService.setCompletionListener(this);

                initSong = false;
                isPlaying = false;
                redrawPlayPauseButton();
                return;
            }
            if (mService.isPlaying()) {
                mService.stop();
            }

            mService.playSong(requireContext(), play.get_mSongUri(), true);
            mSongSeekBar.setProgress(0);
            mSongSeekBar.setMax(mService.getDuration());
            mService.setCompletionListener(this);
            isPlaying = true;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
        Log.d("myserv", "Service disconnected can't modify player");
    }
}
