package com.example.stickerwhatsapp.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stickerwhatsapp.BuildConfig;
import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.utils.BaseActivity;
import com.example.stickerwhatsapp.utils.StickerBook;
import com.example.stickerwhatsapp.utils.StickerPack;
import com.example.stickerwhatsapp.utils.WhitelistCheck;
import com.example.stickerwhatsapp.utils.pictureFacer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailGalleryActivity extends AppCompatActivity {
    private static final String TAG = "DetailGallery";
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    public static final int ADD_PACK = 200;
    @BindView(R.id.btnDeleteToWhatApp)
    Button btnDeleteToWhatApp;
    @BindView(R.id.frameDetail)
    FrameLayout frameDetail;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    @BindView(R.id.btnBackDetailGallery)
    ImageView btnBackDetailGallery;
    @BindView(R.id.txtNameFolder)
    TextView txtNameFolder;
    @BindView(R.id.txtNumFolder)
    TextView txtNumFolder;
    @BindView(R.id.btnShareTwo)
    ImageView btnShareTwo;
    @BindView(R.id.btnAddToWhatApp)
    Button btnAddToWhatApp;
    DetailGalleryAdapter detailGalleryAdapter;
    String foldePath, nameFolder;
    int numberFile = 0;
    @BindView(R.id.rvDetailGallery)
    RecyclerView rvDetailGallery;
    private StickerPack stickerPack;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private AdRequest adRequest;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setContentView(R.layout.activity_detail_gallery);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.adInterId));
        adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        ButterKnife.bind(this);
        initData();
        initAds();

    }
    private void initData(){
        foldePath = getIntent().getStringExtra("folderPath");
        nameFolder = getIntent().getStringExtra("folderName");
        numberFile = getIntent().getIntExtra("number", 0);
        txtNameFolder.setText(nameFolder);
        if (numberFile != 0) {
            txtNumFolder.setText("" + numberFile);
        }
        stickerPack = StickerBook.getStickerPackByName(nameFolder);
        Log.e(TAG, "onCreate: " + stickerPack);
        ArrayList<pictureFacer> listImg = getAllImagesByFolder(foldePath);
        Log.e(TAG, "onCreate: " + nameFolder);
        rvDetailGallery.setLayoutManager(new GridLayoutManager(this, 4));
        detailGalleryAdapter = new DetailGalleryAdapter(listImg);
        rvDetailGallery.setAdapter(detailGalleryAdapter);
        rvDetailGallery.setRecycledViewPool(viewPool);
    }
    private void initAds() {
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.adsbanner));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        frameDetail.addView(adView);
    }
    public ArrayList<pictureFacer> getAllImagesByFolder(String path) {
        ArrayList<pictureFacer> images = new ArrayList<>();
        Uri allVideosuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = DetailGalleryActivity.this.getContentResolver().query(allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            cursor.moveToFirst();
            do {
                pictureFacer pic = new pictureFacer();
                pic.setPicturName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                Uri uriImg = Uri.parse("file://" + pic.getPicturePath());
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

    @OnClick({R.id.btnBackDetailGallery, R.id.btnShareTwo, R.id.btnAddToWhatApp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBackDetailGallery:
                onBackPressed();
                break;
            case R.id.btnShareTwo:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Uri screenshotUri = Uri.parse(getAllImagesByFolder(nameFolder).get(0).getPicturePath());
                Log.e(TAG, "onViewClicked share: " + screenshotUri );
                sharingIntent.setType("image/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                startActivity(Intent.createChooser(sharingIntent, "Share sticker"));
                break;
            case R.id.btnAddToWhatApp:
                if (numberFile > 3 && numberFile < 30) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();
                                showAlertDialog();
                            }
                        });
                    } else {
                        showAlertDialog();
                        mInterstitialAd.loadAd(adRequest);
                    }

                } else {
                    Toast.makeText(this, "You can have over 3 sticker", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void addStickerPackToWhatsApp(StickerPack sp) {
        Intent intent = new Intent();
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        intent.putExtra(DetailGalleryActivity.EXTRA_STICKER_PACK_ID, sp.getIdentifier());
        intent.putExtra(DetailGalleryActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        intent.putExtra(DetailGalleryActivity.EXTRA_STICKER_PACK_NAME, nameFolder);
        try {
            startActivityForResult(intent, ADD_PACK);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED && data != null) {
                final String validationError = data.getStringExtra("validation_error");
                if (validationError != null) {
                    if (BuildConfig.DEBUG) {
                        //validation error should be shown to developer only, not users.
                        BaseActivity.MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(getSupportFragmentManager(), "validation error");
                    }
                    Log.e(TAG, "Validation failed:" + validationError);
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPack);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void updateAddUI(Boolean isWhitelisted) {
        if (isWhitelisted) {
            btnAddToWhatApp.setVisibility(View.GONE);
            btnDeleteToWhatApp.setVisibility(View.VISIBLE);
        } else {
            btnAddToWhatApp.setVisibility(View.VISIBLE);
            btnDeleteToWhatApp.setVisibility(View.GONE);
        }
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, Boolean> {
        private final WeakReference<DetailGalleryActivity> stickerPackDetailsActivityWeakReference;

        WhiteListCheckAsyncTask(DetailGalleryActivity detailGalleryActivity) {
            this.stickerPackDetailsActivityWeakReference = new WeakReference<>(detailGalleryActivity);
        }

        @Override
        protected final Boolean doInBackground(StickerPack... stickerPacks) {
            StickerPack stickerPackCheck = stickerPacks[0];
            final DetailGalleryActivity detailGalleryActivity = stickerPackDetailsActivityWeakReference.get();
            if (detailGalleryActivity == null) {
                return false;
            }
            return WhitelistCheck.isWhitelisted(detailGalleryActivity, stickerPackCheck.getIdentifier());
        }

        @Override
        protected void onPostExecute(Boolean isWhitelisted) {
            final DetailGalleryActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            if (stickerPackDetailsActivity != null) {
                stickerPackDetailsActivity.updateAddUI(isWhitelisted);
            }
        }
    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to add a sticker to WhatsApp?");
        builder.setCancelable(false);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (numberFile >= 3) {
                    addStickerPackToWhatsApp(stickerPack);
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(DetailGalleryActivity.this)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create();
                    alertDialog.setTitle("Invalid Action");
                    alertDialog.setMessage("In order to be applied to WhatsApp, the sticker pack must have at least 3 stickers. Please add more stickers first.");
                    alertDialog.show();
                }
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showAlertDialogDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to remove sticker on WhatsApp");
        builder.setCancelable(false);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                StickerBook.deleteStickerPackById(stickerPack.getIdentifier());
                Toast.makeText(DetailGalleryActivity.this, "Sticker Deleted Complete", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @OnClick(R.id.btnDeleteToWhatApp)
    public void onViewClicked() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    showAlertDialogDelete();
                }
            });
        } else {
            showAlertDialogDelete();
            mInterstitialAd.loadAd(adRequest);
        }

    }
}
