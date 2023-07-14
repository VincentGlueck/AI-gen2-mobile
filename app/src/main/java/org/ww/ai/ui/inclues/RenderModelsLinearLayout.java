package org.ww.ai.ui.inclues;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;

import org.ww.ai.R;
import org.ww.ai.enumif.EventTypes;
import org.ww.ai.event.EventBroker;
import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.rds.ifenum.RenderModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderModelsLinearLayout extends LinearLayout implements RenderModelsUI {

    private final Map<Integer, View> children = new HashMap<>();

    public RenderModelsLinearLayout(Context context) {
        super(context);
    }

    public RenderModelsLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(attrs == null) {
            return;
        }
    }

    public RenderModelsLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private List<EngineUsedNonDao> engineList = new ArrayList<>();
    private FlexboxLayout rootLayout;


    @Override
    public void init(Context context, View view) {
        rootLayout = view.findViewById(R.id.render_model_include);
        Spinner spinner = view.findViewById(R.id.spinner_render_model);
        ArrayAdapter<String> renderedByAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, RenderModel.getAvailableModels());
        renderedByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(renderedByAdapter);
        spinner.setSelection(RenderModel.SDXL_BETA.ordinal());
        ImageView btnPlus = view.findViewById(R.id.imageview_btn_plus);
        btnPlus.setOnClickListener(click -> {
            EngineUsedNonDao entry = getSelectedEngine(view);
            engineList.add(entry);
            addEngineUsedToLayout(entry);
        });
        rootLayout.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                EventBroker.EVENT_BROKER.notifyReceivers(EventTypes.MODEL_ADDED);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                EventBroker.EVENT_BROKER.notifyReceivers(EventTypes.MODEL_REMOVED);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void addEngineUsedToLayout(EngineUsedNonDao entry) {
        LinearLayout dynamicEntry = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.render_model_text_view, rootLayout, false);
        ImageView clearImageView = dynamicEntry.findViewById(R.id.btn_delete);
        clearImageView.setOnClickListener(click -> {
            removeRenderModel(rootLayout, entry);
        });
        TextView textView = dynamicEntry.findViewById(R.id.render_model);
        textView.setText(entry.renderModel.getName() + " (" + entry.credits + ")");
        rootLayout.addView(dynamicEntry);
        children.put(entry.hashCode(), dynamicEntry);
    }

    private void removeRenderModel(ViewGroup viewGroup, EngineUsedNonDao entry) {
        engineList.remove(entry);
        View view = children.getOrDefault(entry.hashCode(), null);
        if(view != null) {
            viewGroup.removeView(view);
        }
    }

    @Override
    public List<EngineUsedNonDao> getEngineList() {
        return engineList;
    }

    public void setEngineList(@Nullable final List<EngineUsedNonDao> list) {
        engineList = new ArrayList<>(list != null ? list : Collections.emptyList());
        engineList.forEach(this::addEngineUsedToLayout);
    }

    private EngineUsedNonDao getSelectedEngine(View view) {
        EngineUsedNonDao entry = new EngineUsedNonDao();
        EditText renderCostsEditText = view.findViewById(R.id.render_costs);
        entry.credits = Integer.parseInt(renderCostsEditText.getText().toString());
        Spinner spinner = view.findViewById(R.id.spinner_render_model);
        String selectedItem = (String) spinner.getSelectedItem();
        entry.renderModel = RenderModel.fromName(selectedItem);
        return entry;
    }
}
