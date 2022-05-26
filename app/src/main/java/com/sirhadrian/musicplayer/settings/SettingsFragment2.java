package com.sirhadrian.musicplayer.settings;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.sirhadrian.musicplayer.MainActivity;
import com.sirhadrian.musicplayer.R;

import java.io.File;

public class SettingsFragment2 extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SettingsViewModel settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        Preference button = findPreference(getString(R.string.scan_button));

        ActivityResultLauncher<Uri> pathLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                result -> {
                    settingsViewModel.set_mDirPath(result);
                    assert button != null;
                    button.setSummary(result.toString());
                }
        );




        assert button != null;
        button.setOnPreferenceClickListener(preference -> {
            pathLauncher.launch(Uri.fromFile(new File("/storage")));
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity)requireActivity();
        activity.showUpButton();
    }
}