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

import androidx.annotation.NonNull;
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

    private final Map<Integer, View> mChildren = new HashMap<>();
    private List<EngineUsedNonDao> mEngineList = new ArrayList<>();
    private FlexboxLayout mRootLayout;


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
    public void init(Context context, View view) {
        mRootLayout = view.findViewById(R.id.render_model_include);
        Spinner spinner = view.findViewById(R.id.spinner_render_model);
        ArrayAdapter<String> renderedByAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, RenderModel.getAvailableModels());
        renderedByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(renderedByAdapter);
        spinner.setSelection(RenderModel.SDXL_1_0.ordinal());
        ImageView btnPlus = view.findViewById(R.id.imageview_btn_plus);
        btnPlus.setOnClickListener(click -> {
            EngineUsedNonDao entry = getSelectedEngine(view);
            mEngineList.add(entry);
            addEngineUsedToLayout(entry);
        });
        mRootLayout.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
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
                .inflate(R.layout.render_model_text_view, mRootLayout, false);
        ImageView clearImageView = dynamicEntry.findViewById(R.id.btn_delete);
        clearImageView.setOnClickListener(click -> removeRenderModel(mRootLayout, entry));
        TextView textView = dynamicEntry.findViewById(R.id.render_model);
        textView.setTextSize(8.0f);
        textView.setText(entry.renderModel.getName() + " (" + entry.credits + ")");
        mRootLayout.addView(dynamicEntry);
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
        EditText renderCostsEditText = view.findViewById(R.id.render_costs);
        entry.credits = Integer.parseInt(renderCostsEditText.getText().toString());
        Spinner spinner = view.findViewById(R.id.spinner_render_model);
        String selectedItem = (String) spinner.getSelectedItem();
        entry.renderModel = RenderModel.fromName(selectedItem);
        return entry;
    }
}
