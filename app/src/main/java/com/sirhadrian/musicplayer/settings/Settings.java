package com.sirhadrian.musicplayer.settings;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sirhadrian.musicplayer.databinding.ActivitySettingsBinding;

import java.io.File;

public class Settings extends AppCompatActivity {

    public static final String uriKey = "dirUri";
    SharedPreferences settings;
    TextView dirPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);



        dirPath = binding.textViewDirPath;
        Button getDirPath = binding.buttonStartIntent;
        Toolbar toolbar = binding.settingsToolbar;

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);

        ActivityResultLauncher<Uri> pathLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                result -> dirPath.setText(result.toString())
        );

        getDirPath.setOnClickListener(view -> pathLauncher.launch(Uri.fromFile(new File("/storage/0/"))));
    }

    @Override
    protected void onStart() {
        super.onStart();
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (settings != null) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(uriKey, dirPath.getText().toString());
            editor.apply();
        }
    }
}