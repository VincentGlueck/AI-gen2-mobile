package org.ww.ai.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.ww.ai.R;
import org.ww.ai.data.RenderResult;
import org.ww.ai.data.SettingsCollection;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.FragmentSecondBinding;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.parser.Parser;
import org.ww.ai.phrase.PhraseGenerator;
import org.ww.ai.phrase.PhraseGeneratorException;
import org.ww.ai.phrase.PhraseGeneratorErrorHandlerIF;
import org.ww.ai.ui.DialogUtil;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class SecondFragment extends Fragment implements PhraseGeneratorErrorHandlerIF {

    private FragmentSecondBinding binding;

    private Context containerContext;

    private SettingsCollection settingsCollection;

    private WhatToRenderIF whatToRender;

    private View view;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        super.onCreateView(inflater, container, savedInstanceState);

        assert container != null;
        this.containerContext = container.getContext();

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        initEngine(context);
    }

    private void initEngine(Context context) {
        Parser parser = new Parser();
        try {
            settingsCollection = parser.getSettings(context, FirstFragment.GENERATOR_RULES);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(view1 -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment));

        this.view = view;
        if(whatToRender == null) {
            whatToRender = new WhatToRender();
        }
        ImageView refreshImageView = view.findViewById(R.id.refresh_results);
        refreshImageView.setOnClickListener(l -> {
            reRenderResults();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = containerContext.getSharedPreferences(WhatToRender.class.getCanonicalName(), Context.MODE_PRIVATE);
        whatToRender = new WhatToRender();
        whatToRender.getFromPreferences(preferences);
        CheckBox checkInstantCopy = view.findViewById(R.id.instant_copy_to_clipboard);
        checkInstantCopy.setChecked(whatToRender.isInstantCopyToClipBoard());
        renderResultText();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = containerContext.getSharedPreferences(WhatToRender.class.getCanonicalName(), Context.MODE_PRIVATE);
        whatToRender.writeToSharedPreferences(preferences);
    }

    private void renderResultText() {
        LinearLayout linearLayout = view.findViewById(R.id.lin_result);
        CheckBox checkBox = view.findViewById(R.id.instant_copy_to_clipboard);
        checkBox.setOnCheckedChangeListener((v, checked) -> {
            whatToRender.setInstantCopyToClipBoard(checked);
        });
        PhraseGenerator phraseGenerator = new PhraseGenerator(whatToRender, settingsCollection, this);

        List<RenderResult> textList = phraseGenerator.getAITextsAsRenderResultList();
        for (RenderResult renderResult : textList) {
            FrameLayout frameLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.single_result, null);
            final EditText editText = frameLayout.findViewById(R.id.textview_result);
            editText.setText(renderResult.getSentence());
            editText.setSelectAllOnFocus(true);

            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && whatToRender.isInstantCopyToClipBoard()) {
                    copyToClipBoard(editText.getText());
                }
            });
            ImageView imageView = frameLayout.findViewById(R.id.btn_show_stats);
            imageView.setOnClickListener(l -> {
                DialogUtil.DIALOG_UTIL.showMessage(getContext(), R.string.render_result_dialog_title,
                        renderResult.toReadableForm(), R.drawable.info);
            });
            linearLayout.addView(frameLayout);
        }
    }

    private void reRenderResults() {
        LinearLayout linearLayout = view.findViewById(R.id.lin_result);
        linearLayout.removeAllViews();
        renderResultText();
    }

    private void copyToClipBoard(Editable text) {
        if(getContext() != null && getContext().getSystemService(Context.CLIPBOARD_SERVICE) != null) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ai gen-2 text", text.toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Text copied", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(containerContext, "Error copying to clipboard", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void handleGeneratorError(PhraseGeneratorException exception) {
        Toast.makeText(containerContext, exception.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleGeneratorError(PhraseGeneratorException exception, Severity severity) {
        handleGeneratorError(exception);
    }
}