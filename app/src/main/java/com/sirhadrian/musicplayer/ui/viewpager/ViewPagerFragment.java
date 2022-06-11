package com.sirhadrian.musicplayer.ui.viewpager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.sirhadrian.musicplayer.databinding.FragmentViewpagerBinding;
import com.sirhadrian.musicplayer.ui.SongDetailFragment;
import com.sirhadrian.musicplayer.ui.SongsListFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragment extends Fragment {

    private List<Fragment> mViewPagerFragments;
    private FragmentStateAdapter mFragmentStateAdapter;
    private ViewPager2 mFragmentViewPager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentViewpagerBinding binding = FragmentViewpagerBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        mFragmentViewPager = binding.fragmentViewPager2;
        mViewPagerFragments = new ArrayList<>();

        Fragment songsListFragment = new SongsListFragment(mFragmentViewPager);
        Fragment songDetailFragment = new SongDetailFragment();

        mViewPagerFragments.add(songsListFragment);
        mViewPagerFragments.add(songDetailFragment);

        mFragmentStateAdapter = new FragmentPageAdapter(requireActivity(), mViewPagerFragments);
        mFragmentViewPager.setAdapter(mFragmentStateAdapter);

        return root;
    }
}