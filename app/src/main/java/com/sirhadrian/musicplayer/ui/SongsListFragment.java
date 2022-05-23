package com.sirhadrian.musicplayer.ui;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongsListBinding;
import com.sirhadrian.musicplayer.model.SongModel;
import com.sirhadrian.musicplayer.settings.SettingsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SongsListFragment extends Fragment {

    private List<SongModel> mSongsList;

    private SharedDataViewModel mSharedData;
    private SettingsViewModel mSettings;
    private SongsListViewModel mSongsListObserved;

    private RecyclerView mRecyclerView;
    private SongsAdapter mSongsAdapter;
    private final ViewPager2 viewPager2Activity;

    private Uri searchFolder;


    public SongsListFragment(ViewPager2 viewPager) {
        this.viewPager2Activity = viewPager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container,
                false);

        View view = binding.getRoot();

        mSongsList = new ArrayList<>();


        mSettings = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        mSettings.get_mDirPath().observe(getViewLifecycleOwner(), s -> searchFolder = s);

        mRecyclerView = binding.fragmentSongsListRecyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSongsAdapter = new SongsAdapter(mSongsList);
        mRecyclerView.setAdapter(mSongsAdapter);


        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mSongsListObserved = new ViewModelProvider(requireActivity()).get(SongsListViewModel.class);

        mSongsListObserved.get_mSongsList().observe(getViewLifecycleOwner(), songModels -> {
            mSongsList.clear();
            mSongsList.addAll(songModels);
            mSongsAdapter.notifyDataSetChanged();
        });

        //test



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Set<String> mediaVolumes = MediaStore.getExternalVolumeNames(requireActivity());

            Log.d("vol", mediaVolumes.toString());
        }

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
            View view = LayoutInflater.from(getContext()).inflate(R.layout.song_item,
                    parent, false);
            return new SongHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SongHolder holder, int position) {
            holder.get_mSongModel().set_mSongTitle(mSongsList.get(position).get_mSongTitle());
            holder.get_mSongModel().set_mSongUri(mSongsTitles.get(position).get_mSongUri());

            holder.get_mSongTitle().setText(mSongsTitles.get(position).get_mSongTitle());
        }

        @Override
        public int getItemCount() {
            return mSongsTitles.size();
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitleTextView;
            private SongModel mSongModel;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitleTextView = itemView.findViewById(R.id.songTitle);
                mSongTitleTextView.setOnClickListener(this);
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
