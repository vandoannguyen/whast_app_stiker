package com.example.stickerwhatsapp.main;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.utils.MediaScannerWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoadingActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_STORAGE_PERMISSION = 34;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        if (!checkRecordAndStoragePermission()) {
            requestRecordAndStoragePermission();
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }
    private boolean copyImgAsset() {
        copyAsset("Sticker pack 6","bo400.png");
        copyAsset("Sticker pack 6","bo401.png");
        copyAsset("Sticker pack 6","bo402.png");
        copyAsset("Sticker pack 6","bo403.png");
        copyAsset("Sticker pack 6","bo404.png");
        copyAsset("Sticker pack 6","bo405.png");
        copyAsset("Sticker pack 6","bo406.png");
        copyAsset("Sticker pack 5","bo300.png");
        copyAsset("Sticker pack 5","bo301.png");
        copyAsset("Sticker pack 5","bo302.png");
        copyAsset("Sticker pack 5","bo303.png");
        copyAsset("Sticker pack 5","bo304.png");
        copyAsset("Sticker pack 5","bo306.png");
        copyAsset("Sticker pack 5","bo3062.png");
        copyAsset("Sticker pack 5","bo3063.png");
        copyAsset("Sticker pack 5","bo307.png");
        copyAsset("Sticker pack 5","bo3071.png");
        copyAsset("Sticker pack 5","bo3072.png");
        copyAsset("Sticker pack 5","bo308.png");
        copyAsset("Sticker pack 4","00.png");
        copyAsset("Sticker pack 4","001.png");
        copyAsset("Sticker pack 4","002.png");
        copyAsset("Sticker pack 4","003.png");
        copyAsset("Sticker pack 4","004.png");
        copyAsset("Sticker pack 4","005.png");
        copyAsset("Sticker pack 4","006.png");
        copyAsset("Sticker pack 3","bo201.png");
        copyAsset("Sticker pack 3","bo202.png");
        copyAsset("Sticker pack 3","bo203.png");
        copyAsset("Sticker pack 3","bo204.png");
        copyAsset("Sticker pack 2","01.png");
        copyAsset("Sticker pack 2","02.png");
        copyAsset("Sticker pack 2","03.png");
        copyAsset("Sticker pack 2","04.png");
        copyAsset("Sticker pack 2","06.png");
        copyAsset("Sticker pack 2","07.png");
        copyAsset("Sticker pack 2","08.png");
        copyAsset("Sticker pack 1","00_tray_icon.png");
        copyAsset("Sticker pack 1","01_pumkin.png");
        copyAsset("Sticker pack 1","02_pumkin.png");
        copyAsset("Sticker pack 1","03_pumkin.png");
        copyAsset("Sticker pack 1","04_pumkin.png");
        copyAsset("Sticker pack 1","05_pumkin.png");
        return true;
    }

    private void copyAsset(String pathname, String filename) {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + pathname;
        File fl = new File(dirPath + "/" + filename);
        MediaScannerWrapper mediaScannerWrapper = new MediaScannerWrapper(getApplicationContext(), fl.getPath(), null);
        mediaScannerWrapper.scan();
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(dirPath, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void requestRecordAndStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, REQUEST_RECORD_STORAGE_PERMISSION);
    }

    private boolean checkRecordAndStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_RECORD_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkRecordAndStoragePermission()) {
                            Log.e("TAG", "onRequestPermissionsResult: truee" );
                            if(copyImgAsset()){
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
                }
                break;
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(LoadingActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
