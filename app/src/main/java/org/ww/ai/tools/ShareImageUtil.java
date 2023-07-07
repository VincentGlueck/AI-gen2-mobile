package org.ww.ai.tools;

import static org.ww.ai.ui.DialogUtil.DIALOG_UTIL;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.ww.ai.rds.entity.RenderResult;

public class ShareImageUtil {


    private static final String WHATS_APP_PACKAGE = "com.whatsapp" ;
    private static final String INTENT_TYPE = "image/jpeg";

    private void shareWhatsapp(Fragment fragment, Uri imgUri) {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        if(imgUri != null) {
            whatsappIntent.setType(INTENT_TYPE);
            whatsappIntent.setPackage(WHATS_APP_PACKAGE);
            whatsappIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
        }
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            if(fragment.getActivity() != null) {
                fragment.getActivity().startActivity(whatsappIntent);
            }
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(fragment.getContext(), "Unable to share", Toast.LENGTH_LONG).show();
        }
    }

    private Uri getUriFromRenderResult(RenderResult renderResult) {
        return null;
    }
}
