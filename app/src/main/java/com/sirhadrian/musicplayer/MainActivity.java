package com.sirhadrian.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.databinding.FragmentHolderBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.settings.SettingsFragment2;
import com.sirhadrian.musicplayer.settings.SettingsViewModel;
import com.sirhadrian.musicplayer.ui.SharedDataViewModel;
import com.sirhadrian.musicplayer.ui.viewpager.ViewPagerFragment;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Uri searchFolder;

    private SharedDataViewModel mSharedData;

    private ActionBar actionBar;

    private ActivityResultLauncher<String> permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentHolderBinding binding = FragmentHolderBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        mSharedData = new ViewModelProvider(this).get(SharedDataViewModel.class);

        permission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                mSharedData.loadSongs(Query.getAllAudioFromDevice(this, null));
            } else {
                respondOnUserPermissionActs();
            }
        });
        permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
                        ArrayList<SongModel> songs = ((Result.Success<ArrayList<SongModel>>) result).get_Data();
                        mSharedData.set_value_in_worker_thread(songs);
                    } else if (result instanceof Result.Error) {
                        ((Result.Error<ArrayList<SongModel>>) result).exception.printStackTrace();
                    }
                });
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_holder, new ViewPagerFragment())
                    .commit();
        }
    }

    private void respondOnUserPermissionActs() {
        //user response
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //permission granted
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //show UI for the user explaining why we need this permission
                //alert dialog
                new AlertDialog.Builder(this)
                        .setTitle("Requesting Permission")
                        .setMessage("Allow the app to fetch songs from your device")
                        .setPositiveButton("Allow ", (dialogInterface, i) -> {
                            //request permission again
                            permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        })
                        .setNegativeButton("Don't Allow", (dialogInterface, i) -> {
                            Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_LONG).show();
                            dialogInterface.dismiss();
                        })
                        .show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void makeScanRequest(Uri folder, ResultCallback<ArrayList<SongModel>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Result<ArrayList<SongModel>> result = new Result.Success<>(Query.getSongsFromFolder(this, folder));
                callback.onComplete(result);
            } catch (Exception e) {
                Result<ArrayList<SongModel>> errorResult = new Result.Error<>(e);
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