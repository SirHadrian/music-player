package com.sirhadrian.musicplayer.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongsListBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.ui.viewpager.ViewPagerFragment;
import com.sirhadrian.musicplayer.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.wasabeef.blurry.Blurry;

public class SongsListFragment extends Fragment implements View.OnClickListener {

    private List<SongModel> mSongsList;

    private SharedDataViewModel mSharedData;

    private SongsAdapter mSongsAdapter;
    //private NavController navController;
    private LruCache<String, Bitmap> memoryCache;

    //Bottom view
    private ImageView backgroundImage;
    private ImageView smallIconImage;
    private TextView songTitle;
    private TextView artistName;
    private ConstraintLayout rowLayout;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        backgroundImage = binding.itemBackgroundImage;
        smallIconImage = binding.smallSongIcon;
        songTitle = binding.songTitle;
        artistName = binding.songArtist;
        rowLayout = binding.bottomRow;

        mSongsList = new ArrayList<>();

        RecyclerView mRecyclerView = binding.fragmentSongsListRecyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSongsAdapter = new SongsAdapter(mSongsList);
        mRecyclerView.setAdapter(mSongsAdapter);


        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSharedData.get_mSongsList().observe(getViewLifecycleOwner(), songs -> {
            mSongsList.clear();
            mSongsList.addAll(songs);
            mSongsAdapter.notifyDataSetChanged();
        });

        mSharedData.get_mPlayingNowIndex().observe(getViewLifecycleOwner(),
                position -> displayPlayingNowIndexAtBottom(position));

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/4th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };


        return view;
    }

    private void displayPlayingNowIndexAtBottom(Integer position) {
        SongModel currentPlaying = mSongsList.get(position);

        songTitle.setText(currentPlaying.get_mSongTitle());
        artistName.setText(currentPlaying.get_mArtistName());

        Bitmap art = Utils.decodeSampledBitmapFromResource(
                Utils.getByteArrayFrom(requireContext(),currentPlaying),
                150, 150
        );
        // Default artwork
        if (art == null) {
            art = BitmapFactory.decodeResource(getResources(), R.drawable.music_icon_big);
        }
        art = Bitmap.createScaledBitmap(art, 150, 150, false);
        smallIconImage.setImageBitmap(art);

        Blurry.with(requireContext())
                .from(art)
                .into(backgroundImage);
        rowLayout.setVisibility(View.VISIBLE);
        rowLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        if (itemId == R.id.bottom_row) {
            ViewPagerFragment.goToDetail();
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //navController = Navigation.findNavController(view);
    }

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

        private final List<SongModel> mSongs;

        public SongsAdapter(List<SongModel> songs) {
            this.mSongs = songs;
        }

        @NonNull
        @Override
        public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.recyclerview_item,
                    parent, false);
            return new SongHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SongHolder holder, int position) {
            holder.get_mSongTitle().setText(mSongs.get(position).get_mSongTitle());
            holder.get_mSongTitle().setSelected(true);
            holder.get_mSongArtistNameTextView().setText(mSongs.get(position).get_mArtistName());
            holder.set_mSongPosition(position);

            processBitmapInBackground(holder);
        }

        private void processBitmapInBackground(SongHolder holder) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                int position1 = holder.getAdapterPosition();
                Bitmap art = getBitmapFromMemCache(mSongs.get(position1).get_mSongTitle());
                if (art == null) {
                    art = Utils.decodeSampledBitmapFromResource(
                            Utils.getByteArrayFrom(requireContext(), mSongs.get(position1)),
                            150, 150
                    );
                    // Default artwork
                    if (art == null) {
                        art = BitmapFactory.decodeResource(getResources(), R.drawable.music_icon_big);
                    }
                    addBitmapToMemoryCache(mSongs.get(position1).get_mSongTitle(), art);
                }

                if (art != null) {
                    art = Bitmap.createScaledBitmap(art, 150, 150, false);
                    setArtFromBackgroundThread(holder, art);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSongs.size();
        }

        private void setArtFromBackgroundThread(SongHolder holder, Bitmap art) {
            requireActivity().runOnUiThread(() -> {
                holder.get_mSmallSongIcon().setImageBitmap(art);

                Blurry.with(requireContext())
                        .from(art)
                        .into(holder.get_mItemImageBackground());
            });
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitleTextView;
            private final TextView mSongArtistNameTextView;
            private final ImageView mSmallSongIcon;
            private final ImageView mItemImageBackground;
            private Integer mSongPosition;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitleTextView = itemView.findViewById(R.id.songTitle);
                mSongArtistNameTextView = itemView.findViewById(R.id.songArtist);
                mSmallSongIcon = itemView.findViewById(R.id.small_song_icon);
                mItemImageBackground = itemView.findViewById(R.id.item_background_image);
                ConstraintLayout mRowLayout = itemView.findViewById(R.id.row_constraint);
                mRowLayout.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                mSharedData.set_mPlayingNowIndex(get_mSongPosition());
                ViewPagerFragment.goToDetail();
            }

            public TextView get_mSongTitle() {
                return mSongTitleTextView;
            }

            public ImageView get_mItemImageBackground() {
                return mItemImageBackground;
            }

            public TextView get_mSongArtistNameTextView() {
                return mSongArtistNameTextView;
            }

            public ImageView get_mSmallSongIcon() {
                return mSmallSongIcon;
            }

            public Integer get_mSongPosition() {
                return mSongPosition;
            }

            public void set_mSongPosition(Integer mSongPosition) {
                this.mSongPosition = mSongPosition;
            }
        }
    }
}
