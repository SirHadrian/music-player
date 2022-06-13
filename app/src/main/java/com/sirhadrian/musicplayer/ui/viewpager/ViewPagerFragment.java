package com.sirhadrian.musicplayer.ui.viewpager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.databinding.FragmentViewpagerBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.ui.SongDetailFragment;
import com.sirhadrian.musicplayer.ui.SongsListFragment;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewPagerFragment extends Fragment {

    private List<Fragment> mViewPagerFragments;
    private FragmentStateAdapter mFragmentStateAdapter;
    private static ViewPager2 mFragmentViewPager;

    // FABs
    private FloatingActionButton mMasterSwitch;
    private FloatingActionButton mFabSettings;
    private boolean isFABOpen;

    private NavController mNavCtrl;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentViewpagerBinding binding = FragmentViewpagerBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        mFragmentViewPager = binding.fragmentViewPager2;
        mViewPagerFragments = new ArrayList<>();

        Fragment songsListFragment = new SongsListFragment();
        Fragment songDetailFragment = new SongDetailFragment();

        mViewPagerFragments.add(songsListFragment);
        mViewPagerFragments.add(songDetailFragment);

        mFragmentStateAdapter = new FragmentPageAdapter(requireActivity(), mViewPagerFragments);
        mFragmentViewPager.setAdapter(mFragmentStateAdapter);


        mMasterSwitch = binding.masterSwitch;
        mFabSettings = binding.fabSettings;

        mFabSettings.setOnClickListener(view -> mNavCtrl.navigate(R.id.action_viewPagerFragment_to_settingsFragment2));

        mMasterSwitch.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();
                mMasterSwitch.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                closeFABMenu();
                mMasterSwitch.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        isFABOpen = false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavCtrl = Navigation.findNavController(view);
    }

    private void closeFABMenu() {
        isFABOpen = false;
        mFabSettings.animate().translationY(0);
    }

    private void showFABMenu() {
        isFABOpen = true;
        int base = 140;
        mFabSettings.animate().translationY(base);
    }

    public static void goToDetail() {
        if (mFragmentViewPager != null) {
            mFragmentViewPager.setCurrentItem(mFragmentViewPager.getCurrentItem() + 1);
        }
    }

    public static boolean isLastItem() {
        if (mFragmentViewPager.getCurrentItem() == 0) {
            return true;
        }
        mFragmentViewPager.setCurrentItem(mFragmentViewPager.getCurrentItem() - 1);
        return false;
    }
}