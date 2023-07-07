package org.ww.ai.tools;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.ui.ImageUtil;

import java.io.File;
import java.io.IOException;

public class ShareImageUtil {
    private static final String WHATS_APP_PACKAGE = "com.whatsapp";
    private static final String INTENT_TYPE = "image/jpeg";

    private static ShareImageUtil instance;

    private Activity activity;

    public static ShareImageUtil getInstance(Activity activity) {
        if (instance == null) {
            instance = new ShareImageUtil(activity);
        }
        return instance;
    }

    private ShareImageUtil(Activity activity) {
        this.activity = activity;
    }

    private void shareWhatsapp(Uri imageUri) {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        if (imageUri != null) {
            whatsappIntent.setType(INTENT_TYPE);
            whatsappIntent.setPackage(WHATS_APP_PACKAGE);
            whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        }
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (activity != null && imageUri != null) {
            activity.startActivity(whatsappIntent);
        } else {
            Toast.makeText(activity, "sorry, no URI", Toast.LENGTH_LONG).show();
        }
    }

    public void startShare(RenderResult renderResult) {
        AppDatabase db = AppDatabase.getInstance(activity);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(renderResult.uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                Bitmap image = IMAGE_UTIL.convertBlobToImage(result.image);
                Toast.makeText(activity, "got image from db, size: " + image.getWidth(), Toast.LENGTH_LONG).show();
                Uri fileUri = getFileUri(renderResult);
                if (fileUri != null) {
                    // Grant temporary read permission to the content URI
                    shareWhatsapp(fileUri);
                }
            }
        }, activity);
        Uri uri = null;
        shareWhatsapp(uri);
    }

    private Uri getFileUri(RenderResult renderResult) {
        File file = null;
        try {
            file = File.createTempFile("share", "jpg");
            Uri uri = writeToJpg(renderResult, file);
            Log.d("URI", "to share: " + uri.toString());
        } catch (IOException e) {
            Log.e("GETFILEURL", "Can't create image");
        }

    }

    private Uri writeToJpg(RenderResult renderResult, File file) {
        Bitmap bitmap = IMAGE_UTIL.convertBlobToImage(renderResult.image);
        return null;
    }
}