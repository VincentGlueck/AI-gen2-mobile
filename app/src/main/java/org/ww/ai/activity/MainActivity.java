package org.ww.ai.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.ActivityMainBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.ui.DialogUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String KEY_BITMAP = "bitmap";
    public static final String KEY_WHAT_TO_RENDER = "whatToRender";
    private AppBarConfiguration appBarConfiguration;

    private WhatToRenderIF lastRender;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        deleteAllTestRecords(AppDatabase.getInstance(this));

        checkIntentPurpose();

    }

    private void deleteAllTestRecords(AppDatabase appDatabase) {
        ListenableFuture<List<RenderResult>> listenableFuture = appDatabase.renderResultDao().getAll();
        AsyncDbFuture<List<RenderResult>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, renderResults -> {
            if(!renderResults.isEmpty()) {
                ListenableFuture<Integer> integerListenableFuture = appDatabase.renderResultDao().deleteRenderResults(renderResults);
                AsyncDbFuture<Integer> integerAsyncDbFuture = new AsyncDbFuture<>();
                integerAsyncDbFuture.processFuture(integerListenableFuture, result -> {
                    Toast.makeText(this, "deleted: " + result.toString(), Toast.LENGTH_LONG).show();
                }, this);
            }
        }, this);
    }

    /*
    private void createTestRecord(AppDatabase appDatabase) {
        RenderResult renderResult = new RenderResult();
        renderResult.queryString = "Dummy";
        renderResult.createdTime = System.currentTimeMillis();
        renderResult.renderEngine = RenderModel.SDXL_0_9;
        ListenableFuture<Void> listenableFuture = appDatabase
                .renderResultDao().insertRenderResult(renderResult);
        AsyncDbFuture<Void> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, callback -> {
            Log.d("UPDATE", "updated...");
        }, this);
    }
     */

    private void checkIntentPurpose() {
        ClipData clipData = getIntent().getClipData();
        if(clipData != null) {
            if(clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                if(item.getUri() != null) {
                    startReceiveImageActivity(item.getUri());
                }
            }
        }
    }

    private void startReceiveImageActivity(Uri uri) {
        Intent intent = new Intent(this, ReceiveImage.class);
        intent.putExtra(KEY_BITMAP, uri);
        intent.putExtra(KEY_WHAT_TO_RENDER, lastRender);
        someActivityResultLauncher.launch(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setLastQuery(WhatToRenderIF whatToRenderIF) {
        lastRender = whatToRenderIF;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    showWhatToDoWithResultDialog(data);
                }
            });

    private void showWhatToDoWithResultDialog(Intent data) {
        if (data != null && data.getSerializableExtra(RenderResult.class.getCanonicalName()) != null) {
            DialogUtil.DIALOG_UTIL.showPrompt(
                    this,
                    getResources().getString(R.string.title_result_stored),
                    getResources().getString(R.string.msg_result_stored),
                    R.string.btn_show_in_history, (d, i1) -> {
                        d.dismiss();
                        Intent intent = new Intent(this, RenderResultsActivity.class);
                        startActivity(intent);
                    },
                    R.string.btn_back, (d, i1) -> {
                        d.dismiss();
                    },
                    R.drawable.info
            );
        }
    }


}