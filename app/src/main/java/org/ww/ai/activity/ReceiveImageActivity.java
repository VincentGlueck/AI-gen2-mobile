package org.ww.ai.activity;

import static org.ww.ai.enumif.EventTypes.MODEL_ADDED;
import static org.ww.ai.enumif.EventTypes.MODEL_REMOVED;
import static org.ww.ai.event.EventBroker.EVENT_BROKER;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.enumif.ReceiveEventIF;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.DialogUtil;
import org.ww.ai.ui.ImageUtil;
import org.ww.ai.ui.inclues.RenderModelsUI;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class ReceiveImageActivity extends AppCompatActivity implements ReceiveEventIF {

    public static final int MAX_IMAGE_SIZE = 2048;
    private static final int MAX_PREVIEW_SIZE = 720;
    private Bitmap mBitmap;

    private WhatToRenderIF mWhatToRender;

    private RenderModelsUI mRenderModels;

    private Button mBtnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_image);
        assert getIntent() != null;
        assert getIntent().getExtras() != null;
        Uri bitmapUri = (Uri) getIntent().getExtras().get(MainActivity.KEY_BITMAP);
        TextView textView = findViewById(R.id.receive_title);
        textView.setText(R.string.receive_result_header);

        Button btnCancel = findViewById(R.id.btn_result_back);
        btnCancel.setOnClickListener(click -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        mBtnSave = findViewById(R.id.btn_result_save);
        mBtnSave.setOnClickListener(click -> saveResult());
        mBtnSave.setEnabled(false);

        ImageView imageView = findViewById(R.id.receive_bitmap);
        mBitmap = getBitmapFromUri(bitmapUri);
        if (mBitmap != null) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(32));
            Glide.with(this)
                    .asBitmap()
                    .load(mBitmap)
                    .override(MAX_PREVIEW_SIZE)
                    .apply(requestOptions)
                    .into(imageView);
        }

        EVENT_BROKER.registerReceiver(this, MODEL_ADDED, MODEL_REMOVED);
    }


    private void saveResult() {
        RenderResult renderResult = new RenderResult();
        if (mBitmap != null) {
            Bitmap thumbNail = IMAGE_UTIL.getScaledBitmap(mBitmap, ImageUtil.THUMB_NAIL_SIZE);
            renderResult.thumbNail = IMAGE_UTIL.convertImageToBlob(thumbNail);
            renderResult.image = IMAGE_UTIL.convertImageToBlob(mBitmap);
            renderResult.width = mBitmap.getWidth();
            renderResult.height = mBitmap.getHeight();
        }
        renderResult.queryString = mWhatToRender.getDescription();
        renderResult.queryUsed = mWhatToRender.getQueryUsed();

        renderResult.enginesUsed = mRenderModels.getmEngineList();

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
        mWhatToRender = new WhatToRender();
        mWhatToRender.getFromPreferences(preferences);
        fillRenderValues(mWhatToRender);
        checkIfResultExists(mWhatToRender);
    }

    private void checkIfResultExists(WhatToRenderIF whatToRender) {
        if (whatToRender.getQueryUsed() == null || whatToRender.getQueryUsed().isEmpty()
                || "undefined".equals(whatToRender.getQueryUsed())) {
            DialogUtil.DIALOG_UTIL.showMessage(this, R.string.title_no_query,
                    R.string.message_no_query, R.drawable.warning);
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
        mRenderModels = (RenderModelsUI) view;
        mRenderModels.init(this, view);
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bitmap = null;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in);
            bitmap = IMAGE_UTIL.getScaledBitmap(bitmap, MAX_IMAGE_SIZE);
        } catch (IOException e) {
            Toast.makeText(this, "ERR: unable to get image: " + uri, Toast.LENGTH_LONG).show();
        }
        return bitmap;
    }

    @Override
    public void receiveEvent(Object... eventObjects) {
        mBtnSave.setEnabled(!mRenderModels.getmEngineList().isEmpty());
    }
}