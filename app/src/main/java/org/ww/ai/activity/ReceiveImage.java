package org.ww.ai.activity;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.ifenum.RenderModel;
import org.ww.ai.ui.DialogUtil;
import org.ww.ai.ui.ImageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ReceiveImage extends AppCompatActivity {

    private Bitmap bitmap;

    private WhatToRenderIF whatToRender;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_image);
        Uri bitmapUri = (Uri) getIntent().getExtras().get(MainActivity.KEY_BITMAP);
        TextView textView = findViewById(R.id.receive_title);
        textView.setText(R.string.receive_result_header);

        bitmap = getBitmapFromUri(bitmapUri);
        if(bitmap != null) {
            ImageView imageView = findViewById(R.id.receive_bitmap);
            imageView.setImageBitmap(bitmap);
        }
        Button btnCancel = findViewById(R.id.btn_result_back);
        btnCancel.setOnClickListener(click -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
        Button btnSave = findViewById(R.id.btn_result_save);
        btnSave.setOnClickListener(click -> {
            saveResult();
        });
    }

    private void saveResult() {
        RenderResult renderResult = new RenderResult();
        if(bitmap != null) {
            Bitmap thumbNail = IMAGE_UTIL.getScaledBitmap(bitmap, 128);
            renderResult.thumbNail = IMAGE_UTIL.convertImageToBlob(thumbNail);
            renderResult.image = IMAGE_UTIL.convertImageToBlob(bitmap);
        }
        Spinner engineSpinner = findViewById(R.id.what_was_rendered_engine_spinner);
        EditText creditsText = findViewById(R.id.what_was_rendered_credits);

        renderResult.queryString = whatToRender.getDescription();
        renderResult.queryUsed = whatToRender.getQueryUsed();
        try {
            renderResult.credits = Integer.parseInt(creditsText.getText().toString());
        } catch (NumberFormatException e) {
            renderResult.credits = 0;
        }
        String selectedItem = (String) engineSpinner.getSelectedItem();
        renderResult.renderEngine = RenderModel.fromName(selectedItem);
        renderResult.createdTime = System.currentTimeMillis();
        storeToDatabase(renderResult);
    }

    private void storeToDatabase(RenderResult renderResult) {
        AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
        ListenableFuture<Void> listenableFuture = appDatabase.renderResultDao().insertRenderResult(renderResult);
        AsyncDbFuture<Void> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, dummy -> {
            finishWithResult(renderResult);
        }, getApplicationContext());
    }

    private void finishWithResult(RenderResult renderResult) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(RenderResult.class.getCanonicalName(), renderResult);
        intent.putExtras(bundle);
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
        if(whatToRender.getQueryUsed() == null || whatToRender.getQueryUsed().isEmpty()
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
        Spinner renderedBySpinner = findViewById(R.id.what_was_rendered_engine_spinner);
        ArrayAdapter<String> renderedByAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, RenderModel.getAvailableModels());
        renderedByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        renderedBySpinner.setAdapter(renderedByAdapter);
        renderedBySpinner.setSelection(RenderModel.SDXL_BETA.ordinal());
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bitmap = null;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in);
            bitmap = IMAGE_UTIL.getScaledBitmap(bitmap, 1024);
        } catch (IOException e) {
            Toast.makeText(this, "ERR: unable to get image: " + uri.toString(), Toast.LENGTH_LONG).show();
        }
        return bitmap;
    }

}