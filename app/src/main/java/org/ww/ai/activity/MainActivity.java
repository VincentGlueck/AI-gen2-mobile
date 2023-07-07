package org.ww.ai.activity;

import static org.ww.ai.activity.RenderDetailsFragment.ARG_UID;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.ActivityMainBinding;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.ImageUtil;

public class MainActivity extends AppCompatActivity {

    public final static String KEY_BITMAP = "bitmap";
    public static final String KEY_WHAT_TO_RENDER = "whatToRender";
    private static final int SIZE_SNACK_THUMB_MAX = 96;
    private AppBarConfiguration appBarConfiguration;
    private WhatToRenderIF lastRender;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        org.ww.ai.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        coordinatorLayout = findViewById(R.id.main_activity_scroll_bar);

        checkIntentPurpose();

    }


    private void checkIntentPurpose() {
        boolean foundSomeThing = false;
        ClipData clipData = getIntent().getClipData();
        if (clipData != null) {
            if (clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item.getUri() != null) {
                    foundSomeThing = true;
                    startReceiveImageActivity(item.getUri());
                }
            }
        }
        if(!foundSomeThing && getIntent().getData() != null) {
            Uri data = getIntent().getData();
            startReceiveImageActivity(data);
        }
    }

    private void startReceiveImageActivity(Uri uri) {
        Intent intent = new Intent(this, ReceiveImageActivity.class);
        intent.putExtra(KEY_BITMAP, uri);
        intent.putExtra(KEY_WHAT_TO_RENDER, lastRender);
        receiveActivityResultLauncher.launch(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    public void setLastQuery(WhatToRenderIF whatToRenderIF) {
        lastRender = whatToRenderIF;
    }

    ActivityResultLauncher<Intent> receiveActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            showWantToSeeSnackBar(data);
        }
    });


    private void showWantToSeeSnackBar(Intent data) {
        final RenderResultLightWeight renderResult = (RenderResultLightWeight)
                data.getSerializableExtra(RenderResultLightWeight.class.getCanonicalName());
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Bitmap bitmap = null;
        if (renderResult != null && renderResult.thumbNail != null) {
            bitmap = ImageUtil.IMAGE_UTIL.convertBlobToImage(renderResult.thumbNail);
            if (bitmap != null) {
                bitmap = ImageUtil.IMAGE_UTIL.getScaledBitmap(bitmap, SIZE_SNACK_THUMB_MAX);
            }
        }
        builder.append(getText(R.string.history_entry_created_snackbar)).append("    ");
        builder.setSpan(new ImageSpan(MainActivity.this, bitmap), builder.length() - 1, builder.length(), 0);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, builder, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.YELLOW);

        snackbar.setAction(getText(R.string.history_entry_show_snackbar), view ->
                Toast.makeText(getApplicationContext(), R.string.history_entry_show_snackbar, Toast.LENGTH_LONG).show());
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event == 1) {
                    NavController navController = Navigation.findNavController(
                            MainActivity.this, R.id.nav_host_fragment_content_main);
                    Bundle bundle = new Bundle();
                    bundle.putInt(RenderDetailsFragment.ARG_UID,
                            renderResult != null ? renderResult.uid : Integer.MIN_VALUE);
                    navController.navigate(R.id.action_MainFragment_to_RenderResultsFragment, bundle);
                }
            }
        });
        snackbar.show();
    }

}