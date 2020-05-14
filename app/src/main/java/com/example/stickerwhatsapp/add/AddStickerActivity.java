package com.example.stickerwhatsapp.add;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.main.MainActivity;
import com.example.stickerwhatsapp.utils.MediaScannerWrapper;
import com.example.stickerwhatsapp.utils.pictureFacer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddStickerActivity extends AppCompatActivity {
    private static final String TAG = "AddStickerActivity";
    private final int CODE_IMG_GALLERY = 1;
    private final int GALLERY_REQUEST = 1;
    private static final String INTENT_ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK";
    ArrayList<pictureFacer> faceList;
    List<String> imgListChoosed = new ArrayList<>();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.edtPackName)
    EditText edtPackName;
    @BindView(R.id.edtAuthor)
    EditText edtAuthor;
    @BindView(R.id.btnCheck)
    ImageView btnCheck;
    @BindView(R.id.txtTapOn)
    TextView txtTapOn;
    @BindView(R.id.rvChooseAdd)
    RecyclerView rvChooseAdd;
    @BindView(R.id.rvImageChoose)
    RecyclerView rvImageChoose;
    AddSubItemAdapter addSubItemAdapter;
    String foldePath;
    @BindView(R.id.relRvChoosed)
    RelativeLayout relRvChoosed;
    ItemChooseAdapter itemChooseAdapter;
    @BindView(R.id.txtNumChoosed)
    TextView txtNumChoosed;
    Uri uriCrop;
    List<Uri> imageClroppeds;
    ProgressDialog progressDialog;
    @BindView(R.id.frameAdd)
    FrameLayout frameAdd;
    private AdRequest adRequest;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sticker);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.adInterId));
        adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        ButterKnife.bind(this);
        initData();
        initView();
        initAds();
    }

    private void initData() {
        imageClroppeds = new ArrayList<>();
        faceList = new ArrayList<>();
        foldePath = getIntent().getStringExtra("folderPath");
        faceList = getAllImagesByFolder(foldePath);
    }

    private void initView() {
        progressDialog = new ProgressDialog(AddStickerActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvChooseAdd.setLayoutManager(linearLayoutManager1);
        itemChooseAdapter = new ItemChooseAdapter(imgListChoosed);
        itemChooseAdapter.setOnClickListener(new ItemChooseAdapter.OnClickListener() {
            @Override
            public void onClickItem(int position) {
                imgListChoosed.remove(position);
                itemChooseAdapter.notifyDataSetChanged();
                txtNumChoosed.setText(imgListChoosed.size() + 1 - 1 + "/30");
            }
        });
        rvChooseAdd.setAdapter(itemChooseAdapter);
        rvChooseAdd.getLayoutManager().smoothScrollToPosition(rvChooseAdd, null, imgListChoosed.size());
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        rvImageChoose.setLayoutManager(linearLayoutManager);
        addSubItemAdapter = new AddSubItemAdapter(faceList);
        addSubItemAdapter.setOnClickListener(new AddSubItemAdapter.OnClickListener() {
            @Override
            public void onClickItem(int position) {
                if (position > 0) {
                    if (imgListChoosed.size() > 0) {
                        for (String item : imgListChoosed) {
                            Log.e(TAG, "onClickItem 11: " + faceList.get(position).getPicturePath());
                            Log.e(TAG, "onClickItem 11: " + item);
                            if (faceList.get(position).getPicturePath().equals(item)) {
                                Toast.makeText(AddStickerActivity.this, "Choose item other", Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                imgListChoosed.add(0, faceList.get(position).getPicturePath());
                                break;
                            }
                        }
                    } else {
                        imgListChoosed.add(faceList.get(position).getPicturePath());
                    }
                    itemChooseAdapter.notifyItemInserted(0);
                    itemChooseAdapter.notifyDataSetChanged();
                    txtNumChoosed.setText(imgListChoosed.size() + "/30");
                    txtTapOn.setVisibility(View.GONE);
                }
                if (position == 0) {
                    startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT)
                            .setType("image/*"), CODE_IMG_GALLERY);
                }
            }
        });
        rvImageChoose.setAdapter(addSubItemAdapter);
    }
    private void initAds() {
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.adsbanner));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        frameAdd.addView(adView);
    }
    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes?");
        builder.setCancelable(false);
        builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onBackPressed();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public ArrayList<pictureFacer> getAllImagesByFolder(String path) {
        ArrayList<pictureFacer> images = new ArrayList<>();
        Uri allVideosuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = AddStickerActivity.this.getContentResolver().query(allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            cursor.moveToFirst();
            do {
                pictureFacer pic = new pictureFacer();
                pic.setPicturName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                Uri uriImg = Uri.parse(pic.getPicturePath());
                images.add(pic);
                Log.e(TAG, "getAllImagesByFolder: " + uriImg);
            } while (cursor.moveToNext());
            cursor.close();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick({R.id.btnBack, R.id.btnCheck})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                showAlertDialog();
                break;
            case R.id.btnCheck:
                progressDialog.show();
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                if (edtAuthor.getText().toString().isEmpty() && edtPackName.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Sticker null", Toast.LENGTH_SHORT).show();
                } else if (imgListChoosed.size() == 0 || imgListChoosed.size() < 3 || imgListChoosed.size() > 30) {
                    Toast.makeText(this, "You need choose 3 sticker", Toast.LENGTH_SHORT).show();
                } else {
                    if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                            mInterstitialAd.setAdListener(new AdListener(){
                                @Override
                                public void onAdClosed(){
                                    super.onAdClosed();
                                    String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + edtPackName.getText() + "/";
                                    File file1 = new File(dirName);
                                    if (!file1.exists()) {
                                        Log.e(TAG, "onViewClicked 2: " + new File(dirName).mkdirs() + "   " + dirName);
                                    }
                                    saveFileUri(file1.getAbsolutePath());
                                    for (int i = 0; i < imgListChoosed.size(); i++) {
                                        if (!imgListChoosed.get(i).contains("file:")) {
                                            File file2 = new File(imgListChoosed.get(i));
                                            Log.e(TAG, "onViewClicked uri: " + file2);
                                            try {
                                                exportFile(file2, file1, i);
                                                MediaScannerWrapper mediaScannerWrapper = new MediaScannerWrapper(getApplicationContext(), exportFile(file2, file1, i).getPath(), null);
                                                mediaScannerWrapper.scan();
                                                progressDialog.dismiss();
                                                Log.e(TAG, "onViewClicked file 2: " + imgListChoosed.get(i));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    onBackPressed();
                                }
                            });
                    } else {
                        String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + edtPackName.getText() + "/";
                        File file1 = new File(dirName);
                        if (!file1.exists()) {
                            Log.e(TAG, "onViewClicked 2: " + new File(dirName).mkdirs() + "   " + dirName);
                        }
                        saveFileUri(file1.getAbsolutePath());
                        for (int i = 0; i < imgListChoosed.size(); i++) {
                            if (!imgListChoosed.get(i).contains("file:")) {
                                File file2 = new File(imgListChoosed.get(i));
                                Log.e(TAG, "onViewClicked uri: " + file2);
                                try {
                                    exportFile(file2, file1, i);
                                    MediaScannerWrapper mediaScannerWrapper = new MediaScannerWrapper(getApplicationContext(), exportFile(file2, file1, i).getPath(), null);
                                    mediaScannerWrapper.scan();
                                    progressDialog.dismiss();
                                    Log.e(TAG, "onViewClicked file 2: " + imgListChoosed.get(i));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        onBackPressed();
                        mInterstitialAd.loadAd(adRequest);
                    }

                }
                break;
        }
    }
    private void saveFileUri(String folder) {
        for (Uri sourceuri : imageClroppeds) {
            String sourceFilename = sourceuri.getPath();
            String name = folder + "/" + Calendar.getInstance().getTimeInMillis() + ".png";
            MediaScannerWrapper mediaScannerWrapper = new MediaScannerWrapper(getApplicationContext(), name, null);
            mediaScannerWrapper.scan();
            Log.e(TAG, "saveFileUri: " + name);
            String destinationFilename = name;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                bis = new BufferedInputStream(new FileInputStream(sourceFilename));
                bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
                byte[] buf = new byte[1024];
                bis.read(buf);
                do {
                    bos.write(buf);
                } while (bis.read(buf) != -1);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) bis.close();
                    if (bos != null) bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File exportFile(File src, File dst, int i) throws IOException {
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File expFile = new File(dst.getPath() + File.separator + "IMG_" + timeStamp + i + ".png");
        Log.e(TAG, "exportFile: " + timeStamp);
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
        return expFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CODE_IMG_GALLERY)) {
            if (data != null) {
                uriCrop = data.getData();
                Log.e(TAG, "onActivityResult 1: " + uriCrop);
                startCrop(uriCrop);
            } else {
                if (uriCrop != null) {
                    startCrop(uriCrop);
                }
            }
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri mImageUri = UCrop.getOutput(data);
            Log.e(TAG, "onActivityResult 2: " + mImageUri);
            String mImg = mImageUri.toString();
            imageClroppeds.add(mImageUri);
            imgListChoosed.add(0, mImg);
            itemChooseAdapter.notifyDataSetChanged();
            txtNumChoosed.setText(imgListChoosed.size() + "/30");
            txtTapOn.setVisibility(View.GONE);
        }
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = Calendar.getInstance().getTimeInMillis() + "";
        destinationFileName += ".png";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        Log.e(TAG, "onActivityResult 3: " + uCrop);
        //UCrop uCrop = UCrop.of(uri, Uri.parse(destinationFileName));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(350, 350);
        uCrop.withOptions(getCropOptions());
        uCrop.start(this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        //Compress Type
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        //UI
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        options.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);
        return options;
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    private File saveBitmap(Bitmap bitmap, String path) {
        File file = null;
        if (bitmap != null) {
            file = new File(path);
            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path); //here is set your file path where you want to save or also here you can set file object directly

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // bitmap is your Bitmap instance, if you want to compress it you can compress reduce percentage
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
