package org.ww.ai.font;

import static org.ww.ai.font.FontCache.*;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.ww.ai.R;

public class FontTextView extends androidx.appcompat.widget.AppCompatTextView {

    public FontTextView(@NonNull Context context) {
        super(context);
    }

    public FontTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public FontTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.FontTextView,0, 0);
        boolean isRegular = attributes.getBoolean(R.styleable.FontTextView_regular_icon, false);
        if(isRegular) {
            setTypeface(FONT_CACHE.getTypeface(getContext(), FA_FONT_REGULAR));
        } else {
            setTypeface(FONT_CACHE.getTypeface(getContext(), FA_FONT_SOLID));
        }
    }
}
