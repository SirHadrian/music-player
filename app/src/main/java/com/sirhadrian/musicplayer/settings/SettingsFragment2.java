package com.sirhadrian.musicplayer.settings;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.sirhadrian.musicplayer.MainActivity;
import com.sirhadrian.musicplayer.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingsFragment2 extends PreferenceFragmentCompat {

    private final String mCacheFile = "resultObject";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SettingsViewModel settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        Preference button = findPreference(getString(R.string.scan_button));

        try {
            File.createTempFile(mCacheFile, null, requireContext().getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File cacheFile = new File(requireActivity().getCacheDir(), mCacheFile);

        Uri res = readResultObjectFromFile(cacheFile);
        if (res != null) {
            settingsViewModel.set_mDirPath(res);
        }

        ActivityResultLauncher<Uri> pathLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                result -> {
                    settingsViewModel.set_mDirPath(result);
                    writeResultObjectToFile(result, cacheFile);
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
        MainActivity activity = (MainActivity) requireActivity();
        activity.showUpButton();
    }

    private Uri readResultObjectFromFile(File file) {
        FileInputStream fis;
        Uri result = null;
        try {
            fis = requireContext().openFileInput(mCacheFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(fis);
            result = (Uri) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void writeResultObjectToFile(Uri res, File file) {
        FileOutputStream fos;
        try {
            fos = requireContext().openFileOutput(mCacheFile, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}