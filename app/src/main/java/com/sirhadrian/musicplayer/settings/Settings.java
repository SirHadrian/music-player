package com.sirhadrian.musicplayer.settings;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sirhadrian.musicplayer.databinding.ActivitySettingsBinding;

import java.io.File;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        TextView dirPath = binding.textViewDirPath;
        Button getDirPath = binding.buttonStartIntent;
        Toolbar toolbar = binding.settingsToolbar;

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);

        ActivityResultLauncher<Uri> pathLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                result -> dirPath.setText(result.toString()));

        getDirPath.setOnClickListener(view -> pathLauncher.launch(Uri.fromFile(new File("/storage"))));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}