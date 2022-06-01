package com.sirhadrian.musicplayer.settings;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingsFragment2 extends PreferenceFragmentCompat {

    private final String mCacheFile = "resultObject";
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
            //intent.setDataAndType(Uri.parse(path), "*/*");
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