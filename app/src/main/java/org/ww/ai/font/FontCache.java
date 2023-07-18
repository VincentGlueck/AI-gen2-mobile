package org.ww.ai.font;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

public enum FontCache {
    FONT_CACHE;

    public static final String FA_FONT_REGULAR = "font_awesome_regular.otf";
    public static final String FA_FONT_SOLID = "font_awesome_solid.otf";

    private final Map<String, Typeface> cache = new HashMap<>();

    public Typeface getTypeface(Context context, String name) {
        Typeface typeface = cache.getOrDefault(name, null);
        if(typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), name);
            if(typeface == null) {
                throw new IllegalArgumentException("Typeface '" + name + "' is invalid.");
            }
            cache.put(name, typeface);
        }
        return typeface;
    }
}
