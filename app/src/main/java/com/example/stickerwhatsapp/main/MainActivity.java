package com.example.stickerwhatsapp.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stickerwhatsapp.add.AddStickerActivity;
import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.detail.DetailGalleryActivity;
import com.example.stickerwhatsapp.utils.Sticker;
import com.example.stickerwhatsapp.utils.StickerBook;
import com.example.stickerwhatsapp.utils.StickerPack;
import com.example.stickerwhatsapp.utils.imageFolder;
import com.example.stickerwhatsapp.utils.pictureFacer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    @BindView(R.id.rvStickerAdded)
    RecyclerView rvStickerAdded;
    @BindView(R.id.rvMain)
    RecyclerView rvMain;
    @BindView(R.id.imgAdd)
    ImageView imgAdd;
    MainAdapter mainAdapter;
    List<imageFolder> mainModels;
    ArrayList<pictureFacer> allpictures;
    ArrayList<Uri> uriList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ButterKnife.bind(this);
        //SD Card
        mainModels = getPicturePaths();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvMain.setLayoutManager(llm);
        mainAdapter = new MainAdapter(mainModels, this);
        rvMain.setAdapter(mainAdapter);
        mainAdapter.setOnClickListener(new MainAdapter.OnClickListener() {
            @Override
            public void onClickItem(int position) {
                for(int i=0; i<allpictures.size(); i++){
                    Uri uriImg = Uri.fromFile(new File(allpictures.get(i).getPicturePath()));
                    uriList.add(uriImg);
                    Log.e(TAG, "onClickItem: "+ uriImg);
                }
                createNewStickerPack(mainModels.get(position).getFolderName(), mainModels.get(position).getFolderName(),uriList.get(0), uriList,getApplicationContext());
                StickerPack sp = StickerBook.getStickerPackByName(mainModels.get(position).getFolderName());
                //onPicClicked( mainModels.get(position).getPath(), mainModels.get(position).getFolderName(), mainModels.get(position).getNumberOfPics());
                onPicClicked(sp, mainModels.get(position).getPath(), mainModels.get(position).getFolderName(), mainModels.get(position).getNumberOfPics());
                //Log.e(TAG, "onClickItem: "+mainModels.get(position).getPath());
            }
        });
    }
    private ArrayList<imageFolder> getPicturePaths() {
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
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
                    picFolders.add(folds);
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
            Log.e(TAG, "getPicturePaths: "+ allImagesuri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return picFolders;
    }

    public ArrayList<pictureFacer> getAllImagesByFolder(String path, String folder) {
        ArrayList<pictureFacer> images = new ArrayList<>();
        Uri allVideosuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
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
        for(Uri uri:imageChildUri){
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
        Intent move = new Intent(this, DetailGalleryActivity.class);
        move.putExtra("sp", sp);
        move.putExtra("folderPath", pictureFolderPath);
        move.putExtra("folderName", folderName);
        move.putExtra("number", numberFolder);
        startActivity(move);
    }

    @OnClick(R.id.imgAdd)
    public void onViewClicked() {
        onClickAdd(mainModels.get(1).getPath());
    }

    public void onClickAdd(String pictureFolderPath) {
        Intent move = new Intent(this, AddStickerActivity.class);
        move.putExtra("folderPath", pictureFolderPath);
        startActivity(move);
    }
}
