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
    // Static reference so can be used with the static methods
    private static ViewPager2 mFragmentViewPager;
    // Used to get the nav Control in child fragment
    private SongsListViewModel mSongsListVM;
    // For easy app navigation
    private NavController mNavCtrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflating PageViewer2
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Navigation control can only be found after the view is created
        mNavCtrl = Navigation.findNavController(view);
        mSongsListVM.set_mNavCtrl(mNavCtrl);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // User in SongsListFragment to open the detail view on item click
    public static void goToDetail() {
        if (mFragmentViewPager != null) {
            mFragmentViewPager.setCurrentItem(mFragmentViewPager.getCurrentItem() + 1);
        }
    }

    // Used in MainActivity to exit the app or go back to list view
    public static boolean isLastItem() {
        if (mFragmentViewPager.getCurrentItem() == 0) {
            return true;
        }
        mFragmentViewPager.setCurrentItem(mFragmentViewPager.getCurrentItem() - 1);
        return false;
    }
}