package org.ww.ai.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import org.ww.ai.R;

public enum DialogUtil {
    DIALOG_UTIL;

    public void showMessage(Context context, int titleResourceId, int messageResourceId, int... iconResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(titleResourceId)
                .setMessage(messageResourceId)
                .setPositiveButton(R.string.dialogOk, null);
        if(iconResourceId.length > 0) {
            builder.setIcon(iconResourceId[0]);
        }
        builder.show();
    }

    public void showMessage(Context context, int titleResourceId, String messageText, int... iconResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(titleResourceId)
                .setMessage(messageText)
                .setPositiveButton(R.string.dialogOk, null);
        if(iconResourceId.length > 0) {
            builder.setIcon(iconResourceId[0]);
        }
        builder.show();
    }

    public void showPrompt(Context context,
                           int titleResourceId,
                           int messageResourceId,
                           int resourcePositive,
                           final DialogInterface.OnClickListener positiveListener,
                           int resourceNegative,
                           final DialogInterface.OnClickListener negativeListener,
                           int... iconResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(titleResourceId)
                .setMessage(messageResourceId)
                .setPositiveButton(resourcePositive, positiveListener)
                .setNegativeButton(resourceNegative, negativeListener);
        if(iconResourceId.length > 0) {
            builder.setIcon(iconResourceId[0]);
        }
        builder.show();
    }


    public void showPrompt(Context context, String title, String msg,
                           int resourcePositive,
                           final DialogInterface.OnClickListener positiveListener,
                           int resourceNegative,
                           final DialogInterface.OnClickListener negativeListener,
                           int... iconResourceId) {

        buildPrompt(context, title, msg, resourcePositive, positiveListener,
                resourceNegative, negativeListener, iconResourceId).show();
    }

    public AlertDialog.Builder buildPrompt(Context context, String title, String msg,
                                           int resourcePositive,
                                           final DialogInterface.OnClickListener positiveListener,
                                           int resourceNegative,
                                           final DialogInterface.OnClickListener negativeListener,
                                           int... iconResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(resourcePositive, positiveListener)
                .setNegativeButton(resourceNegative, negativeListener);
        if(iconResourceId.length > 0) {
            builder.setIcon(iconResourceId[0]);
        }
        return builder;
    }


    public void showErrorMessage(Context context, String errText, DialogInterface.OnClickListener... positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.titleError)
                .setMessage(errText)
                .setIcon(R.drawable.error);
        if(positiveListener.length > 0) {
            builder.setPositiveButton(R.string.dialogWhoCares, positiveListener[0]);
        } else {
            builder.setPositiveButton(R.string.dialogWhoCares, null);
        }
        builder.show();
    }
    public void showHtmlDialog(Context context, int titleResourceId, String htmlUrl, int... iconResourceId) {
        final View htmlView = View.inflate(context, R.layout.html_dialog_view, null);
        final WebView html = htmlView.findViewById(R.id.html);
        html.loadUrl(htmlUrl);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(titleResourceId)
                .setView(htmlView)
                .setPositiveButton(R.string.dialogOk, null);
        if(iconResourceId.length > 0) {
            builder.setIcon(iconResourceId[0]);
        }
        builder.show();
    }

    public void showLargeTextDialog(Context context, int titleResourceId, String largeText, int... iconResourceId) {
        final View largeTextView = View.inflate(context, R.layout.large_text_dialog_view, null);
        final EditText editText = largeTextView.findViewById(R.id.large_text);
        editText.setText(largeText);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(titleResourceId)
                .setView(largeTextView)
                .setPositiveButton(R.string.dialogOk, null);
        if(iconResourceId.length > 0) {
            builder.setIcon(iconResourceId[0]);
        }
        builder.show();
    }

}
