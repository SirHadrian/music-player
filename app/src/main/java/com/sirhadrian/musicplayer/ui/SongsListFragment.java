package com.sirhadrian.musicplayer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentSongsListBinding;

import java.util.ArrayList;
import java.util.List;

public class SongsListFragment extends Fragment {
    private List<String> tempSongs;

    private RecyclerView mRecyclerView;
    private SongsAdapter mSongsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentSongsListBinding binding = FragmentSongsListBinding.inflate(inflater, container,
                false);

        View view = binding.getRoot();

        mRecyclerView=binding.fragmentSongsListRecyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tempSongs = new ArrayList<>();
        for (int i = 0; i < 30; ++i) {
            tempSongs.add("Song number #"+ (i+1));
        }

        if (mSongsAdapter == null) {
            mSongsAdapter = new SongsAdapter(tempSongs);
        }
        mRecyclerView.setAdapter(mSongsAdapter);

        return view;
    }

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

        private List<String> mSongsTitles;

        public SongsAdapter(List<String> songs) {
            mSongsTitles = songs;
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
            holder.get_mSongTitle().setText(mSongsTitles.get(position));
        }

        @Override
        public int getItemCount() {
            return mSongsTitles.size();
        }


        private class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mSongTitle;

            public SongHolder(@NonNull View itemView) {
                super(itemView);
                mSongTitle = itemView.findViewById(R.id.songTitle);
                mSongTitle.setOnClickListener(this);
            }

            public TextView get_mSongTitle() {
                return mSongTitle;
            }

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        get_mSongTitle().getText().toString() + " Clicked!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
