package com.example.stickerwhatsapp.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stickerwhatsapp.BuildConfig;
import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.utils.BaseActivity;
import com.example.stickerwhatsapp.utils.StickerBook;
import com.example.stickerwhatsapp.utils.StickerPack;
import com.example.stickerwhatsapp.utils.imageFolder;
import com.example.stickerwhatsapp.utils.pictureFacer;

import java.util.ArrayList;
import java.util.UUID;

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
    imageFolder imgFol;
    pictureFacer picFo;
    private StickerPack stickerPack;
    //ArrayList<StickerPack> stickerPackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_gallery);
        ButterKnife.bind(this);
        //stickerPack = getIntent().getParcelableExtra("sp");
        foldePath = getIntent().getStringExtra("folderPath");
        nameFolder = getIntent().getStringExtra("folderName");
        numberFile = getIntent().getIntExtra("number", 0);
        txtNameFolder.setText(nameFolder);
        if (numberFile != 0) {
            txtNumFolder.setText("" + numberFile);
        }
        stickerPack = StickerBook.getStickerPackByName(nameFolder);
        Log.e(TAG, "onCreate: "+stickerPack );
        ArrayList<pictureFacer> listImg = getAllImagesByFolder(foldePath);
        Log.e(TAG, "onCreate: " + nameFolder);
        rvDetailGallery.setLayoutManager(new GridLayoutManager(this, 4));
        detailGalleryAdapter = new DetailGalleryAdapter(listImg);
        rvDetailGallery.setAdapter(detailGalleryAdapter);
        rvDetailGallery.setRecycledViewPool(viewPool);
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
                //Uri uriImg = Uri.parse(pic.getPicturePath());
                //Uri uriImg = Uri.fromFile(new File(pic.getPicturePath()));
                Uri uriImg = Uri.parse("file://" + pic.getPicturePath());
                images.add(pic);
                //createNewStickerPack(nameFolder, nameFolder, uriImg);
                Log.e(TAG, "getAllImagesByFolder: " + uriImg);
            } while (cursor.moveToNext());
            cursor.close();

//            Log.e(TAG, "getAllImagesByFolder 2: " + StickerBook.getAllStickerPacks().get(1).getIdentifier());
//            Log.e(TAG, "getAllImagesByFolder 2: " + StickerBook.getAllStickerPacks().get(2).getIdentifier());
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
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Here is the share content body";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case R.id.btnAddToWhatApp:
                if (numberFile > 3 && numberFile < 30) {
                    showAlertDialog();
                } else {
                    Toast.makeText(this, "You can have over 3 sticker", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void createNewStickerPack(String name, String creator, Uri trayImage) {
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
//        StickerBook.addStickerPackExisting(sp);
    }

    //    private void addStickerPackToWhatsApp(String stickerPackName) {
//        String id = UUID.randomUUID().toString();
//        Intent intent = new Intent();
//        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
//        intent.putExtra(DetailGalleryActivity.EXTRA_STICKER_PACK_ID, id);
//        intent.putExtra(DetailGalleryActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY);
//        intent.putExtra(DetailGalleryActivity.EXTRA_STICKER_PACK_NAME, stickerPackName);
//
//        try {
//            startActivityForResult(intent, ADD_PACK);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
//        }
//    }
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

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn có muốn thêm bộ sticker vào WhatsApp không?");
        builder.setCancelable(false);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                if (stickerPack.getStickers().size() >= 3) {
                if (numberFile >= 3) {
                    addStickerPackToWhatsApp(stickerPack);
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(DetailGalleryActivity.this)
                            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
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


}
