package com.sirhadrian.musicplayer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.sirhadrian.musicplayer.databinding.FragmentViewpagerBinding;
import com.sirhadrian.musicplayer.ui.SongDetailFragment;
import com.sirhadrian.musicplayer.ui.SongsListFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Fragment> mViewPagerFragments;
    private FragmentStateAdapter mFragmentStateAdapter;
    private ViewPager2 mFragmentViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentViewpagerBinding binding = FragmentViewpagerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mFragmentViewPager = binding.fragmentViewPager2;

        mViewPagerFragments = new ArrayList<>();

        Fragment songsListFragment = new SongsListFragment(mFragmentViewPager);
        Fragment songDetailFragment = new SongDetailFragment();

        mViewPagerFragments.add(songsListFragment);
        mViewPagerFragments.add(songDetailFragment);

        mFragmentStateAdapter = new FragmentPageAdapter(this, mViewPagerFragments);
        mFragmentViewPager.setAdapter(mFragmentStateAdapter);

    }
}