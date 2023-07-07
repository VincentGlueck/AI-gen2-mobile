package org.ww.ai.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.slider.Slider;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translator;

import org.ww.ai.R;
import org.ww.ai.data.AttributeValue;
import org.ww.ai.data.Setting;
import org.ww.ai.data.SettingsCollection;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.MainFragmentBinding;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.parser.Parser;
import org.ww.ai.tools.ResourceLoader;
import org.ww.ai.tools.SimpleTranslationUtil;
import org.ww.ai.tools.TranslationAvailableNotifierIF;
import org.ww.ai.ui.DialogUtil;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

public class MainFragment extends Fragment implements TranslationAvailableNotifierIF  {
    public static final String GENERATOR_RULES = "generator.xml";
    private static final String KEY_ARTIST_TYPE = "artisttype";
    private MainFragmentBinding binding;

    private SettingsCollection settingsCollection;

    private WhatToRenderIF whatToRender;
    private Context containerContext;
    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        assert container != null;
        this.containerContext = container.getContext();

        binding = MainFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        binding.btnNext.setOnClickListener(view1 -> NavHostFragment.findNavController(MainFragment.this).navigate(R.id.action_MainFragment_to_ShowSentencesFragment));

        binding.btnRenderResults.setOnClickListener(v -> NavHostFragment.findNavController(MainFragment.this).navigate(R.id.action_MainFragment_to_RenderResultsFragment));

        binding.btnGallery.setOnClickListener(v -> NavHostFragment.findNavController(MainFragment.this).navigate(R.id.action_MainFragment_to_ResultsGalleryFragment));

        if (whatToRender == null) {
            whatToRender = new WhatToRender();
        } else {
            whatToRender.setQueryUsed("");
        }

        EditText editText = view.findViewById(R.id.editTextTextMultiLine);

