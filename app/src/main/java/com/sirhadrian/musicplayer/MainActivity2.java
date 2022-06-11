package com.sirhadrian.musicplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.services.PlaySongs;
import com.sirhadrian.musicplayer.ui.SharedDataViewModel;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity implements ServiceConnection {

    private NavController mNav;

    private Uri mSearchFolder;
    private SharedDataViewModel mSharedData;
    private Toolbar mToolBar;
    private ActivityResultLauncher<String> mUserPermission;

    // The activity should own the service
    private PlaySongs mService;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, PlaySongs.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        // Shared data between all fragments
        mSharedData = new ViewModelProvider(this).get(SharedDataViewModel.class);
        // Asking user for Read storage permission
        mUserPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            Context context = this;
            if (result) {
                mSharedData.loadSongs(Query.getAllAudioFromDevice(context, null));
            } else {
                respondOnUserPermissionActs(context);
            }
        });
        mUserPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
        Log.d("myserv", "Service disconnected can't modify player");
    }

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        PlaySongs.LocalBinder binder = (PlaySongs.LocalBinder) service;
        mService = binder.getService();
        mBound = true;
        Log.d("myserv", "Service connected can modify player");
    }

    public boolean ismBound() {
        return mBound;
    }

    public void set_mBound(boolean mBound) {
        this.mBound = mBound;
    }

    public PlaySongs get_mService() {
        return mService;
    }
}