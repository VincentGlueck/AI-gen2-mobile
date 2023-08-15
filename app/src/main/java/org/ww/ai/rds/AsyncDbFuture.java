package org.ww.ai.rds;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;


public class AsyncDbFuture<T> {

    public void processFuture(ListenableFuture<T> listenableFuture,
                              OnSuccessCallback<T> callback, Context context) {
        Futures.addCallback(
                listenableFuture,
                new FutureCallback<>() {
                    public void onSuccess(T result) {
                        try {
                            callback.processResult(result);
                        } catch (IOException e) {
                            Log.e("ABOUT", "failed to create backup: " + e.getMessage());
                        }
                    }

                    public void onFailure(@NonNull Throwable thrown) {
                        throw new RuntimeException(thrown);
                    }
                },
                context.getMainExecutor()
        );
    }

    public interface OnSuccessCallback<T> {
        void processResult(T result) throws IOException;
    }
}
