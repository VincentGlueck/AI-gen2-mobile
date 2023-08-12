package org.ww.ai.ui.inclues;

import static org.ww.ai.event.EventBroker.EVENT_BROKER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;

import org.ww.ai.R;
import org.ww.ai.enumif.EventTypes;
import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.rds.ifenum.RenderModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderModelsLinearLayout extends LinearLayout implements RenderModelsUI {

    private final Map<Integer, View> mChildren = new HashMap<>();
    private List<EngineUsedNonDao> mEngineList = new ArrayList<>();
    private FlexboxLayout rootLayout;
    private final AtomicBoolean mFirstItemSelected = new AtomicBoolean(true);


    public RenderModelsLinearLayout(Context context) {
        super(context);
    }

    public RenderModelsLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RenderModelsLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, @NonNull View view, @NonNull List<EngineUsedNonDao> engineList) {
        mEngineList = engineList;
        init(context, view);
    }

    @Override
    public void init(final Context context, final View view) {
        mFirstItemSelected.set(true);
        rootLayout = view.findViewById(R.id.render_model_include);
        Spinner spinner = view.findViewById(R.id.spinner_render_model);
        ArrayAdapter<String> renderedByAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, RenderModel.getAvailableModels());
        renderedByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(renderedByAdapter);
        spinner.setSelection(RenderModel.SDXL_1_0.ordinal());
        List<String> mCostList = new ArrayList<>(Arrays.asList(context.getResources()
                .getStringArray(R.array.render_costs)));
        mCostList.add(0, "");
        ArrayAdapter<String> costsAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, mCostList);
        Spinner costsSpinner = view.findViewById(R.id.spinner_render_costs);
        costsSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                        if (position >= 0 && !mFirstItemSelected.get()) {
                            addRenderEngineEntry(view);
                        }
                        mFirstItemSelected.set(false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        costsSpinner.setAdapter(costsAdapter);
        rootLayout.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                EVENT_BROKER.notifyReceivers(EventTypes.MODEL_ADDED);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                EVENT_BROKER.notifyReceivers(EventTypes.MODEL_REMOVED);
            }
        });
    }

    private void addRenderEngineEntry(View view) {
        EngineUsedNonDao entry = getSelectedEngine(view);
        if (entry != null) {
            mEngineList.add(entry);
            addEngineUsedToLayout(entry);
        }
    }

    @SuppressLint("SetTextI18n")
    private void addEngineUsedToLayout(EngineUsedNonDao entry) {
        LinearLayout dynamicEntry = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.render_model_text_view, rootLayout, false);
        ImageView clearImageView = dynamicEntry.findViewById(R.id.btn_delete);
        clearImageView.setOnClickListener(click -> removeRenderModel(rootLayout, entry));
        TextView textView = dynamicEntry.findViewById(R.id.render_model);
        textView.setTextSize(12.0f);
        textView.setText(entry.renderModel.getName() + " (" + entry.credits + ")");
        rootLayout.addView(dynamicEntry);
        mChildren.put(entry.hashCode(), dynamicEntry);
    }

    private void removeRenderModel(ViewGroup viewGroup, EngineUsedNonDao entry) {
        mEngineList.remove(entry);
        View view = mChildren.getOrDefault(entry.hashCode(), null);
        if (view != null) {
            viewGroup.removeView(view);
        }
    }

    @Override
    public List<EngineUsedNonDao> getmEngineList() {
        return mEngineList;
    }

    public void setEngineList(@Nullable final List<EngineUsedNonDao> list) {
        mEngineList = new ArrayList<>(list != null ? list : Collections.emptyList());
        mEngineList.forEach(this::addEngineUsedToLayout);
    }

    private EngineUsedNonDao getSelectedEngine(View view) {
        EngineUsedNonDao entry = new EngineUsedNonDao();
        Spinner costsSpinner = view.findViewById(R.id.spinner_render_costs);
        if (costsSpinner.getSelectedItem() == null || costsSpinner.getSelectedItem().toString().isEmpty()) {
            return null;
        }
        entry.credits = Integer.parseInt(costsSpinner.getSelectedItem().toString());
        Spinner spinner = view.findViewById(R.id.spinner_render_model);
        String selectedItem = (String) spinner.getSelectedItem();
        entry.renderModel = RenderModel.fromName(selectedItem);
        costsSpinner.setSelection(0);
        return entry;
    }
}
