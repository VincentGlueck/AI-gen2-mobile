package org.ww.ai.ui;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public enum ImageUtil {
    IMAGE_UTIL;

    public byte[] convertImageToBlob(final Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap convertBlobToImage(final byte[] buffer) {
        return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
    }

    public Bitmap getThumbnail(Uri uri, ContentResolver resolver, double size) throws IOException {
        InputStream input = resolver.openInputStream(uri);

        Log.d("INPUT", "available: " + input.available());

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = Math.max(onlyBoundsOptions.outHeight, onlyBoundsOptions.outWidth);

        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = resolver.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
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
        bitmap = null;
        return newBitmap;
    }

    public Bitmap getRotated(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        return k == 0 ? 1 : k;
    }
}