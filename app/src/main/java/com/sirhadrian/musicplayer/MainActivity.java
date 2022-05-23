package com.sirhadrian.musicplayer;

import android.os.Bundle;
import android.util.Log;
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

    private String searchFolder;

    private SettingsViewModel settingsViewModel;
    private SongsListViewModel songsListViewModel;

    private List<SongModel> temp;


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
            switch (item.getItemId()) {
                case R.id.settings:
                    openSettingsFragment();
                    break;

                case R.id.scan:
                    /*
                    makeScanRequest(searchFolder, result -> {
                        if (result instanceof Result.Success) {
                            temp = ((Result.Success<List<SongModel>>) result).get_Data();
                        } else if (result instanceof Result.Error) {
                            ((Result.Error<List<SongModel>>) result).exception.printStackTrace();
                        }
                    });
                    */
                    temp=Query.getAllAudioFromDevice(this);
                    songsListViewModel.set_mSongList(temp);

                    Log.d("scan", "End Scan");
                    break;

                default:
                    return false;
            }

            return true;
        });

        if (savedInstanceState == null) {
            Fragment fragment = new ViewPagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_holder, fragment)
                    .commit();
        }
    }

    private void makeScanRequest(String folder, ResultCallback<List<SongModel>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Result<List<SongModel>> result = new Result.Success<>(Query.getAllAudioFromDevice(this));
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
        } else {
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_holder, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}