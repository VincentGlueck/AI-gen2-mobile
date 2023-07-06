package org.ww.ai.tools;

import android.view.View;

import com.google.mlkit.nl.translate.Translator;

public interface TranslationAvailableNotifierIF {

    void onTranslationAvailable();

    void notifyTranslationAvailable(View view, Translator translator);

}
