package com.sirhadrian.musicplayer.settings;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.sirhadrian.musicplayer.R;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.ui.SharedDataViewModel;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment2 extends PreferenceFragmentCompat {

    private final int mRequestCodeOpenDir = 99919;

    private Preference mScanDirectory;
    private Preference mScanButton;

    private SettingsViewModel settingsViewModel;
    private SharedDataViewModel mSharedData;

    private Uri mFolder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        mSharedData = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);

        // Action Buttons
        mScanDirectory = findPreference(getString(R.string.scan_directory));
        mScanButton = findPreference(getString(R.string.scan_button));

        assert mScanDirectory != null;
        mScanDirectory.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(Intent.createChooser(intent, "Choose directory"), mRequestCodeOpenDir);

            return true;
        });
        assert mScanButton != null;
        mScanButton.setOnPreferenceClickListener(preference -> {
            if (mFolder == null) return false;
            makeScanRequest(requireContext(),
                    mFolder,
                    result -> {
                        if (result instanceof Result.Success) {
                            ArrayList<SongModel> songs = ((Result.Success<ArrayList<SongModel>>) result).get_Data();
                            mSharedData.set_value_in_worker_thread(songs);
                        } else if (result instanceof Result.Error) {
                            ((Result.Error<ArrayList<SongModel>>) result).exception.printStackTrace();
                        }
                    }
            );
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == mRequestCodeOpenDir && resultCode == RESULT_OK) {
            assert data != null;
            Uri path = data.getData();
            mScanDirectory.setSummary(path.toString());
            mFolder = path;
            settingsViewModel.set_mDirPath(mFolder);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void makeScanRequest(Context context, Uri folder, ResultCallback<ArrayList<SongModel>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Result<ArrayList<SongModel>> result = new Result.Success<>(Query.getSongsFromFolder(context, folder));
                callback.onComplete(result);
            } catch (Exception e) {
                Result<ArrayList<SongModel>> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }
}