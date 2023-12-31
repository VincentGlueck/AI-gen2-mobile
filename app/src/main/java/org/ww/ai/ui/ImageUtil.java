package org.ww.ai.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public enum ImageUtil {
    IMAGE_UTIL;

    public static final int THUMB_NAIL_SIZE = 480;

    public byte[] convertImageToBlob(final Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap convertBlobToImage(final byte[] buffer) {
        return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
    }

    public BitmapDrawable convertBlobToDrawable(final byte[] buffer) {
        ByteArrayInputStream imageStream = new ByteArrayInputStream(buffer);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        return new BitmapDrawable(Resources.getSystem(), bitmap);
    }


    public Bitmap getScaledBitmap(Bitmap bitmap, int size) {
        int width = size;
        int height = size;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            double ratio = (double) bitmap.getWidth() / (double) size;
            height = (int) ((double) bitmap.getHeight() / ratio);
        } else {
            double ratio = (double) bitmap.getHeight() / (double) size;
            width = (int) ((double) bitmap.getWidth() / ratio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public Bitmap setOpacity(Bitmap bitmap, int opacity) {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(opacity < 0 ? 0 : Math.min(opacity, 100));
        canvas.drawBitmap(bitmap, 0, 0, alphaPaint);
        return newBitmap;
    }

    public Bitmap getRotated(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public Bitmap cropToSquare(Bitmap bitmap, int desiredSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(height, width);
        if (newWidth > desiredSize) {
            newWidth = desiredSize;
        }
        int newHeight = (height > width) ? height - (height - width) : height;
        int cropW = Math.max((width - height) >> 1, 0);
        int cropH = Math.max((height - width) >> 1, 0);
        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

    public Bitmap getBitmapFittingDisplayMetrics(Bitmap bitmap, DisplayMetrics displayMetrics) {
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        float scale = 1.0f;
        if (bitmap.getHeight() > bitmap.getWidth()) {
            if (bitmap.getHeight() > width) {
                scale = 0.85f * (float) bitmap.getHeight() * (float) height / (float) bitmap.getHeight();
            }
        } else {
            if (bitmap.getWidth() > width) {
                scale = (float) bitmap.getWidth() * (float) width / (float) bitmap.getWidth();
            }
        }
        return getScaledBitmap(bitmap, (int) scale);
    }

    public RectF getImageBounds(ImageView imageView) {
        RectF bounds = new RectF();
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            imageView.getImageMatrix().mapRect(bounds, new RectF(drawable.getBounds()));
        }
        return bounds;
    }

    public void setFittingImageViewFromBitmap(ComponentActivity activity,
                                              ImageView imageView, byte[] bytes) {
        Bitmap bitmap = IMAGE_UTIL.convertBlobToImage(bytes);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageView.setImageBitmap(IMAGE_UTIL.getBitmapFittingDisplayMetrics(bitmap, displayMetrics));
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        return k == 0 ? 1 : k;
    }

    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}