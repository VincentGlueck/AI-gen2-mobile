package org.ww.ai.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.units.qual.A;
import org.ww.ai.R;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.ifenum.RenderModel;
import org.ww.ai.ui.ImageUtil;

import java.text.DateFormat;
import java.util.Date;

public class RenderResultDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_render_result_full);
        int uid = getIntent().getIntExtra("uid", Integer.MIN_VALUE);
        if(uid > Integer.MIN_VALUE) {
            loadRenderResultFromDatabase(uid);
        }
        Button btnBack = findViewById(R.id.history_detail_back);
        btnBack.setOnClickListener(click -> {
            finish();
        });

    }

    private void loadRenderResultFromDatabase(int uid) {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if(result != null) {
                fillContentViewFromResult(result);
            }
        }, getApplicationContext());
    }

    private void fillContentViewFromResult(RenderResult result) {
        ImageView imageView = findViewById(R.id.history_bitmap);
        imageView.setImageBitmap(ImageUtil.IMAGE_UTIL.convertBlobToImage(result.image));
        EditText textViewWhatWasRendered = findViewById(R.id.what_was_rendered_value);
        textViewWhatWasRendered.setText(result.queryString);
        EditText textViewWhatWasUsed = findViewById(R.id.what_was_rendered_query_value);
        textViewWhatWasUsed.setText(result.queryUsed);
        TextView textViewCredits = findViewById(R.id.what_was_rendered_credits);
        textViewCredits.setText(String.valueOf(result.credits));
        TextView textViewDate = findViewById(R.id.what_was_rendered_date);
        textViewDate.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(new Date(result.createdTime)));
        Spinner spinnerRenderedBy = findViewById(R.id.what_was_rendered_engine_spinner);
        ArrayAdapter<String> renderedByAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, RenderModel.getAvailableModels());
        renderedByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRenderedBy.setAdapter(renderedByAdapter);
        spinnerRenderedBy.setSelection(result.renderEngine.ordinal());
        spinnerRenderedBy.setEnabled(false);
    }

}
