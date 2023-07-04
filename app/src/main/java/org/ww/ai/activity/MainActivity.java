package org.ww.ai.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.ActivityMainBinding;
import org.ww.ai.rds.entity.RenderResult;

public class MainActivity extends AppCompatActivity {

    public final static String KEY_BITMAP = "bitmap";
    public static final String KEY_WHAT_TO_RENDER = "whatToRender";
    private AppBarConfiguration appBarConfiguration;

    private WhatToRenderIF lastRender;
    private ActivityMainBinding binding;

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        coordinatorLayout = findViewById(R.id.main_activity_scroll_bar);

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
        receiveActivityResultLauncher.launch(intent);
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

    ActivityResultLauncher<Intent> receiveActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    showWantToSeeSnackBar(data);
                }
            });

    private void showWantToSeeSnackBar(Intent data) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, getText(R.string.history_entry_create_snackbar), Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.history_entry_show_snackbar), view -> {
            Toast.makeText(this, "data: " +
                    data.getBundleExtra(RenderResult.class.getCanonicalName()),
                    Toast.LENGTH_LONG).show();
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

}