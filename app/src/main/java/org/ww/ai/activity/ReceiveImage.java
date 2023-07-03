package org.ww.ai.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.rds.ifenum.RenderModel;
import org.ww.ai.ui.ImageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class ReceiveImage extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_image);
        Uri bitmapUri = (Uri) getIntent().getExtras().get(MainActivity.KEY_BITMAP);
        TextView textView = findViewById(R.id.receive_title);
        textView.setText(R.string.receive_result_header);

        Bitmap bitmap = getBitmapFromUri(bitmapUri);
        if(bitmap != null) {
            ImageView imageView = findViewById(R.id.receive_bitmap);
            imageView.setImageBitmap(bitmap);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(WhatToRender.class.getCanonicalName(), Context.MODE_PRIVATE);
        WhatToRenderIF whatToRender = new WhatToRender();
        whatToRender.getFromPreferences(preferences);
        fillRenderValues(whatToRender);
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
            bitmap = ImageUtil.IMAGE_UTIL.getScaledBitmap(bitmap, 1024);
        } catch (IOException e) {
            Toast.makeText(this, "ERR: unable to get image: " + uri.toString(), Toast.LENGTH_LONG).show();
        }
        return bitmap;
    }

}