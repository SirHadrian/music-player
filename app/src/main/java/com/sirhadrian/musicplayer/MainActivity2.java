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
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.sirhadrian.musicplayer.model.database.SongModel;
import com.sirhadrian.musicplayer.ui.SharedDataViewModel;
import com.sirhadrian.musicplayer.utils.Query;
import com.sirhadrian.musicplayer.utils.Result;
import com.sirhadrian.musicplayer.utils.ResultCallback;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity {

    private NavController mNav;

    private Uri mSearchFolder;
    private SharedDataViewModel mSharedData;
    private Toolbar mToolBar;
    private ActivityResultLauncher<String> mUserPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}