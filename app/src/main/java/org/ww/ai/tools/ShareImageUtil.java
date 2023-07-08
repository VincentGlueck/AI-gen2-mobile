package org.ww.ai.tools;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.ui.ImageUtil;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ShareImageUtil {
    private static final String WHATS_APP_PACKAGE = "com.whatsapp";
    private final Activity activity;

    public ShareImageUtil(Activity activity) {
        this.activity = activity;
    }

    private void shareImage(Bitmap bitmap, RenderResult renderResult) {
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentResolver contentResolver = activity.getContentResolver();
        ContentValues contentValues = new ContentValues();
        String name = "image_" + System.currentTimeMillis() + ".jpg";
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        Uri imageContentUri = contentResolver.insert(contentUri, contentValues);

        try (ParcelFileDescriptor fileDescriptor =
                     contentResolver.openFileDescriptor(imageContentUri, "w", null)) {
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            OutputStream outputStream = new FileOutputStream(fd);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            Log.e("ERROR", "Error saving bitmap", e);
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, imageContentUri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, renderResult.queryString);
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("image/jpeg");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share with");
        activity.startActivity(shareIntent);
    }

    public void startShare(RenderResult renderResult) {
        AppDatabase db = AppDatabase.getInstance(activity);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(renderResult.uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                Bitmap bitmap = ImageUtil.IMAGE_UTIL.convertBlobToImage(renderResult.image);
                if (bitmap != null) {
                    shareImage(bitmap, renderResult);
                }
            }
        }, activity);
    }

}