package com.sirhadrian.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.sirhadrian.musicplayer.databinding.ActivityMainBinding;
import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.services.PlaySongs;
import com.sirhadrian.musicplayer.ui.SharedDataViewModel;
import com.sirhadrian.musicplayer.ui.main.MainActivityViewModel;
import com.sirhadrian.musicplayer.ui.viewpager.ViewPagerFragment;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private SharedDataViewModel mSharedData;
    private MainActivityViewModel mMainData;

    private ActivityResultLauncher<String> mUserPermission;

    // The activity should own the service
    // Those values need to survive
    private PlaySongs mService;
    private boolean mBound;

    public static final String bound_key = "BOUND";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        Intent intent = new Intent(this, PlaySongs.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        // Shared data between all fragments
        mSharedData = new ViewModelProvider(this).get(SharedDataViewModel.class);
        // Asking user for Read storage permission
        mUserPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            Context context = this;
            if (result) {
                Query.makeScanRequest(this, null, result1 -> {
                    if (result1 instanceof Result.Success) {
                        ArrayList<SongModel> songs = ((Result.Success<ArrayList<SongModel>>) result1).get_Data();
                        mSharedData.set_value_in_worker_thread(songs);
                    } else if (result1 instanceof Result.Error) {
                        ((Result.Error<ArrayList<SongModel>>) result1).exception.printStackTrace();
                    }
                });
            } else {
                respondOnUserPermissionActs(context);
            }
        });
        mUserPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Data persistence for Service
        mMainData = new ViewModelProvider(this).get(MainActivityViewModel.class);

        setContentView(root);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(bound_key, mBound);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mBound = savedInstanceState.getBoolean(bound_key, false);
    }

    private void respondOnUserPermissionActs(Context context) {
        //user response
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //permission granted
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_LONG).show();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //show UI for the user explaining why we need this permission
                //alert dialog
                new AlertDialog.Builder(context)
                        .setTitle("Requesting Permission")
                        .setMessage("Allow the app to fetch songs from your device")
                        .setPositiveButton("Allow ", (dialogInterface, i) -> {
                            //request permission again
                            mUserPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        })
                        .setNegativeButton("Don't Allow", (dialogInterface, i) -> {
                            Toast.makeText(context, "Permission denied", Toast.LENGTH_LONG).show();
                            dialogInterface.dismiss();
                        })
                        .show();
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
    }

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        PlaySongs.LocalBinder binder = (PlaySongs.LocalBinder) service;
        mService = binder.getService();
        mBound = true;

        mMainData.set_BoundValueRaw(true);
        mMainData.setServiceBound(mService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBound = mMainData.isBoundValueRaw();
        if (mService == null) {
            mService = mMainData.get_mService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mMainData.isHasOrientationChanged()) {
            mService.release();
            unbindService(this);
        }
        NotificationManagerCompat.from(this).cancelAll();
        mMainData.setHasOrientationChanged(false);
    }

    @Override
    public void onBackPressed() {
        if (ViewPagerFragment.isLastItem()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mMainData.setHasOrientationChanged(true);
    }
}