package org.ww.ai.ui.inclues;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;

import org.ww.ai.R;
import org.ww.ai.rds.dao.EngineUsedNonDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowRenderModelsLinearLayout extends LinearLayout implements RenderModelsUI {

    private List<EngineUsedNonDao> engineList = new ArrayList<>();
    private FlexboxLayout rootLayout;

    public ShowRenderModelsLinearLayout(Context context) {
        super(context);
    }

    public ShowRenderModelsLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowRenderModelsLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, View view) {
        // TODO: this should work somehow different, this impl is too unstable
        if(LinearLayout.class.isAssignableFrom(view.getClass())) {
            LinearLayout linearLayout = (LinearLayout) view;
            LinearLayout child = (LinearLayout) linearLayout.getChildAt(linearLayout.getChildCount() - 1);
            LinearLayout other = (LinearLayout) child.getChildAt(child.getChildCount() - 1);
            rootLayout = (FlexboxLayout) other.getChildAt(0);
        }

    }

    @Override
    public void setEngineList(@Nullable List<EngineUsedNonDao> list) {
        engineList = new ArrayList<>(list != null ? list : Collections.emptyList());
        engineList.forEach(el -> addEngineUsedToLayout(el, engineList.size()));
    }

    @Override
    public List<EngineUsedNonDao> getEngineList() {
        return engineList;
    }

    @SuppressLint("SetTextI18n")
    private void addEngineUsedToLayout(EngineUsedNonDao entry, int idx) {
        LinearLayout dynamicEntry = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.render_model_text_view, rootLayout, false);
        TextView textView = dynamicEntry.findViewById(R.id.render_model);
        textView.setText(entry.renderModel.getName() + " (" + entry.credits + ")");
        rootLayout.addView(dynamicEntry);
    }

    private void showIllegalUse() {
        throw new IllegalArgumentException("wrong layout used LinearLayout[1].LinearLayout[1][0] must contain FlexboxLayout");
    }
}
