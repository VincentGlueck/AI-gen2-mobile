package org.ww.ai.activity;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.ActivityMainBinding;
import org.ww.ai.ui.DialogUtil;

public class MainActivity extends AppCompatActivity {

    public final static String KEY_BITMAP = "bitmap";
    public static final String KEY_WHAT_TO_RENDER = "whatToRender";
    private AppBarConfiguration appBarConfiguration;

    private WhatToRenderIF lastRender;
    private ActivityMainBinding binding;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> Log.d("ON_RESULT", uri.toString()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

/*
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        Log.d("DATABASE", "entries: " + appDatabase.renderResultDao());
        if(appDatabase.renderResultDao() != null) {
            LiveData<List<RenderResult>> liveData = appDatabase.renderResultDao().getAll();
            liveData.observe(this, renderResults -> {
                if(renderResults != null && !renderResults.isEmpty()) {
                    renderResults.forEach(r -> {
                        Log.d("RENDER_RESULT", r.toString());
                    });
                }
            });
        }
        */

        checkIntentPurpose();

    }

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
        startActivity(intent);
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


}