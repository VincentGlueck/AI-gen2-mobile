package org.ww.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.ww.ai.data.SettingsCollection;
import org.ww.ai.data.WhatToRenderIF;
import org.ww.ai.databinding.FragmentFirstBinding;
import org.ww.ai.parcel.WhatToRender;
import org.ww.ai.parser.Parser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class FirstFragment extends Fragment {
    public static final String GENERATOR_RULES = "generator.xml";
    private static final int DEFAULT_ARTIST_COUNT = 3;
    private FragmentFirstBinding binding;

    private SettingsCollection settingsCollection;

    private WhatToRenderIF whatToRender;
    private Context containerContext;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        assert container != null;
        this.containerContext = container.getContext();

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        binding.btnNext.setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));

        if(whatToRender == null) {
            whatToRender = new WhatToRender();
        }

        addDescriptionTextListener(view);
        addValuesToArtistTypeSpinner(view);
        addValuesToLayoutSpinner(view);
        addCheckBoxListeners(view);
        addValuesToNumOfArtistsSpinner(view);

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
        CheckBox checkBoxRandomArtist =  view.findViewById(R.id.chk_random_artist);
        checkBoxRandomArtist.setChecked(whatToRender.getArtistTypeName().isEmpty());

        selectSpinner(view.findViewById(R.id.spin_layout), whatToRender.getPreset());
        selectSpinner(view.findViewById(R.id.spin_artist_type), whatToRender.getArtistTypeName());
        selectSpinner(view.findViewById(R.id.spin_num_artists), String.valueOf(whatToRender.getNumOfArtists()));

    }

    private void selectSpinner(Spinner spinner, String value) {
        SpinnerAdapter adapter = spinner.getAdapter();
        for(int n=0; n<adapter.getCount(); n++) {
            String str = (String) adapter.getItem(n);
            if(value.equalsIgnoreCase(str)) {
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
        settingsCollection.getSetting("artisttype").getAttributes().forEach(a -> artistTypes.add(a.getName()));
        artistTypes.add(0, view.getContext().getResources().getString(R.string.spinner_none));
        final Spinner spinner = (Spinner) view.findViewById(R.id.spin_artist_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_item, artistTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = spinner.getSelectedItem().toString();
                whatToRender.setArtistTypeName(str.startsWith("(") ? "" : str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    private void addValuesToLayoutSpinner(@NonNull View view) {
        List<String> presets = new ArrayList<>();
        settingsCollection.getPresets().forEach(p -> presets.add(p.getName()));
        presets.add(0, view.getContext().getResources().getString(R.string.spinner_none));
        Spinner spinner = view.findViewById(R.id.spin_layout);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_item, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = spinner.getSelectedItem().toString();
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_item, selection);
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


    private void addDescriptionTextListener(View view) {
        EditText editText = view.findViewById(R.id.editTextTextMultiLine);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                whatToRender.setDescription(s.toString());
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
        Spinner layoutSpinner = (Spinner) view.findViewById(R.id.spin_layout);
        addDisableCheckBoxListener(checkBoxNoLayout, layoutSpinner);
        CheckBox checkBoxNoArtist = view.findViewById(R.id.chk_no_artists);
        CheckBox checkBoxRandomArtist =  view.findViewById(R.id.chk_random_artist);
        Spinner artistSpinner = (Spinner) view.findViewById(R.id.spin_artist_type);
        addDisableCheckBoxListener(checkBoxRandomArtist, artistSpinner);
        checkBoxNoArtist.setOnCheckedChangeListener((v, checked) -> {
            whatToRender.setNumOfArtists(checked ? 0 : DEFAULT_ARTIST_COUNT);
        });
    }

    private void addDisableCheckBoxListener(CheckBox checkBoxLayout, Spinner layoutSpinner) {
        checkBoxLayout.setOnCheckedChangeListener((v, checked) -> {
            if(checked) {
                layoutSpinner.setSelection(0);
                layoutSpinner.setEnabled(false);
            } else {
                layoutSpinner.setEnabled(true);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}