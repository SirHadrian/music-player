package com.sirhadrian.musicplayer.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongsListBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;

import java.util.ArrayList;
import java.util.List;

public class SongsListFragment extends Fragment {

    private List<SongModel> mSongsList;

    private SharedDataViewModel mSharedData;

    private SongsAdapter mSongsAdapter;
    private final ViewPager2 viewPager2Activity;


    public SongsListFragment(ViewPager2 viewPager) {
        this.viewPager2Activity = viewPager;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container,
                false);

        View view = binding.getRoot();

        mSongsList = new ArrayList<>();


        RecyclerView mRecyclerView = binding.fragmentSongsListRecyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSongsAdapter = new SongsAdapter(mSongsList);
        mRecyclerView.setAdapter(mSongsAdapter);


        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        SongsListViewModel mSongsListObserved = new ViewModelProvider(requireActivity()).get(SongsListViewModel.class);

        mSongsListObserved.get_mSongsList().observe(getViewLifecycleOwner(), songModels -> {
            mSongsList.clear();
            mSongsList.addAll(songModels);
            mSongsAdapter.notifyDataSetChanged();
        });

        return view;
    }

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

        private final List<SongModel> mSongsTitles;

        public SongsAdapter(List<SongModel> songs) {
            this.mSongsTitles = songs;
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
            holder.get_mSongModel().set_mSongTitle(mSongsList.get(position).get_mSongTitle());
            holder.get_mSongModel().set_mSongUri(mSongsTitles.get(position).get_mSongUri());

            holder.get_mSongTitle().setText(mSongsTitles.get(position).get_mSongTitle());

            if (position % 2 == 0) {
                holder.get_mRowLayout().setBackgroundColor(getResources().getColor(R.color.bg_dark));
            }else
            {
                holder.get_mRowLayout().setBackgroundColor(getResources().getColor(R.color.bg_light));
            }
        }

        @Override
        public int getItemCount() {
            return mSongsTitles.size();
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitleTextView;
            private SongModel mSongModel;
            private ConstraintLayout mRowLayout;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitleTextView = itemView.findViewById(R.id.songTitle);
                mRowLayout = itemView.findViewById(R.id.row_constraint);
                mSongTitleTextView.setOnClickListener(this);
                mSongTitleTextView.setSelected(true);
            }

            public SongModel get_mSongModel() {
                if (mSongModel == null) {
                    mSongModel = new SongModel();
                }
                return mSongModel;
            }

            public TextView get_mSongTitle() {
                return mSongTitleTextView;
            }

            public ConstraintLayout get_mRowLayout() {
                return mRowLayout;
            }

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), mSongModel.get_mSongTitle() + " Clicked!",
                        Toast.LENGTH_SHORT).show();

                mSharedData.select(get_mSongModel());

                viewPager2Activity.setCurrentItem(viewPager2Activity.getCurrentItem() + 1);
            }
        }
    }
}
