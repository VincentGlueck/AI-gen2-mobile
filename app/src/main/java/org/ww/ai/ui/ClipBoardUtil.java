package org.ww.ai.ui;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClipBoardUtil {

    private static ClipBoardUtil instance;
    private final ClipboardManager clipboardManager;

    private ClipBoardUtil(ClipboardManager clipboardManager) {
        this.clipboardManager = clipboardManager;
    }

    public synchronized static ClipBoardUtil getInstance(Context context) {
        if(instance == null) {
            if(context == null) {
                throw new IllegalArgumentException("ClipBoardUtil has to be initialized with Context before first use!");
            }
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            instance = new ClipBoardUtil(clipboardManager);
        }
        return instance;
    }

    public String testContent() {
        List<String> mimeTypes = new ArrayList<>();
        ClipData clip = clipboardManager.getPrimaryClip();
        int mimeTypeCount = clip.getDescription().getMimeTypeCount();
        if(mimeTypeCount == 0) {
            return "no mimetype found in clipboard";
        }
        for(int n=0; n<mimeTypeCount; n++) {
            String mimeType = clip.getDescription().getMimeType(n);
            Log.d("MIMETYPE", mimeType);
            mimeTypes.add(mimeType);
        }
        return String.join(", ", mimeTypes);
    }



}
