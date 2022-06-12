package com.sirhadrian.musicplayer.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

public class SongsListFragment extends Fragment {

    private List<SongModel> mSongsList;

    private SharedDataViewModel mSharedData;

    private SongsAdapter mSongsAdapter;
    //private NavController navController;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

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

        return view;
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
            holder.get_mSongArtistNameTextView().setText(mSongs.get(position).get_mArtistName());
            holder.set_mSongPosition(position);

            if (position % 2 == 0) {
                holder.get_mRowLayout().setBackgroundColor(getResources().getColor(R.color.bg_dark));
            } else {
                holder.get_mRowLayout().setBackgroundColor(getResources().getColor(R.color.bg_light));
            }
        }

        @Override
        public int getItemCount() {
            return mSongs.size();
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitleTextView;
            private final TextView mSongArtistNameTextView;
            private final ConstraintLayout mRowLayout;
            private Integer mSongPosition;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitleTextView = itemView.findViewById(R.id.songTitle);
                mSongArtistNameTextView = itemView.findViewById(R.id.songArtist);
                mRowLayout = itemView.findViewById(R.id.row_constraint);
                mSongTitleTextView.setOnClickListener(this);
                mSongTitleTextView.setSelected(true);
            }


            public TextView get_mSongTitle() {
                return mSongTitleTextView;
            }

            public TextView get_mSongArtistNameTextView() {
                return mSongArtistNameTextView;
            }

            public Integer get_mSongPosition() {
                return mSongPosition;
            }

            public void set_mSongPosition(Integer mSongPosition) {
                this.mSongPosition = mSongPosition;
            }


            public ConstraintLayout get_mRowLayout() {
                return mRowLayout;
            }

            @Override
            public void onClick(View view) {
                mSharedData.set_mPlayingNowIndex(get_mSongPosition());
                //navController.navigate(R.id.action_songsListFragment_to_songDetailFragment);
                ViewPagerFragment.goToDetail();
            }
        }
    }
}
