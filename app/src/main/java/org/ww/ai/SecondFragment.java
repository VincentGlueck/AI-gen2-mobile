package org.ww.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.ww.ai.data.SettingsCollection;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.FragmentSecondBinding;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.parser.Parser;
import org.ww.ai.phrase.PhraseGenerator;
import org.ww.ai.phrase.PhraseGeneratorException;
import org.ww.ai.phrase.PraseGeneratorErrorHandlerIF;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class SecondFragment extends Fragment implements PraseGeneratorErrorHandlerIF {

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
        renderResultText();
    }

    private void renderResultText() {
        LinearLayout linearLayout = view.findViewById(R.id.lin_result);
        // TODO remove after it works ;-)
        whatToRender.setPhraseCount(4);
        PhraseGenerator phraseGenerator = new PhraseGenerator(whatToRender, settingsCollection, this);

        List<String> textList = phraseGenerator.getAITextAsList();
        for(String text : textList) {
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.single_result, null);
            final EditText editText = relativeLayout.findViewById(R.id.textview_result);
            editText.setText(text);
            editText.setSelectAllOnFocus(true);
            linearLayout.addView(relativeLayout);
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