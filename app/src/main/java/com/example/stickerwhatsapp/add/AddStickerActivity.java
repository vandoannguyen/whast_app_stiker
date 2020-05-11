package com.example.stickerwhatsapp.add;

import android.Manifest;
import android.app.AlertDialog;
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
import com.example.stickerwhatsapp.utils.imageFolder;
import com.example.stickerwhatsapp.utils.pictureFacer;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddStickerActivity extends AppCompatActivity {
    private static final String TAG = "AddStickerActivity";
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private final int CODE_IMG_GALLERY = 1;
    private static final String INTENT_ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK";
    ArrayList<pictureFacer> faceList;
    List<String> imgListChoosed = new ArrayList<>();
    private final int GALLERY_REQUEST = 1;
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
    List<imageFolder> picFolders = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sticker);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
//        if (imgListChoosed.size() == 0) {
//            txtTapOn.setVisibility(View.VISIBLE);
//            relRvChoosed.setVisibility(View.GONE);
//        }
//        else{
//            txtTapOn.setVisibility(View.GONE);
//            relRvChoosed.setVisibility(View.VISIBLE);
//        }
        faceList = new ArrayList<>();
        foldePath = getIntent().getStringExtra("folderPath");
        faceList = getAllImagesByFolder(foldePath);
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvChooseAdd.setLayoutManager(linearLayoutManager1);
        itemChooseAdapter = new ItemChooseAdapter(imgListChoosed);
        itemChooseAdapter.setOnClickListener(new ItemChooseAdapter.OnClickListener() {
            @Override
            public void onClickItem(int position) {

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
                    if (imgListChoosed.size() == 0) {
                        imgListChoosed.add(faceList.get(position).getPicturePath());
                    } else {
                        for (int i = 0; i < imgListChoosed.size(); i++) {
                            if (faceList.get(position).equals(imgListChoosed.get(i))) {
                                Toast.makeText(AddStickerActivity.this, "Choose item other", Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                imgListChoosed.add(faceList.get(position).getPicturePath());
                                break;
                            }
                        }
                    }
                    itemChooseAdapter.notifyDataSetChanged();
                    txtNumChoosed.setText(imgListChoosed.size() + "/30");
                    txtTapOn.setVisibility(View.GONE);
                    Log.e(TAG, "onClickItem: " + imgListChoosed.size());
                }
                if (position == 0) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                }
            }
        });
        rvImageChoose.setAdapter(addSubItemAdapter);
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
            for(int i = images.size()-1;i > -1;i--){
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
                Log.e(TAG, "onViewClicked 1: " + (ActivityCompat.checkSelfPermission(this.btnBack.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this.btnBack.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE
                       },0);
                if (edtAuthor.getText().toString().isEmpty() && edtPackName.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Sticker null", Toast.LENGTH_SHORT).show();
                } else if (imgListChoosed.size() == 0 || imgListChoosed.size() < 3 || imgListChoosed.size() > 30) {
                    Toast.makeText(this, "You need choose 3 sticker", Toast.LENGTH_SHORT).show();
                } else {
                    String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + edtPackName.getText() + "/";
                    if (!new File(dirName).exists()) {
                        Log.e(TAG, "onViewClicked 2: " + new File(dirName).mkdirs() + "   " + dirName);
                    }
                    Log.e(TAG, "onViewClicked 3: " + dirName);
                    for(int i=0; i < imgListChoosed.size(); i++){
                        File file1 = new File(dirName);
                        File file2 = new File(imgListChoosed.get(i).toString());
                        try{
                            exportFile(file2, file1);
                            Log.e(TAG, "onViewClicked 5: "+ file1);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    onBackPressed();
                }
                break;
        }
    }
    private File exportFile(File src, File dst) throws IOException {
        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File expFile = new File(dst.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
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
                Uri uri = data.getData();
                startCrop(uri);
            }
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri mImageUri = UCrop.getOutput(data);
            String mImg = mImageUri.toString();
            imgListChoosed.add(mImg);
            itemChooseAdapter.notifyDataSetChanged();
            txtNumChoosed.setText(imgListChoosed.size() + "/30");
            txtTapOn.setVisibility(View.GONE);
        }
    }
    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMG_NAME;
        destinationFileName += ".jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        //uCrop.withAspectRatio(3,4);
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
        return options;
    }
}
