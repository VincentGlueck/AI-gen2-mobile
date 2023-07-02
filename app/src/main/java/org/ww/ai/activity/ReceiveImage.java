package org.ww.ai.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import org.ww.ai.R;

public class ReceiveImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_image);

        ImageView imageView = findViewById(R.id.receive_bitmap);
        Bitmap bitmap = (Bitmap) getIntent().getExtras().get(MainActivity.KEY_BITMAP);
        imageView.setImageBitmap(bitmap);
    }
}