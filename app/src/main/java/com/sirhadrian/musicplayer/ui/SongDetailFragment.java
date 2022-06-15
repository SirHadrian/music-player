package com.sirhadrian.musicplayer.ui;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.sirhadrian.musicplayer.ui.main.MainActivityViewModel;
import com.sirhadrian.musicplayer.utils.Playable;
import com.sirhadrian.musicplayer.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.wasabeef.blurry.Blurry;

public class SongDetailFragment extends Fragment implements Playable, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    private TextView mSongTitle;
    private TextView mSongArtistName;
    private ImageView mArtImageView;

    private ArrayList<SongModel> mSongs;

    // Shared directly with songs list
    private Integer mPlayingNowIndex = 0;

    private boolean isPlaying = false;
    private ImageView mPlayPauseButton;
    private ImageView mShuffleButton;
    private SeekBar mSongSeekBar;

    private boolean shuffled = false;
    private boolean firstStart = true;
    private SharedDataViewModel mSharedData;

    private MainActivityViewModel mServiceBound;

    private TextView endPosition;
    private TextView startPosition;

    private Stack<Integer> mPrevSongsShuffleOn;

    // For blurry
    private ImageView mBlurBackground;

    public static final String CHANNEL_ID = "CHANNEL_1";
    public static final String ACTION_PREVIOUS = "action-previous";
    public static final String ACTION_PLAY = "action-play";
    public static final String ACTION_NEXT = "action-next";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongDetailBinding binding = FragmentSongDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mServiceBound = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);


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

        mSongArtistName = binding.songArtist;

        mArtImageView = binding.songArt;
        mSongSeekBar = binding.seekBar;
        mSongSeekBar.setOnSeekBarChangeListener(this);

        startPosition = binding.currentTime;
        endPosition = binding.totalTime;

        mPlayPauseButton = binding.pausePlay;
        ImageView mPrevButton = binding.prev;
        ImageView mNextButton = binding.next;
        mShuffleButton = binding.shuffle;
        mShuffleButton.setOnClickListener(this);

        mPlayPauseButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);

        mBlurBackground = binding.blurBackground;

        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSharedData.get_mSongsList().observe(getViewLifecycleOwner(), songs -> {
            mSongs = songs;
            if (firstStart) {
                playSong(mSongs.get(mPlayingNowIndex), true);
                firstStart = false;
            }
        });
        mSharedData.get_mPlayingNowIndex().observe(getViewLifecycleOwner(), position -> {
            if (Objects.equals(position, mPlayingNowIndex) && mPlayingNowIndex != 0) return;
            mPlayingNowIndex = position;
            playSong(mSongs.get(mPlayingNowIndex), false);
        });

        mPrevSongsShuffleOn = new Stack<>();

        return root;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (isPlaying && mServiceBound.get_mService().isPlayerAlive()) {
            if (b) {
                mServiceBound.get_mService().seekTo(i);
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
            createNotification(requireContext(), mSongs.get(mPlayingNowIndex));
        } else if (buttonId == R.id.shuffle) {
            if (shuffled) {
                mShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_24);
                shuffled = false;
                mPrevSongsShuffleOn.clear();
            } else {
                mShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_32_true);
                shuffled = true;
            }
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
        if (shuffled) {
            if (!mPrevSongsShuffleOn.isEmpty()) {
                mPlayingNowIndex = mPrevSongsShuffleOn.pop();
            } else return;

        } else if (mPlayingNowIndex - 1 >= 0) {
            mPlayingNowIndex -= 1;
        } else return;
        playSong(mSongs.get(mPlayingNowIndex), false);
        mSharedData.set_mPlayingNowIndex(mPlayingNowIndex);
    }

    public void next() {
        if (shuffled) {
            mPrevSongsShuffleOn.add(mPlayingNowIndex);
            mPlayingNowIndex = Utils.getRandomNumberUsingNextInt(mPlayingNowIndex, 0, mSongs.size());
        } else if (mPlayingNowIndex + 1 < mSongs.size()) {
            mPlayingNowIndex += 1;
        } else return;
        playSong(mSongs.get(mPlayingNowIndex), false);
        mSharedData.set_mPlayingNowIndex(mPlayingNowIndex);

    }

    public void playOrPause() {
        if (isPlaying) {
            mServiceBound.get_mService().pause();
            isPlaying = false;
        } else {
            mServiceBound.get_mService().start();
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
        redrawPlayPauseButton();
        createNotification(requireContext(), mSongs.get(mPlayingNowIndex));
    }

    @Override
    public void onTrackPause() {
        Log.e("buttons", "pause pressed");
        playOrPause();
        redrawPlayPauseButton();
        createNotification(requireContext(), mSongs.get(mPlayingNowIndex));
    }

    @Override
    public void onTrackNext() {
        Log.e("buttons", "next pressed");
        next();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    private void createNotification(Context context, SongModel song) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //MediaSessionCompat mediaSession = new MediaSessionCompat(context, "tag");

            String title = song.get_mSongTitle();
            String artist = song.get_mArtistName();

            PendingIntent pendingIntentPrevious;
            int draw_prev;
            if (mPlayingNowIndex == 0 || shuffled && mPrevSongsShuffleOn.isEmpty()) {
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
                    .setContentText(artist)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setSilent(true) // No sound
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity());

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1234, builder.build());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPlaying && mServiceBound.isBoundValueRaw()) {
                    mSongSeekBar.setProgress(mServiceBound.get_mService().getCurrentPosition());
                    startPosition.setText(Utils.convertToMMSS(mServiceBound.get_mService().getCurrentPosition() + ""));
                }
                new Handler().postDelayed(this, 100);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void displayCurrentSong(SongModel play) {
        mSongTitle.setText(play.get_mSongTitle());
        mSongTitle.setSelected(true);
        mSongArtistName.setText(play.get_mArtistName());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Bitmap art = Utils.decodeSampledBitmapFromResource(
                    Utils.getByteArrayFrom(requireContext(), play), 400, 400);
            // Default artwork
            if (art == null) {
                art = BitmapFactory.decodeResource(getResources(), R.drawable.music_icon_big);
            }
            setArtFromBackgroundThread(mArtImageView, mBlurBackground, art);
        });

    }

    private void setArtFromBackgroundThread(ImageView image, ImageView background, Bitmap art) {
        requireActivity().runOnUiThread(() -> {
            image.setImageBitmap(art);
            Blurry.with(requireContext())
                    .from(art)
                    .into(background);
        });
    }

    private void playSong(SongModel play, boolean justInitSong) {
        if (mServiceBound.isBoundValueRaw()) {
            displayCurrentSong(play);
            if (justInitSong) {
                mServiceBound.get_mService().playSong(play.get_mSongUri(), false);
                mSongSeekBar.setProgress(0);
                mSongSeekBar.setMax(mServiceBound.get_mService().getDuration());
                endPosition.setText(Utils.convertToMMSS(mServiceBound.get_mService().getDuration() + ""));
                mServiceBound.get_mService().setCompletionListener(this);
                isPlaying = false;
                redrawPlayPauseButton();
                return;
            }
            if (mServiceBound.get_mService().isPlaying()) {
                mServiceBound.get_mService().stop();
            }

            mServiceBound.get_mService().playSong(play.get_mSongUri(), true);
            mSongSeekBar.setProgress(0);
            mSongSeekBar.setMax(mServiceBound.get_mService().getDuration());
            endPosition.setText(Utils.convertToMMSS(mServiceBound.get_mService().getDuration() + ""));
            mServiceBound.get_mService().setCompletionListener(this);
            isPlaying = true;

            redrawPlayPauseButton();
            createNotification(requireContext(), play);
        }
    }


}
