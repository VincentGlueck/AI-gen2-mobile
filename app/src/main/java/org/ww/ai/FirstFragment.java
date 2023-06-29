package org.ww.ai;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.ww.ai.data.Setting;
import org.ww.ai.data.SettingsCollection;
import org.ww.ai.data.SettingsCollectionIF;
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
    private static final String GENERATOR_RULES = "generator.xml";
    private FragmentFirstBinding binding;

    private SettingsCollection settingsCollection;

    private WhatToRenderIF whatToRender;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        whatToRender = new WhatToRender();

        addValuesToArtistTypeSpinner(view);
        addValuesToLayoutSpinner(view);
        addCheckBoxListeners(view);

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
                Toast.makeText(view.getContext(), "Setting artist(type) to " + whatToRender.getArtistTypeName(), Toast.LENGTH_LONG).show();
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
        Spinner spinner = (Spinner) view.findViewById(R.id.spin_layout);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_item, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String str = spinner.getSelectedItem().toString();
                whatToRender.setPreset(str.startsWith("(") ? "" : str);
                Toast.makeText(view.getContext(), "Setting preset to " + whatToRender.getPreset(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    private void addCheckBoxListeners(@NonNull View view) {
        CheckBox checkBoxLayout = (CheckBox) view.findViewById(R.id.chk_no_layout);
        Spinner layoutSpinner = (Spinner) view.findViewById(R.id.spin_layout);
        addDisableCheckBoxListener(checkBoxLayout, layoutSpinner);
        CheckBox checkBoxNoLayout = (CheckBox) view.findViewById(R.id.chk_no_artists);
        Spinner artistSpinner = (Spinner) view.findViewById(R.id.spin_artist_type);
        addDisableCheckBoxListener(checkBoxNoLayout, artistSpinner);
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