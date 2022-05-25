package com.sirhadrian.musicplayer;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.databinding.FragmentHolderBinding;
import com.sirhadrian.musicplayer.model.SongModel;
import com.sirhadrian.musicplayer.settings.SettingsFragment;
import com.sirhadrian.musicplayer.settings.SettingsViewModel;
import com.sirhadrian.musicplayer.ui.SongsListViewModel;
import com.sirhadrian.musicplayer.ui.viewpager.ViewPagerFragment;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Uri searchFolder;

    private SettingsViewModel settingsViewModel;
    private SongsListViewModel songsListViewModel;

    private List<SongModel> mSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentHolderBinding binding = FragmentHolderBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        songsListViewModel = new ViewModelProvider(this).get(SongsListViewModel.class);

        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.get_mDirPath().observe(this, s -> searchFolder = s);

        Toolbar toolbar = binding.myToolbar;
        toolbar.setOnMenuItemClickListener(item -> {
            int actionId = item.getItemId();

            if (actionId == R.id.settings) {
                openSettingsFragment();
            } else if (actionId == R.id.scan) {
                if (searchFolder == null) return false;

                makeScanRequest(searchFolder, result -> {
                    if (result instanceof Result.Success) {
                        mSongs = ((Result.Success<List<SongModel>>) result).get_Data();
                        songsListViewModel.set_value_in_worker_thread(mSongs);
                    } else if (result instanceof Result.Error) {
                        ((Result.Error<List<SongModel>>) result).exception.printStackTrace();
                    }
                });
            } else return false;

            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_holder, new ViewPagerFragment())
                    .commit();
        }
    }

    private void makeScanRequest(Uri folder, ResultCallback<List<SongModel>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Result<List<SongModel>> result = new Result.Success<>(Query.getSongsFromFolder(this, folder));
                callback.onComplete(result);
            } catch (Exception e) {
                Result<List<SongModel>> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    private void openSettingsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_settings);
        if (fragment == null) {
            fragment = new SettingsFragment();
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .add(R.id.fragment_holder, fragment)
                    .commit();
        }
    }
}