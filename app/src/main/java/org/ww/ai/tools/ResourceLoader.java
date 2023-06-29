package org.ww.ai.tools;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public enum ResourceLoader {
	RESOURCE_LOADER;

	public InputStream getResource(Context context, String resourceName) throws IOException {
		return context.getResources().openRawResource(
				context.getResources().getIdentifier("generator",
						"raw", context.getPackageName()));
	}
	
}
