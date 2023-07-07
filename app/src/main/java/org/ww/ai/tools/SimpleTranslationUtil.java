package org.ww.ai.tools;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Locale;

public class SimpleTranslationUtil implements TranslationAvailableNotifierIF {

    private static SimpleTranslationUtil instance;

    private static Translator translator;

    private boolean useTranslator = true;

    public static SimpleTranslationUtil getInstance(Context context) {
        Locale current = context.getResources().getConfiguration().getLocales().get(0);
        if (instance == null) {
            instance = new SimpleTranslationUtil(context, current.getLanguage());
        }
        return instance;
    }

    private SimpleTranslationUtil(Context context, String language) {
        if(TranslateLanguage.ENGLISH.equals(language)) {
            useTranslator = false;
            Toast.makeText(context, "Translation not required as you already use " +
                    language, Toast.LENGTH_LONG).show();
        }
        if (translator == null) {
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(language).setTargetLanguage(TranslateLanguage.ENGLISH).build();
            translator = Translation.getClient(options);
        }
    }

    public Translator getTranslator() {
        return translator;
    }

    public boolean isUseTranslator() {
        return useTranslator;
    }

    @Override
    public void onTranslationAvailable() {
        Log.d(getClass().getName(), "You should not ask me. I'll will send it back if avail");
    }

    @Override
    public void notifyTranslationAvailable(View view, Translator translator) {
        if (CheckBox.class.isAssignableFrom(view.getClass())) {
            ((CheckBox) view).setEnabled(true);
        } else {
            Toast.makeText(view.getContext(), "Sorry, not implemented, but nice to hear", Toast.LENGTH_LONG).show();
        }
    }

}
