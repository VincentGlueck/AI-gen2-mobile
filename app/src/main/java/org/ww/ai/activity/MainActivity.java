package org.ww.ai.activity;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;

import org.ww.ai.R;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.ActivityMainBinding;
import org.ww.ai.enumif.MenuEnableIF;
import org.ww.ai.fragment.GalleryFragment;
import org.ww.ai.fragment.LicenseFragment;
import org.ww.ai.fragment.MainFragment;
import org.ww.ai.fragment.RenderDetailsFragment;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.ImageUtil;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MenuEnableIF {

    public final static String KEY_BITMAP = "bitmap";
    public static final String KEY_WHAT_TO_RENDER = "whatToRender";
    private static final int SIZE_SNACK_THUMB_MAX = 96;
    private AppBarConfiguration mAppBarConfiguration;
    private WhatToRenderIF mLastRender;
    private CoordinatorLayout mCoordinatorLayout;
    private NavController mNavController;
    private NavHostFragment mNavHostFragment;
    private MenuProvider mMenuProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        org.ww.ai.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        mNavHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert mNavHostFragment != null;
        mNavController = mNavHostFragment.getNavController();
        mAppBarConfiguration = new AppBarConfiguration.Builder(mNavController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        mCoordinatorLayout = findViewById(R.id.main_activity_coordinator_layout);
        checkIntentPurpose();
    }

    private void addMenu() {
        mMenuProvider = new MenuProvider() {

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.mainmenu, menu);
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                try {
                    removeMenuProvider(mMenuProvider);
                    if (R.id.action_so_far == id) {
                        mNavController.navigate(R.id.action_MainFragment_to_RenderResultsFragment);
                    } else if (R.id.action_gallery == id) {
                        mNavController.navigate(R.id.action_MainFragment_to_ResultsGalleryFragment);
                    } else if (R.id.action_settings == id) {
                        mNavController.navigate(R.id.action_MainFragment_to_SettingsFragment);
                    } else if (R.id.action_license == id) {
                        mNavController.navigate(R.id.action_MainFragment_to_LicenseFragment);
                    } else {
                        return false;
                    }
                } catch (IllegalArgumentException e) {
                    Log.d("NAVGRAPH", "Sorry, no route for this. Trying to go back to MainFragment");
                    mNavController.navigate(R.id.MainFragment);
                    return false;
                }
                return true;
            }
        };
        addMenuProvider(mMenuProvider);
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
        if (!foundSomeThing && getIntent().getData() != null) {
            Uri data = getIntent().getData();
            startReceiveImageActivity(data);
        }
    }

    private void startReceiveImageActivity(Uri uri, int... replaceUid) {
        // ignore parameter for now
        if (uri.toString().toLowerCase(Locale.ROOT).endsWith(".xml")) {
            Toast.makeText(this, "XML pasted", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, ReceiveImageActivity.class);
        intent.putExtra(KEY_BITMAP, uri);
        intent.putExtra(KEY_WHAT_TO_RENDER, mLastRender);
        receiveActivityResultLauncher.launch(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }



    @Override
    public void addMenuIfRequired() {
        Fragment fragment = mNavHostFragment.getChildFragmentManager().getFragments().get(0);
        if(MainFragment.class.isAssignableFrom(fragment.getClass())) {
            addMenu();
        }
    }

    public void setLastQuery(WhatToRenderIF whatToRenderIF) {
        mLastRender = whatToRenderIF;
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
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_LONG);
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

    @Override
    public void onBackPressed() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if(LicenseFragment.class.isAssignableFrom(currentFragment.getClass())) {
            LicenseFragment licenseFragment = (LicenseFragment) currentFragment;
            if(licenseFragment.canGoBackMyself()) {
                return;
            }
        }
        super.onBackPressed();
    }

}