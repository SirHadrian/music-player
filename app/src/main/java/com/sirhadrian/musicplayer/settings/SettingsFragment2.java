package com.sirhadrian.musicplayer.settings;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.sirhadrian.musicplayer.MainActivity;
import com.sirhadrian.musicplayer.R;

public class SettingsFragment2 extends PreferenceFragmentCompat {

    private final int mRequestCodeOpenDir = 99919;

    private Preference button;
    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        button = findPreference(getString(R.string.scan_button));

        assert button != null;
        button.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(Intent.createChooser(intent, "Choose directory"), mRequestCodeOpenDir);

            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == mRequestCodeOpenDir && resultCode == RESULT_OK) {
            assert data != null;
            Uri path = data.getData();
            button.setSummary(path.toString());
            settingsViewModel.set_mDirPath(path);
            Log.d("geturi", path + "   --- " + path.getPath());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) requireActivity();
        activity.showUpButton();
    }
}