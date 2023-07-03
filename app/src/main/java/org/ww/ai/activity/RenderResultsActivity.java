package org.ww.ai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.RenderResultAdapter;

import java.util.List;

public class RenderResultsActivity extends AppCompatActivity implements RenderResultAdapter.OnItemClickListener {

    private RenderResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.render_result_history);

        RecyclerView renderResultView = findViewById(R.id.render_result_List);

        adapter = new RenderResultAdapter(this);
        renderResultView.setAdapter(adapter);
        renderResultView.setLayoutManager(new LinearLayoutManager(this));
        getRenderResultsFromDatabase();

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(click -> {
            finish();
        });
    }

    private void getRenderResultsFromDatabase() {
        AppDatabase appDatabase = AppDatabase.getInstance(getApplication());
        // createTestRecord(appDatabase);
        ListenableFuture<List<RenderResult>> listenableFuture = appDatabase.renderResultDao().getAll();
        AsyncDbFuture<List<RenderResult>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, renderResults -> {
            adapter.addRenderResults(renderResults);
            // adapter.notifyItemRangeInserted(0, renderResults.size());
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Items added: " + renderResults.size(), Toast.LENGTH_LONG).show();
        }, getApplication());
    }

    @Override
    public void onItemClick(RenderResultLightWeight item) {
        Intent intent = new Intent(getApplication(), RenderResultDetailsActivity.class);
        startActivity(intent);
    }
}
