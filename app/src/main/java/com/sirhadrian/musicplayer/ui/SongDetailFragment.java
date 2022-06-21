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

import com.sirhadrian.musicplayer.MainActivity;
import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongDetailBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.services.NotificationActionService;
import com.sirhadrian.musicplayer.services.OnClearFromRecentService;
import com.sirhadrian.musicplayer.services.PlaySongsService;
import com.sirhadrian.musicplayer.utils.Playable;
import com.sirhadrian.musicplayer.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import jp.wasabeef.blurry.Blurry;

public class SongDetailFragment extends Fragment implements Playable, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    // MainActivity reference for getting bound service
    private MainActivity mMainActivity;
    private PlaySongsService mService;
    private boolean mBound = false;
    // Current playlist
    private ArrayList<SongModel> mSongs;
    // Shared with SongListFragment
    private Integer mPlayingNowIndex = 0;
    // Shared data with all fragments
    private SharedDataViewModel mSharedData;
    // Detail song screen
    private TextView mSongTitle;
    private TextView mSongArtistName;
    private ImageView mSongArtWork;
    private SeekBar mSongSeekBar;
    private ImageView mPlayPauseButton;
    private ImageView mPrevButton;
    private ImageView mNextButton;
    // To init the index=0 song
    private boolean firstStart = true;
    // Song duration textview display
    private TextView endPosition;
    private TextView startPosition;
    // Shuffle songs
    private ImageView mShuffleButton;
    private Stack<Integer> mPrevSongsShuffleOn;
    private boolean shuffled = false;
    // For blurry background
    private ImageView mBlurBackground;
    // Notification channel actions
    public static final String CHANNEL_ID = "CHANNEL_1";
    public static final String ACTION_PREVIOUS = "action-previous";
    public static final String ACTION_PLAY = "action-play";
    public static final String ACTION_NEXT = "action-next";
    // Notification
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManagerCompat mNotifyManager;
    // Notification ID must be unique and can be used to update an existing notification
    private static final int mNotificationID = 3321;

    // region Lifecycle methods
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflating layout
        FragmentSongDetailBinding binding = FragmentSongDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Creating notification channel and broadcast actions
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
                            if (mService.isPlaying()) {
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

        // View bindings song detail
        mSongTitle = binding.songTitle;
        mSongArtistName = binding.songArtist;
        mSongArtWork = binding.songArt;
        mSongSeekBar = binding.seekBar;
        startPosition = binding.currentTime;
        endPosition = binding.totalTime;
        mPlayPauseButton = binding.pausePlay;
        mPrevButton = binding.prev;
        mNextButton = binding.next;
        mShuffleButton = binding.shuffle;
        mBlurBackground = binding.blurBackground;
        // View Listeners
        mShuffleButton.setOnClickListener(this);
        mSongSeekBar.setOnSeekBarChangeListener(this);
        mPlayPauseButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);

        // Shared songs between fragments
        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSharedData.get_mSongsList().observe(getViewLifecycleOwner(), songs -> {
            mSongs = songs;
            if (firstStart) {
                buildNotification(requireContext());
                playSong(mSongs.get(mPlayingNowIndex), true);
                firstStart = false;
            }
        });
        // Shared current selected song
        mSharedData.get_mPlayingNowIndex().observe(getViewLifecycleOwner(), position -> {
            if (Objects.equals(position, mPlayingNowIndex) && mPlayingNowIndex != 0) return;
            mPlayingNowIndex = position;
            playSong(mSongs.get(mPlayingNowIndex), false);
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Keeps list of previews played songs from the first next() call
        mPrevSongsShuffleOn = new Stack<>();
        // Get reference to parent activity and get the service binder
        mMainActivity = (MainActivity) requireActivity();
        mService = mMainActivity.get_mService();
        mBound = mMainActivity.ismBound();

        mNotifyManager = NotificationManagerCompat.from(requireActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBound) {
                    if (mService.isPlaying()) {
                        mSongSeekBar.setProgress(mService.getCurrentPosition());
                        startPosition.setText(Utils.convertToMMSS(mService.getCurrentPosition() + ""));
                    }
                    new Handler().postDelayed(this, 100);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    // endregion

    // region Implemented Interfaces
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (mService.isPlaying()) {
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
        } else if (buttonId == R.id.shuffle) {
            if (shuffled) {
                mShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_24);
                shuffled = false;
                mPrevSongsShuffleOn.clear();
            } else {
                mShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_32_true);
                shuffled = true;
                //buildNotification(requireContext(), mSongs.get(mPlayingNowIndex));
            }
        }
    }
    // endregion

    // region Song controls
    private void redrawPlayPauseButton() {
        if (mService.isPlaying()) {
            mPlayPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
        } else {
            mPlayPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
        }
        drawNotificationDisplayButtons(requireContext(), mNotificationBuilder);
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
        if (mService.isPlaying()) {
            mService.pause();
        } else {
            mService.start();
        }
        // Update the notification play button
        redrawPlayPauseButton();
        mNotifyManager.notify(mNotificationID, mNotificationBuilder.build());
    }
    // endregion

    // region Display and play Song
    private void displayCurrentSong(SongModel play) {
        mSongTitle.setText(play.get_mSongTitle());
        mSongTitle.setSelected(true);
        mSongArtistName.setText(play.get_mArtistName());

        redrawPlayPauseButton();
        mNotificationBuilder.setContentTitle(play.get_mSongTitle());
        mNotificationBuilder.setContentText(play.get_mArtistName());
        mNotifyManager.notify(mNotificationID, mNotificationBuilder.build());

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Bitmap art = Utils.decodeSampledBitmapFromResource(
                    Utils.getByteArrayFrom(requireContext(), play), 400, 400);
            // Default artwork
            if (art == null) {
                art = BitmapFactory.decodeResource(getResources(), R.drawable.music_icon_big);
            }
            setArtFromBackgroundThread(mSongArtWork, mBlurBackground, art);
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
        if (mBound) {
            if (mService.isPlaying()) {
                mService.stop();
            }

            mService.playSong(play.get_mSongUri(), !justInitSong);

            mSongSeekBar.setProgress(0);
            mSongSeekBar.setMax(mService.getDuration());
            endPosition.setText(Utils.convertToMMSS(mService.getDuration() + ""));
            mService.setCompletionListener(this);

            displayCurrentSong(play);
        }
    }
    // endregion

    // region Notification
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
    private void buildNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationBuilder = new NotificationCompat.Builder(requireActivity(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2))
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setSilent(true) // No sound
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            drawNotificationDisplayButtons(context, mNotificationBuilder);
        }
    }

    // Redraw the notification buttons with the correct constraints
    private void drawNotificationDisplayButtons(Context context, NotificationCompat.Builder builder) {
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
        if (mService.isPlaying()) {
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
        // Redraw notification buttons
        builder.clearActions();
        builder.addAction(draw_prev, "Previous", pendingIntentPrevious) // #0
                .addAction(draw_playPause, "Pause", pendingIntentPlay)  // #1
                .addAction(draw_next, "Next", pendingIntentNext);     // #2
    }
    // endregion

    // region Broadcast Interface
    @Override
    public void onTrackPrevious() {
        prev();
    }

    @Override
    public void onTrackPlay() {
        playOrPause();
    }

    @Override
    public void onTrackPause() {
        playOrPause();
    }

    @Override
    public void onTrackNext() {
        next();
    }
    // endregion
}
