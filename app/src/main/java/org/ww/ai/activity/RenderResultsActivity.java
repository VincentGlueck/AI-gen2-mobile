package org.ww.ai.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.RenderResultAdapter;
import org.ww.ai.ui.SwipeToDeleteCallback;

import java.util.List;

public class RenderResultsActivity extends AppCompatActivity implements RenderResultAdapter.OnItemClickListener {

    private RenderResultAdapter adapter;
    private LinearLayout linearLayout;

    private RecyclerView renderResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.render_result_history);

        renderResultView = findViewById(R.id.render_result_List);

        adapter = new RenderResultAdapter(this, this);
        renderResultView.setAdapter(adapter);
        renderResultView.setLayoutManager(new LinearLayoutManager(this));
        getRenderResultsFromDatabase();

        linearLayout = findViewById(R.id.render_result_linear_layout);
        enableSwipeToDeleteAndUndo();

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(click -> {
            finish();
        });
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                RenderResultLightWeight renderResultLightWeight = adapter.itemAt(position);
                adapter.removeResult(position);

                Snackbar snackbar = Snackbar
                        .make(linearLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", view -> {
                    adapter.restoreResult(renderResultLightWeight, position);
                    renderResultView.scrollToPosition(position);
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(renderResultView);
    }

    private void getRenderResultsFromDatabase() {
        AppDatabase appDatabase = AppDatabase.getInstance(getApplication());
        ListenableFuture<List<RenderResult>> listenableFuture = appDatabase.renderResultDao().getAll();
        AsyncDbFuture<List<RenderResult>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, renderResults -> {
            adapter.addRenderResults(renderResults);
        }, getApplication());
    }

    @Override
    public void onItemClick(RenderResultLightWeight item) {
        Intent intent = new Intent(getApplication(), RenderResultDetailsActivity.class);
        startActivity(intent);
    }

}
