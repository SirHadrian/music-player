package com.sirhadrian.musicplayer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongsListBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.ui.viewpager.ViewPagerFragment;
import com.sirhadrian.musicplayer.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.wasabeef.blurry.Blurry;

public class SongsListFragment extends Fragment implements View.OnClickListener {

    private List<SongModel> mSongsList;
    private ArrayList<SongModel> mSongsAlwaysFull;

    private SharedDataViewModel mSharedData;
    private SongsListViewModel mSongsListVM;

    private SongsAdapter mSongsAdapter;
    private LruCache<String, Bitmap> memoryCache;

    //Bottom view
    private ImageView backgroundImage;
    private ImageView smallIconImage;
    private TextView songTitle;
    private TextView artistName;
    private ConstraintLayout rowLayout;
    private TextView mIndexAndTotal;

    // FABs
    private FloatingActionButton mMasterSwitch;
    private FloatingActionButton mFabSettings;
    private FloatingActionButton mFabShuffle;
    private FloatingActionButton mFabSearch;
    private boolean editTextOpen = false;
    private EditText mSearchBox;
    private boolean isFABOpen;

    private String mLastFindPattern;

    private NavController mNavCtrl;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mSongsListVM = new ViewModelProvider(requireActivity()).get(SongsListViewModel.class);

        backgroundImage = binding.itemBackgroundImage;
        smallIconImage = binding.smallSongIcon;
        songTitle = binding.songTitle;
        artistName = binding.songArtist;
        rowLayout = binding.bottomRow;
        mIndexAndTotal = binding.indexAndTotal;

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

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        mMasterSwitch = binding.masterSwitch;
        mFabSettings = binding.fabSettings;
        mFabShuffle = binding.fabShuffleSongs;
        mFabSearch = binding.fabSearchSong;
        mSearchBox = binding.searchEditText;

        mFabShuffle.setOnClickListener(view -> {
            ArrayList<SongModel> mSongs = mSharedData.get_mSongsList().getValue();
            if (mSongs != null) {
                Collections.shuffle(mSongs);
                mSharedData.loadSongs(mSongs);
            }
        });

        mFabSettings.setOnClickListener(view -> {
            if (mNavCtrl == null) return;
            mNavCtrl.navigate(R.id.action_viewPagerFragment_to_settingsFragment2);
        });

        mMasterSwitch.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();

            } else {
                closeFABMenu();
            }
        });

        mFabSearch.setOnClickListener(view -> {
            if (editTextOpen) {
                closeEditBox();
            } else {
                mFabSearch.setImageResource(R.drawable.ic_baseline_close_24);
                mSongsAlwaysFull = new ArrayList<>(mSongsList);
                mSearchBox.setVisibility(View.VISIBLE);

                mSearchBox.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);

                editTextOpen = true;
            }
        });

        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mLastFindPattern = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int
                    i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() < mLastFindPattern.length()) {
                    mSharedData.loadSongs(mSongsAlwaysFull);
                } else if (editable.toString().length() > mLastFindPattern.length()) {
                    filter(editable.toString());
                }
            }
        });

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (editTextOpen) {
            closeEditBox();
        }
        if (isFABOpen) {
            closeFABMenu();
        }
    }

    private void closeEditBox() {
        mSearchBox.setText("");
        mFabSearch.setImageResource(R.drawable.ic_baseline_search_24);
        mSearchBox.clearFocus();
        mSearchBox.setVisibility(View.GONE);
        editTextOpen = false;
    }

    private void filter(String search) {
        ArrayList<SongModel> filteredSongs = new ArrayList<>();

        for (SongModel song : mSongsList) {
            if (song.get_mSongTitle().toLowerCase().trim().contains(search.toLowerCase().trim())) {
                filteredSongs.add(song);
            }
        }
        mSharedData.loadSongs(filteredSongs);
    }

    private void closeFABMenu() {
        mMasterSwitch.setImageResource(R.drawable.ic_baseline_expand_more_24);
        isFABOpen = false;
        mFabSettings.animate().translationY(0);
        mFabShuffle.animate().translationY(0);
        mFabSearch.animate().translationY(0);
    }

    private void showFABMenu() {
        mMasterSwitch.setImageResource(R.drawable.ic_baseline_expand_less_24);
        isFABOpen = true;
        int base = 100;
        mFabSettings.animate().translationY(base);
        mFabShuffle.animate().translationY(base * 2);
        mFabSearch.animate().translationY(base * 3);
    }

    private void displayPlayingNowIndexAtBottom(Integer position) {
        SongModel currentPlaying = mSongsList.get(position);

        mIndexAndTotal.setText(String.format("%s/%s", position + 1, mSongsList.size()));

        songTitle.setText(currentPlaying.get_mSongTitle());
        artistName.setText(currentPlaying.get_mArtistName());

        Bitmap art = Utils.decodeSampledBitmapFromResource(
                Utils.getByteArrayFrom(requireContext(), currentPlaying),
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
        mNavCtrl = mSongsListVM.get_mNavCtrl();
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
            holder.get_mSongDuration().setText(Utils.convertToMMSS(String.valueOf(mSongs.get(position).get_mSongDuration())));

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
            private final TextView mSongTitle;
            private final TextView mSongArtistName;
            private final TextView mSongDuration;
            private final ImageView mSmallSongIcon;
            private final ImageView mItemImageBackground;
            private Integer mSongPosition;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitle = itemView.findViewById(R.id.songTitle);
                mSongArtistName = itemView.findViewById(R.id.songArtist);
                mSmallSongIcon = itemView.findViewById(R.id.small_song_icon);
                mItemImageBackground = itemView.findViewById(R.id.item_background_image);
                mSongDuration = itemView.findViewById(R.id.song_duration);
                ConstraintLayout mRowLayout = itemView.findViewById(R.id.row_constraint);
                mRowLayout.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                mSharedData.set_mPlayingNowIndex(get_mSongPosition());
                ViewPagerFragment.goToDetail();
            }

            public TextView get_mSongTitle() {
                return mSongTitle;
            }

            public TextView get_mSongDuration() {
                return mSongDuration;
            }

            public ImageView get_mItemImageBackground() {
                return mItemImageBackground;
            }

            public TextView get_mSongArtistNameTextView() {
                return mSongArtistName;
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
