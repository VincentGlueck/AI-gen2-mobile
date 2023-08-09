package org.ww.ai.ui.inclues;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
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

    private List<EngineUsedNonDao> mEngineList = new ArrayList<>();
    private FlexboxLayout mRootLayout;

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
        if(!ViewGroup.class.isAssignableFrom(view.getClass())) {
            showIllegalUse();
        }
        mRootLayout = findFlexBoxLayout((ViewGroup) view);
        if (mRootLayout == null) {
            showIllegalUse();
        }
    }

    @Override
    public void init(Context context, @NonNull View view,
                     @NonNull List<EngineUsedNonDao> engineList) {
        init(context, view);
        setEngineList(engineList);
    }

    private FlexboxLayout findFlexBoxLayout(ViewGroup root) {
        if(FlexboxLayout.class.isAssignableFrom(root.getClass())) {
            return (FlexboxLayout) root;
        }
        FlexboxLayout result = null;
        for (int n = 0; n < root.getChildCount(); n++) {
            View child = root.getChildAt(n);
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
        mEngineList = new ArrayList<>(list != null ? list : Collections.emptyList());
        mEngineList.forEach(this::addEngineUsedToLayout);
    }

    @Override
    public List<EngineUsedNonDao> getmEngineList() {
        return mEngineList;
    }

    @SuppressLint("SetTextI18n")
    private void addEngineUsedToLayout(EngineUsedNonDao entry) {
        LinearLayout dynamicEntry = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.render_model_text_view, mRootLayout, false);
        TextView textView = dynamicEntry.findViewById(R.id.render_model);
        textView.setText(entry.renderModel.getName() + " (" + entry.credits + ")");
        ImageView imageView = dynamicEntry.findViewById(R.id.btn_delete);
        imageView.setVisibility(GONE);
        mRootLayout.addView(dynamicEntry);
    }

    private void showIllegalUse() {
        throw new IllegalArgumentException("root layout must a) be a ViewGroup, " +
                "b) contain FlexboxLayout somewhere in it's children!");
    }
}
