package com.sirhadrian.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.sirhadrian.musicplayer.databinding.FragmentHolderBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentHolderBinding binding = FragmentHolderBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }
}