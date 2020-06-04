package stickermaker.wastickerapps.stickersforwhatsapp.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class MediaScannerWrapper implements MediaScannerConnection.MediaScannerConnectionClient {

    private Context context;
    private String mPath;
    private String mMimeType;
    private MediaScannerConnection mConnection;

    public MediaScannerWrapper(Context context, String mPath, String mMimeType) {
        this.context = context;
        this.mPath = mPath;
        this.mMimeType = mMimeType;
        mConnection = new MediaScannerConnection(context, this);
    }

    public void scan() {
        mConnection.connect();
    }

    // start the scan when scanner is ready
    @Override
    public void onMediaScannerConnected() {
        mConnection.scanFile(mPath, mMimeType);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        // when scan is completes, update media file tags
    }
}

