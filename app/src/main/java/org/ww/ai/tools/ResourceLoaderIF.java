package org.ww.ai.tools;

import android.content.Context;

import java.io.InputStream;

public interface ResourceLoaderIF {

    void initDeviceSpecific(Object... fromRoot) throws ResourceException;

    InputStream getResourceInputStream(Context context, String name) throws ResourceException;

}
