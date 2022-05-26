package com.sirhadrian.musicplayer.settings;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.databinding.FragmentSettingsBinding;

import java.io.File;

public class SettingsFragment extends Fragment {

    TextView dirPath;
    SettingsViewModel settingsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        dirPath = binding.textViewDirPath;
        Button getDirPath = binding.buttonStartIntent;

        ActivityResultLauncher<Uri> pathLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                result -> {
                    dirPath.setText(result.toString());
                    settingsViewModel.set_mDirPath(result);
                }
        );

        getDirPath.setOnClickListener(view -> pathLauncher.launch(Uri.fromFile(new File("/storage"))));

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
