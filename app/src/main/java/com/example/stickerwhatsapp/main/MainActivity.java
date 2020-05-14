package com.example.stickerwhatsapp.main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ratedialog.RatingDialog;
import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.add.AddStickerActivity;
import com.example.stickerwhatsapp.detail.DetailGalleryActivity;
import com.example.stickerwhatsapp.utils.SharedPrefsUtils;
import com.example.stickerwhatsapp.utils.StickerBook;
import com.example.stickerwhatsapp.utils.StickerPack;
import com.example.stickerwhatsapp.utils.imageFolder;
import com.example.stickerwhatsapp.utils.pictureFacer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements RatingDialog.RatingDialogInterFace {
    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_RECORD_STORAGE_PERMISSION = 34;
    private static final int PERMISSION_REQUEST_CODE = 200;
    @BindView(R.id.rvMain)
    RecyclerView rvMain;
    @BindView(R.id.imgAdd)
    ImageView imgAdd;
    MainAdapter mainAdapter;
    List<imageFolder> mainModels;
    ArrayList<pictureFacer> allpictures;
    ArrayList<Uri> uriList = new ArrayList<>();
    @BindView(R.id.infoTxtCredits)
    TextView infoTxtCredits;
    FrameLayout frameMain;
    ProgressDialog progressDialog;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;
    int rand = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (ContextCompat.checkSelfPermission(MainActivity.this,
//                Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED)
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        if (!checkRecordAndStoragePermission()) {
            requestRecordAndStoragePermission();
        }
        rateAuto();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.adInterId));
        adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        ButterKnife.bind(this);
        //SD Card
        initView();
        initAds();
    }
    private void initView(){
        mainModels = getPicturePaths();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvMain.setLayoutManager(llm);
        mainAdapter = new MainAdapter(mainModels, this);
        rvMain.setAdapter(mainAdapter);
        mainAdapter.setOnClickListener(new MainAdapter.OnClickListener() {
            @Override
            public void onClickItem(int position) {
                progressDialog.show();
                for (int i = 0; i < allpictures.size(); i++) {
                    Uri uriImg = Uri.fromFile(new File(allpictures.get(i).getPicturePath()));
                    uriList.add(uriImg);
                    Log.e(TAG, "onClickItem: " + uriImg);
                }
                Random rn = new Random();
                rand = rn.nextInt(11);
                if (mInterstitialAd.isLoaded()) {
                    createNewStickerPack(mainModels.get(position).getFolderName(), mainModels.get(position).getFolderName(), uriList.get(0), uriList, getApplicationContext());
                    StickerPack sp = StickerBook.getStickerPackByName(mainModels.get(position).getFolderName());
                    if(rand < 8){
                        progressDialog.dismiss();
                        mInterstitialAd.show();
                        mInterstitialAd.setAdListener(new AdListener(){
                            @Override
                            public void onAdClosed(){
                                super.onAdClosed();
                                onPicClicked(sp, mainModels.get(position).getPath(), mainModels.get(position).getFolderName(), mainModels.get(position).getNumberOfPics());
                            }
                        });
                    }
                    else{
                        progressDialog.dismiss();
                        onPicClicked(sp, mainModels.get(position).getPath(), mainModels.get(position).getFolderName(), mainModels.get(position).getNumberOfPics());
                    }

                } else {
                    createNewStickerPack(mainModels.get(position).getFolderName(), mainModels.get(position).getFolderName(), uriList.get(0), uriList, getApplicationContext());
                    StickerPack sp = StickerBook.getStickerPackByName(mainModels.get(position).getFolderName());
                    progressDialog.dismiss();
                    onPicClicked(sp, mainModels.get(position).getPath(), mainModels.get(position).getFolderName(), mainModels.get(position).getNumberOfPics());
                    mInterstitialAd.loadAd(adRequest);
                }

            }
        });
    }
    private void initAds() {
        frameMain = findViewById(R.id.frameMain);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.adsbanner));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        frameMain.addView(adView);
    }
    private ArrayList<imageFolder> getPicturePaths() {
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                String folderpaths2 = folderpaths + folder + "/";
                allpictures = getAllImagesByFolder(folderpaths2, folder);
                folderpaths = folderpaths + folder + "/";
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);
                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);
                    folds.setSubItem(allpictures);
                    folds.addpics();
                    picFolders.add(0, folds);
                    Log.e(TAG, "getPicturePaths: " + folder);
                } else {
                    for (int i = 0; i < picFolders.size(); i++) {
                        if (picFolders.get(i).getPath().equals(folderpaths)) {
                            picFolders.get(i).setFirstPic(datapath);
                            picFolders.get(i).addpics();

                        }
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
            Log.e(TAG, "getPicturePaths: " + allImagesuri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return picFolders;
    }

    public ArrayList<pictureFacer> getAllImagesByFolder(String path, String folder) {
        ArrayList<pictureFacer> images = new ArrayList<>();
        Uri allVideosuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = MainActivity.this.getContentResolver().query(allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            cursor.moveToFirst();
            do {
                pictureFacer pic = new pictureFacer();
                pic.setPicturName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                Uri uriImg = Uri.parse(pic.getPicturePath());
                //Uri uriImg = Uri.fromFile(new File(pic.getPicturePath()));
                images.add(pic);
            } while (cursor.moveToNext());
            cursor.close();
            StickerBook.writeTOoJSON(this);
            ArrayList<pictureFacer> reSelection = new ArrayList<>();
            for (int i = images.size() - 1; i > -1; i--) {
                reSelection.add(images.get(i));
            }
            images = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    private void createNewStickerPack(String name, String creator, Uri trayImage, List<Uri> imageChildUri, Context context) {
        String newId = UUID.randomUUID().toString();
        StickerPack sp = new StickerPack(
                newId,
                name,
                creator,
                trayImage,
                "",
                "",
                "",
                "",
                this);
        for (Uri uri : imageChildUri) {
            sp.addSticker(uri, context);
        }
        StickerBook.addStickerPackExisting(sp);
    }

    public void onPicClicked(String pictureFolderPath, String folderName, int numberFolder) {
        Intent move = new Intent(this, DetailGalleryActivity.class);
        move.putExtra("folderPath", pictureFolderPath);
        move.putExtra("folderName", folderName);
        move.putExtra("number", numberFolder);
        startActivity(move);

    }

    public void onPicClicked(StickerPack sp, String pictureFolderPath, String folderName, int numberFolder) {
        final ProgressDialog pd = ProgressDialog.show(this,
                "", "Loading", true);
        Intent move = new Intent(this, DetailGalleryActivity.class);
        new Thread(new Runnable() {
            public void run() {
                move.putExtra("sp", sp);
                move.putExtra("folderPath", pictureFolderPath);
                move.putExtra("folderName", folderName);
                move.putExtra("number", numberFolder);
                startActivity(move);
                pd.dismiss();
            }
        }).start();
//        move.putExtra("sp", sp);
//        move.putExtra("folderPath", pictureFolderPath);
//        move.putExtra("folderName", folderName);
//        move.putExtra("number", numberFolder);
//        startActivity(move);
    }

    public void onClickAdd(String pictureFolderPath) {
        Intent move = new Intent(this, AddStickerActivity.class);
        move.putExtra("folderPath", pictureFolderPath);
        startActivity(move);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestRecordAndStoragePermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    @OnClick({ R.id.infoTxtCredits, R.id.imgAdd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.infoTxtCredits:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("http://www.google.com"));
                startActivity(browserIntent);
                break;
            case R.id.imgAdd:
                Random rn = new Random();
                rand = rn.nextInt(11);
                if (mInterstitialAd.isLoaded()) {
                    if(rand < 8){
                        mInterstitialAd.show();
                        mInterstitialAd.setAdListener(new AdListener(){
                            @Override
                            public void onAdClosed(){
                                super.onAdClosed();
                                onClickAdd(mainModels.get(0).getPath());
                            }
                        });
                    }
                    else{
                        onClickAdd(mainModels.get(mainAdapter.getItemCount() -1 ).getPath());
                    }

                } else {
                    onClickAdd(mainModels.get(mainAdapter.getItemCount() - 1).getPath());
                    mInterstitialAd.loadAd(adRequest);
                }
                break;
        }
    }
    void moveToNewApp(String appId) {
        Intent intent = new Intent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + appId)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void goToMarket() {
        Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://search?q=torrent clients"));
        startActivity(goToMarket);
    }

    public static void rateApp(Context context) {
        Intent intent = new Intent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void rateAuto() {
        int rate = SharedPrefsUtils.getInstance(this).getInt("rate");
        if (rate < 1) {
            RatingDialog ratingDialog = new RatingDialog(this);
            ratingDialog.setRatingDialogListener((RatingDialog.RatingDialogInterFace) this);
            ratingDialog.showDialog();
            SharedPrefsUtils.getInstance(this).putInt("rate", 5);

        }
    }

    void rateManual() {
        RatingDialog ratingDialog = new RatingDialog(this);
        ratingDialog.setRatingDialogListener((RatingDialog.RatingDialogInterFace) this);
        ratingDialog.showDialog();
    }

    @Override
    public void onDismiss() {

    }

    @Override
    public void onSubmit(float rating) {
        if (rating > 3) {
            rateApp(this);
            SharedPrefsUtils.getInstance(this).putInt("rate", 5);
        }
    }

    @Override
    public void onRatingChanged(float rating) {

    }
}
