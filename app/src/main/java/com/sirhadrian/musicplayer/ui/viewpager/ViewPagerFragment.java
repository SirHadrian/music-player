package com.sirhadrian.musicplayer.ui.viewpager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.sirhadrian.musicplayer.databinding.FragmentViewpagerBinding;
import com.sirhadrian.musicplayer.ui.SongDetailFragment;
import com.sirhadrian.musicplayer.ui.SongsListFragment;
import com.sirhadrian.musicplayer.ui.SongsListViewModel;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragment extends Fragment {

    private List<Fragment> mViewPagerFragments;
    private FragmentStateAdapter mFragmentStateAdapter;
    private static ViewPager2 mFragmentViewPager;

    private SongsListViewModel mSongsListVM;

    private static boolean settingsOpen = false;

    private NavController mNavCtrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentViewpagerBinding binding = FragmentViewpagerBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        mSongsListVM = new ViewModelProvider(requireActivity()).get(SongsListViewModel.class);

        mFragmentViewPager = binding.fragmentViewPager2;
        mViewPagerFragments = new ArrayList<>();

        Fragment songsListFragment = new SongsListFragment();
        Fragment songDetailFragment = new SongDetailFragment();

        mViewPagerFragments.add(songsListFragment);
        mViewPagerFragments.add(songDetailFragment);

        mFragmentStateAdapter = new FragmentPageAdapter(requireActivity(), mViewPagerFragments);
        mFragmentViewPager.setAdapter(mFragmentStateAdapter);

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavCtrl = Navigation.findNavController(view);
        mSongsListVM.set_mNavCtrl(mNavCtrl);
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
        if (settingsOpen) {
            settingsOpen = false;
            return true;
        }
        mFragmentViewPager.setCurrentItem(mFragmentViewPager.getCurrentItem() - 1);
        return false;
    }

    public static void set_SettingsOpen(boolean settingsOpen) {
        ViewPagerFragment.settingsOpen = settingsOpen;
    }
}