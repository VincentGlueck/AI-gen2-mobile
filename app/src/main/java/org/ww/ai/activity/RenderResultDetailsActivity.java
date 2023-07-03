package org.ww.ai.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.ww.ai.R;

public class RenderResultDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_render_result_full);
        int uid = getIntent().getIntExtra("uid", Integer.MIN_VALUE);
        if(uid > Integer.MIN_VALUE) {
            loadRenderResultHistory(uid);
        }

    }

    private void loadRenderResultHistory(int uid) {
        Toast.makeText(getApplicationContext(), "Loading " + uid, Toast.LENGTH_LONG).show();
    }
}
