package org.ww.ai.tools;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ShareImageUtil {
    private final Activity activity;

    public ShareImageUtil(Activity activity) {
        this.activity = activity;
    }

    private void shareImage(Bitmap bitmap, RenderResult renderResult, boolean includeText) {
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
        assert imageContentUri != null;

        try (ParcelFileDescriptor fileDescriptor =
                     contentResolver.openFileDescriptor(imageContentUri, "w", null)) {
            assert fileDescriptor != null;
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
        if(includeText) {
            sendIntent.putExtra(Intent.EXTRA_TEXT, renderResult.queryString);
        }
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("image/jpeg");

        List<ComponentName> excludedComponents  = new ArrayList<>();
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/*");

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> resInfo = activity.getPackageManager().queryIntentActivities(sendIntent, 0);
        resInfo.forEach(r -> {
            String packageName = r.activityInfo.packageName;
            if (packageName.startsWith(activity.getPackageName())) {
                excludedComponents.add(new ComponentName(packageName, r.activityInfo.name));
            }
        });
        Intent chooserIntent = Intent.createChooser(sendIntent,
                activity.getResources().getString(R.string.share_with_title));
        chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponents.toArray(new Parcelable[]{}));
        activity.startActivity(chooserIntent);
    }

    public void startShare(int uid, boolean includeText) {
        AppDatabase db = AppDatabase.getInstance(activity);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                Bitmap bitmap = IMAGE_UTIL.convertBlobToImage(result.image);
                if (bitmap != null) {
                    shareImage(bitmap, result, includeText);
                }
            }
        }, activity);
    }


}