        addDescriptionTextListener(view);
        addCheckBoxListeners(view);
        addValuesToArtistTypeSpinner(view);
        addValuesToLayoutSpinner(view);
        addValuesToNumOfArtistsSpinner(view);
        addValuesToCameraSpinner(view);
        addValuesToResolutionSpinner(view);
        ImageView imageView = view.findViewById(R.id.btn_clear);
        imageView.setOnClickListener(click -> {
            editText.setText("");
        });
        initRandomWordsSlider(view);
        initSentencesCountSlider(view);
        CheckBox checkBoxTranslate = view.findViewById(R.id.check_translate);
        checkBoxTranslate.setEnabled(false);
        checkBoxTranslate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                translateEditText(editText.getText().toString());
            } else {
                whatToRender.setTranslateToEnglishDescription(editText.getText().toString());
            }

        });
        checkTranslation();
    }

    private void translateEditText(String str) {
        SimpleTranslationUtil instance = SimpleTranslationUtil.getInstance();
        if (instance != null) {
            instance.getTranslator().translate(str)
                    .addOnSuccessListener(s -> {
                        whatToRender.setTranslateToEnglishDescription(s);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(containerContext, getText(R.string.unable_to_translate)
                                + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                        whatToRender.setTranslateToEnglishDescription("Failure");
                    });

        } else {
            Toast.makeText(containerContext, "Sorry, NO TRANSLATOR", Toast.LENGTH_LONG).show();
        }
    }

    // TODO: maybe in a later version, again
    private void showGeneratorXML() {
        try {
            InputStream in = ResourceLoader.RESOURCE_LOADER.getResource(containerContext, GENERATOR_RULES);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            DialogUtil.DIALOG_UTIL.showLargeTextDialog(containerContext, R.string.title_activity_main, sb.toString());
        } catch (IOException e) {
            Toast.makeText(containerContext, "Error loading " + GENERATOR_RULES, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = containerContext.getSharedPreferences(WhatToRender.class.getCanonicalName(), Context.MODE_PRIVATE);
        whatToRender = new WhatToRender();
        whatToRender.getFromPreferences(preferences);
        EditText editText = view.findViewById(R.id.editTextTextMultiLine);
        editText.setText(whatToRender.getDescription(), TextView.BufferType.EDITABLE);
        CheckBox checkBoxNoLayout = view.findViewById(R.id.chk_no_layout);
        checkBoxNoLayout.setChecked(whatToRender.getPreset().isEmpty());
        CheckBox checkBoxNoArtist = view.findViewById(R.id.chk_no_artists);
        checkBoxNoArtist.setChecked(whatToRender.isUseNoArtists());
        CheckBox checkBoxRandomArtist = view.findViewById(R.id.chk_random_artist);
        checkBoxRandomArtist.setChecked(whatToRender.getArtistTypeName().isEmpty());
        CheckBox checkRandomCamera = view.findViewById(R.id.chk_random_camera);
        checkRandomCamera.setChecked(whatToRender.isRandomCamera());
        CheckBox checkRandomResolution = view.findViewById(R.id.chk_random_resolution);
        checkRandomResolution.setChecked(whatToRender.isRandomResolution());

        selectSpinner(view.findViewById(R.id.spin_layout), whatToRender.getPreset());
        selectSpinner(view.findViewById(R.id.spin_artist_type), whatToRender.getArtistTypeName());
        selectSpinner(view.findViewById(R.id.spin_num_artists), String.valueOf(whatToRender.getNumOfArtists()));
        selectSpinner(view.findViewById(R.id.spin_camera), String.valueOf(whatToRender.getCamera()));
        selectSpinner(view.findViewById(R.id.spin_resolution), String.valueOf(whatToRender.getResolution()));

        if (whatToRender.getRandomCount() < 5) {
            whatToRender.setRandomCount(50);
        }
        Slider sliderRandomWords = view.findViewById(R.id.slider_random_words);
        sliderRandomWords.setValue((float) whatToRender.getRandomCount());

        if (whatToRender.getPhraseCount() < 2) {
            whatToRender.setPhraseCount(2);
        }

        Slider sliderSentencesCount = view.findViewById(R.id.slider_sentences_count);
        sliderSentencesCount.setValue(whatToRender.getPhraseCount());

    }

    private void selectSpinner(Spinner spinner, String value) {
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int n = 0; n < adapter.getCount(); n++) {
            String str = (String) adapter.getItem(n);
            if (value.equalsIgnoreCase(str)) {
                spinner.setSelection(n);
                return;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = containerContext.getSharedPreferences(WhatToRender.class.getCanonicalName(), Context.MODE_PRIVATE);
        whatToRender.writeToSharedPreferences(preferences);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        initEngine(context);
    }

    private void initEngine(Context context) {
        Parser parser = new Parser();
        try {
            settingsCollection = parser.getSettings(context, GENERATOR_RULES);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addValuesToArtistTypeSpinner(@NonNull View view) {
        List<String> artistTypes = new ArrayList<>();
        settingsCollection.getSetting(KEY_ARTIST_TYPE).getAttributes().forEach(a -> artistTypes.add(a.getName()));
        artistTypes.add(0, view.getContext().getResources().getString(R.string.spinner_none));
        final Spinner spinner = (Spinner) view.findViewById(R.id.spin_artist_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, artistTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = spinner.getSelectedItem().toString();
                whatToRender.setArtistTypeName(str.startsWith("(") ? "" : str);
                showArtistsMatching(whatToRender.getArtistTypeName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    private void showArtistsMatching(String artistTypeName) {
        TextView artistsView = view.findViewById(R.id.lbl_resulting_artists);
        Setting setting = settingsCollection.getSetting("artists");
        List<AttributeValue> list = new ArrayList<>();
        if (setting != null) {
            list = settingsCollection.getAttributesMatchingExtraData(KEY_ARTIST_TYPE, artistTypeName, setting);
        }
        if (!list.isEmpty()) {
            String artistsStr = list.stream().map(AttributeValue::getValue).collect(Collectors.joining(", "));
            artistsView.setText(artistsStr);
            artistsView.setVisibility(View.VISIBLE);
        } else {
            artistsView.setVisibility(View.GONE);
        }
    }

    private void addValuesToLayoutSpinner(@NonNull View view) {
        List<String> presets = new ArrayList<>();
        settingsCollection.getPresets().forEach(p -> presets.add(p.getName()));
        presets.add(0, view.getContext().getResources().getString(R.string.spinner_none));
        Spinner layoutSpinner = view.findViewById(R.id.spin_layout);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutSpinner.setAdapter(adapter);
        layoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = layoutSpinner.getSelectedItem().toString();
                whatToRender.setPreset(str.startsWith("(") ? "" : str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void addValuesToNumOfArtistsSpinner(@NonNull View view) {
        final String[] selection = new String[]{"1", "2", "3"};
        Spinner spinner = view.findViewById(R.id.spin_num_artists);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, selection);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = spinner.getSelectedItem().toString();
                whatToRender.setNumOfArtists(Integer.parseInt(str));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void addValuesToCameraSpinner(@NonNull View view) {
        Spinner cameraSpinner = view.findViewById(R.id.spin_camera);
        List<String> cameras = new ArrayList<>();
        cameras.add(view.getContext().getResources().getString(R.string.spinner_none));
        Setting setting = settingsCollection.getSetting("camera");
        if (setting == null) {
            cameraSpinner.setEnabled(false);
        } else {
            List<AttributeValue> attributeValues = setting.getAttributes().stream().flatMap(s -> s.getValues().stream()).collect(Collectors.toList());
            cameras.addAll(attributeValues.stream().map(AttributeValue::getValue).collect(Collectors.toList()));
        }
        ArrayAdapter<String> cameraAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, cameras);
        cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cameraSpinner.setAdapter(cameraAdapter);
        cameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = cameraSpinner.getSelectedItem().toString();
                whatToRender.setCamera(str.startsWith("(") ? "" : str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void addValuesToResolutionSpinner(@NonNull View view) {
        Spinner resolutionSpinner = view.findViewById(R.id.spin_resolution);
        List<String> resolutions = new ArrayList<>();
        resolutions.add(view.getContext().getResources().getString(R.string.spinner_none));
        Setting setting = settingsCollection.getSetting("resolution");
        if (setting == null) {
            resolutionSpinner.setEnabled(false);
        } else {
            List<AttributeValue> attributeValues = setting.getAttributes().stream().flatMap(s -> s.getValues().stream()).collect(Collectors.toList());
            resolutions.addAll(attributeValues.stream().map(AttributeValue::getValue).collect(Collectors.toList()));
        }
        ArrayAdapter<String> resolutionAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, resolutions);
        resolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resolutionSpinner.setAdapter(resolutionAdapter);
        resolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = resolutionSpinner.getSelectedItem().toString();
                whatToRender.setResolution(str.startsWith("(") ? "" : str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

    }

    private void addDescriptionTextListener(View view) {
        EditText editText = view.findViewById(R.id.editTextTextMultiLine);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                whatToRender.setDescription(str);
                whatToRender.setTranslateToEnglishDescription(str);
                Button btn = view.findViewById(R.id.btn_next);
                btn.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(whatToRender.getClass().getCanonicalName(), whatToRender);
        super.onSaveInstanceState(outState);
    }

    private void addCheckBoxListeners(@NonNull View view) {
        CheckBox checkBoxNoLayout = view.findViewById(R.id.chk_no_layout);
        Spinner layoutSpinner = view.findViewById(R.id.spin_layout);
        checkBoxNoLayout.setOnCheckedChangeListener((v, checked) -> whatToRender.setPreset(checked ? "" : (String) layoutSpinner.getSelectedItem()));
        CheckBox checkBoxNoArtist = view.findViewById(R.id.chk_no_artists);
        Spinner numOfArtist = view.findViewById(R.id.spin_num_artists);
        checkBoxNoArtist.setOnCheckedChangeListener((v, checked) -> {
            whatToRender.setNumOfArtists(checked ? 0 : 1);
            numOfArtist.setEnabled(!checked);
        });
        Spinner cameraSpinner = view.findViewById(R.id.spin_camera);
        CheckBox checkRandomCamera = view.findViewById(R.id.chk_random_camera);
        checkRandomCamera.setOnCheckedChangeListener((v, checked) -> {
            whatToRender.setRandomCamera(checked);
            cameraSpinner.setEnabled(!checked);
        });
        Spinner artistTypeSpinner = view.findViewById(R.id.spin_artist_type);
        CheckBox checkRandomArtistType = view.findViewById(R.id.chk_random_artist);
        checkRandomArtistType.setOnCheckedChangeListener((v, checked) -> {
            whatToRender.setArtistTypeName("");
            artistTypeSpinner.setEnabled(!checked);
        });
        Spinner resolutionSpinner = view.findViewById(R.id.spin_resolution);
        CheckBox checkRandomResolution = view.findViewById(R.id.chk_random_resolution);
        checkRandomResolution.setOnCheckedChangeListener((v, checked) -> {
            whatToRender.setRandomResolution(checked);
            resolutionSpinner.setEnabled(!checked);
        });
    }

    private void initRandomWordsSlider(View view) {
        Slider slider = view.findViewById(R.id.slider_random_words);
        slider.addOnChangeListener((sl, v, fromUser) -> whatToRender.setRandomCount((int) v));
    }

    private void initSentencesCountSlider(View view) {
        Slider slider = view.findViewById(R.id.slider_sentences_count);
        slider.addOnChangeListener((sl, v, fromUser) -> whatToRender.setPhraseCount((int) v));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void checkTranslation() {
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        SimpleTranslationUtil instance = SimpleTranslationUtil.getInstance();
        instance.getTranslator().downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    CheckBox checkBoxTranslate = view.findViewById(R.id.check_translate);
                    instance.notifyTranslationAvailable(checkBoxTranslate,
                            SimpleTranslationUtil.getInstance().getTranslator());
                })
                .addOnFailureListener(e -> Toast.makeText(containerContext,
                        getText(R.string.no_translation_ger_to_eng),
                        Toast.LENGTH_LONG).show());
    }

    @Override
    public void onTranslationAvailable() {
        Log.d(getClass().getName(), "Not my business");
    }

    @Override
    public void notifyTranslationAvailable(View view, Translator translator) {
        view.setEnabled(true);
    }
}