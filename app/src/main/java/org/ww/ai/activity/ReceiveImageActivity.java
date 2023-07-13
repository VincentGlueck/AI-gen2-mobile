package org.ww.ai.activity;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.rds.ifenum.RenderModel;
import org.ww.ai.ui.DialogUtil;
import org.ww.ai.ui.ImageUtil;
import org.ww.ai.ui.inclues.RenderModelsUI;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class ReceiveImageActivity extends AppCompatActivity {

    public static final int MAX_IMAGE_SIZE = 2048;
    private Bitmap bitmap;

    private WhatToRenderIF whatToRender;

    private RenderModelsUI renderModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_image);
        Uri bitmapUri = (Uri) getIntent().getExtras().get(MainActivity.KEY_BITMAP);
        TextView textView = findViewById(R.id.receive_title);
        textView.setText(R.string.receive_result_header);

        bitmap = getBitmapFromUri(bitmapUri);
        if (bitmap != null) {
            SubsamplingScaleImageView imageView = findViewById(R.id.receive_bitmap);
            IMAGE_UTIL.setFittingImageViewFromBitmap(this, imageView, bitmap);
        }
        Button btnCancel = findViewById(R.id.btn_result_back);
        btnCancel.setOnClickListener(click -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
        Button btnSave = findViewById(R.id.btn_result_save);
        btnSave.setOnClickListener(click -> saveResult());
    }

    private void saveResult() {
        RenderResult renderResult = new RenderResult();
        if (bitmap != null) {
            Bitmap thumbNail = IMAGE_UTIL.getScaledBitmap(bitmap, ImageUtil.THUMB_NAIL_SIZE);
            renderResult.thumbNail = IMAGE_UTIL.convertImageToBlob(thumbNail);
            renderResult.image = IMAGE_UTIL.convertImageToBlob(bitmap);
            renderResult.width = bitmap.getWidth();
            renderResult.height = bitmap.getHeight();
        }
        // Spinner engineSpinner = findViewById(R.id.what_was_rendered_engine_spinner);
        // EditText creditsText = findViewById(R.id.what_was_rendered_credits);

        renderResult.queryString = whatToRender.getDescription();
        renderResult.queryUsed = whatToRender.getQueryUsed();
//        try {
//            renderResult.credits = Integer.parseInt(creditsText.getText().toString());
//        } catch (NumberFormatException e) {
//            renderResult.credits = 0;
//        }
//        String selectedItem = (String) engineSpinner.getSelectedItem();
//        renderResult.renderEngine = RenderModel.fromName(selectedItem);
        renderResult.createdTime = System.currentTimeMillis();
        storeToDatabase(renderResult);
    }

    private void storeToDatabase(RenderResult renderResult) {
        AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
        ListenableFuture<Long> listenableFuture = appDatabase.renderResultDao().insertRenderResult(renderResult);
        AsyncDbFuture<Long> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, id -> {
            RenderResultLightWeight lightWeight = RenderResultLightWeight.fromRenderResult(renderResult);
            lightWeight.uid = id.intValue();
            finishWithResult(lightWeight);
        }, getApplicationContext());
    }

    private void finishWithResult(RenderResultLightWeight renderResult) {
        Intent intent = new Intent();
        intent.putExtra(RenderResultLightWeight.class.getCanonicalName(), renderResult);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(WhatToRender.class.getCanonicalName(), Context.MODE_PRIVATE);
        whatToRender = new WhatToRender();
        whatToRender.getFromPreferences(preferences);
        fillRenderValues(whatToRender);
        checkIfResultExists(whatToRender);
    }

    private void checkIfResultExists(WhatToRenderIF whatToRender) {
        if (whatToRender.getQueryUsed() == null || whatToRender.getQueryUsed().isEmpty()
                || "undefined".equals(whatToRender.getQueryUsed())) {
            DialogUtil.DIALOG_UTIL.showMessage(this, R.string.title_no_query, R.string.message_no_query, R.drawable.warning);
        }
    }

    private void fillRenderValues(WhatToRenderIF whatToRender) {
        TextView whatWasRendered = findViewById(R.id.what_was_rendered_value);
        whatWasRendered.setText(whatToRender.getDescription());
        TextView whatWasRenderedDetail = findViewById(R.id.what_was_rendered_query_value);
        whatWasRenderedDetail.setText(whatToRender.getQueryUsed());
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        TextView whatWasRenderedDate = findViewById(R.id.what_was_rendered_date);
        whatWasRenderedDate.setText(dateFormat.format(new Date(System.currentTimeMillis())));
        View view = findViewById(R.id.render_models_root);
        ((RenderModelsUI) view).init(this, view);
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bitmap = null;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in);
            bitmap = IMAGE_UTIL.getScaledBitmap(bitmap, MAX_IMAGE_SIZE);
        } catch (IOException e) {
            Toast.makeText(this, "ERR: unable to get image: " + uri.toString(), Toast.LENGTH_LONG).show();
        }
        return bitmap;
    }

}