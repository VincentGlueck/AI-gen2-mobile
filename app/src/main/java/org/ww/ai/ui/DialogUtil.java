package org.ww.ai.ui;

import static org.ww.ai.tools.FileUtil.FILE_UTIL;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.FileUtils;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.ww.ai.R;
import org.ww.ai.tools.FileUtil;

import java.util.concurrent.atomic.AtomicReference;

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
        String shortTitle = title.length() > 20 ? (title.substring(0, 18)) + "..." : title;
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(shortTitle)
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

    public AlertDialog.Builder createHtmlDialogBoxBuilder(Context context, int titleResourceId, String htmlUrlOrData,
                                                          boolean cancelable,
                                                          int positiveResourceId,
                                                          DialogInterface.OnClickListener positiveListener,
                                                          int negativeResourceId,
                                                          DialogInterface.OnClickListener negativeListener,
                                                          DialogInterface.OnDismissListener dismissListener,
                                                          int... iconResourceId) {
        return createHtmlDialogBoxBuilderHolder(context, titleResourceId, htmlUrlOrData,
                cancelable, positiveResourceId, positiveListener,
                negativeResourceId, negativeListener, dismissListener, iconResourceId
        ).builder;
    }

    public AlertDialogBuilderHolder createHtmlDialogBoxBuilderHolder(Context context,
                                                                     int titleResourceId,
                                                                     String htmlUrlOrData,
                                                                     boolean cancelable,
                                                                     int positiveResourceId,
                                                                     DialogInterface.OnClickListener positiveListener,
                                                                     int negativeResourceId,
                                                                     DialogInterface.OnClickListener negativeListener,
                                                                     DialogInterface.OnDismissListener dismissListener,
                                                                     int... iconResourceId) {
        AlertDialogBuilderHolder resultHolder = new AlertDialogBuilderHolder();
        final View htmlView = View.inflate(context, R.layout.html_dialog_view, null);
        final TextView htmlText = htmlView.findViewById(R.id.html);
        String text = "";
        if(htmlUrlOrData.endsWith(".html") || htmlUrlOrData.endsWith(".txt")) {
            text = FILE_UTIL.loadStringAsset(context, htmlUrlOrData);
        } else {
            text = htmlUrlOrData;
        }
        htmlText.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(htmlView)
                .setCancelable(cancelable)
                .setPositiveButton(R.string.dialogOk, (dialogInterface, i) -> dialogInterface.dismiss());
        if(titleResourceId != -1) {
            builder.setTitle(titleResourceId);
        }
        if(positiveListener != null && positiveResourceId > -1) {
            builder.setPositiveButton(positiveResourceId, positiveListener);
        }
        if(negativeListener != null && negativeResourceId > -1) {
            builder.setNegativeButton(negativeResourceId, negativeListener);
        }
        if(dismissListener != null) {
            builder.setOnDismissListener(dismissListener);
        }
        if(iconResourceId.length > 0) {
            builder.setIcon(iconResourceId[0]);
        }
        resultHolder.builder = builder;
        return resultHolder;
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

    public static class AlertDialogBuilderHolder {
        public AlertDialog.Builder builder;
        public AlertDialog dialog;
        public AtomicReference<CountDownTimer> countDownTimer;
    }

}
