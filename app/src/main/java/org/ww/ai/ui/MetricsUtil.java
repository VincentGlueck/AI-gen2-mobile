package org.ww.ai.ui;

import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

public class MetricsUtil {

    public static Screen getScreen(WindowManager windowManager) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return new Screen(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public static class Screen {
        public int width;
        public int height;

        public Screen(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
