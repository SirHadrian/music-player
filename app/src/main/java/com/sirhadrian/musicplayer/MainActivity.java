package com.sirhadrian.musicplayer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sirhadrian.musicplayer.databinding.FragmentHolderBinding;
import com.sirhadrian.musicplayer.ui.viewpager.ViewPagerFragment;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentHolderBinding binding = FragmentHolderBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_holder);
        if (fragment == null) {
            fragment = new ViewPagerFragment();
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_holder, fragment)
                    .commit();
        }
    }
}