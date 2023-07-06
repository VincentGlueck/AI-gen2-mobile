package org.ww.ai.tools;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class SimpleTranslationUtil implements TranslationAvailableNotifierIF {

    private static SimpleTranslationUtil instance;
    private static final String sourceLanguage = TranslateLanguage.GERMAN;

    private static Translator translator;

    public static SimpleTranslationUtil getInstance() {
        if (instance == null) {
            instance = new SimpleTranslationUtil();
        }
        return instance;
    }

    private SimpleTranslationUtil() {
        if (translator == null) {
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage).setTargetLanguage(TranslateLanguage.ENGLISH).build();
            translator = Translation.getClient(options);
        }
    }

    public Translator getTranslator() {
        return translator;
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
