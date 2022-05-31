package com.sirhadrian.musicplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sirhadrian.musicplayer.databinding.FragmentHolderBinding;
import com.sirhadrian.musicplayer.model.database.SongDao;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.model.database.SongsDatabase;
import com.sirhadrian.musicplayer.settings.SettingsFragment2;
import com.sirhadrian.musicplayer.settings.SettingsViewModel;
import com.sirhadrian.musicplayer.ui.SongsListViewModel;
import com.sirhadrian.musicplayer.ui.viewpager.ViewPagerFragment;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Uri searchFolder;

    private SongsListViewModel songsListViewModel;

    private ActionBar actionBar;

    private SongDao songDao;
    private SongsDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentHolderBinding binding = FragmentHolderBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        songsListViewModel = new ViewModelProvider(this).get(SongsListViewModel.class);

        //SharedPreferences preferences = getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        //mSongs = readCacheSongs(preferences);

        //test
        /*
        database = Room.databaseBuilder(
                        getApplicationContext(),
                        SongsDatabase.class,
                        "db-songs")
                .allowMainThreadQueries()
                .build();
        songDao = database.songDao();
        songsListViewModel.set_mSongList(songDao.getAll());
*/

        SettingsViewModel settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.get_mDirPath().observe(this, s -> searchFolder = s);

        Toolbar toolbar = binding.myToolbar;
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        toolbar.setOnMenuItemClickListener(item -> {
            int actionId = item.getItemId();

            if (actionId == R.id.settings) {
                openSettingsFragment();
            } else if (actionId == R.id.scan) {
                if (searchFolder == null) return false;

                makeScanRequest(searchFolder, result -> {
                    if (result instanceof Result.Success) {
                        List<SongModel> songs = ((Result.Success<List<SongModel>>) result).get_Data();
                        //writeCacheSongs(mSongs, preferences);
                        songsListViewModel.set_value_in_worker_thread(songs);
                        //songDao.insertAll(songs);
                    } else if (result instanceof Result.Error) {
                        ((Result.Error<List<SongModel>>) result).exception.printStackTrace();
                    }
                });
            }

            /*else if (actionId == R.id.home) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                    return true;
                }
                return false;*/

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
        if (fragmentManager.getBackStackEntryCount() > 0) return;
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .add(R.id.fragment_holder, new SettingsFragment2())
                .commit();
    }

    private void writeCacheSongs(List<SongModel> songs, SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String jsonString = gson.toJson(songs);

        editor.putString(getString(R.string.songs_key_serial), jsonString);
        editor.apply();
    }

    private ArrayList<SongModel> readCacheSongs(SharedPreferences preferences) {
        Gson gson = new Gson();
        String jsonString = preferences.getString(getString(R.string.songs_key_serial), null);
        if (jsonString != null) {
            Type collectionType = new TypeToken<ArrayList<SongModel>>() {
            }.getType();
            return gson.fromJson(jsonString, collectionType);
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }


    public void showUpButton() {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void hideUpButton() {
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
            hideUpButton();
            return true;
        } else {
            super.onSupportNavigateUp();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}