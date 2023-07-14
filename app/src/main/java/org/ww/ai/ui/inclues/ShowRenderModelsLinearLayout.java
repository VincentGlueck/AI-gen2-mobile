package org.ww.ai.ui.inclues;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    public void init(Context context, @NonNull View view) {
        if(!ViewGroup.class.isAssignableFrom(view.getClass())) {
            showIllegalUse();
        }
        rootLayout = findFlexBoxLayout((ViewGroup) view);
        if (rootLayout == null) {
            showIllegalUse();
        }
    }

    private FlexboxLayout findFlexBoxLayout(ViewGroup root) {
        FlexboxLayout result = null;
        for (int n = 0; n < ((ViewGroup) root).getChildCount(); n++) {
            View child = ((ViewGroup) root).getChildAt(n);
            if (FlexboxLayout.class.isAssignableFrom(child.getClass())) {
                result = (FlexboxLayout) child;
                break;
            } else if (ViewGroup.class.isAssignableFrom(child.getClass())) {
                result = findFlexBoxLayout((ViewGroup) child);
                if (result != null) {
                    return result;
                }
            }
        }
        return result;
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
        ImageView imageView = dynamicEntry.findViewById(R.id.btn_delete);
        imageView.setVisibility(GONE);
        rootLayout.addView(dynamicEntry);
    }

    private void showIllegalUse() {
        throw new IllegalArgumentException("root layout must a) be a ViewGroup, " +
                "b) contain FlexboxLayout somewhere in it's children!");
    }
}
