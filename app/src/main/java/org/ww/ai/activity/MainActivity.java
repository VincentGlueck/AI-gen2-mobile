package org.ww.ai.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ww.ai.R;
import org.ww.ai.databinding.ActivityMainBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.ui.ImageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
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

        installKeyBoardAutoHide();

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

        checkIntent();

    }

    private void checkIntent() {
        if(getIntent().getData() != null){
            InputStream in = null;
            try {
                Uri uri = getIntent().getData();
                in = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                Toast.makeText(this, "bitmap: " + bitmap.getByteCount(), Toast.LENGTH_LONG).show();
                bitmap = ImageUtil.IMAGE_UTIL.getScaledBitmap(bitmap, 1024);
                Toast.makeText(this, "after scale: " + bitmap.getByteCount(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }


    private void installKeyBoardAutoHide() {
        EditText editText = (EditText) findViewById(R.id.editTextTextMultiLine);
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private class MyFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if(v.getId() == R.id.editTextTextMultiLine && !hasFocus) {
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }
    }

}