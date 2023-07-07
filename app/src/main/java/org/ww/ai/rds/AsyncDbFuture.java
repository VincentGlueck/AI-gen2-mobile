package org.ww.ai.rds;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


public class AsyncDbFuture<T> {

    public void processFuture(ListenableFuture<T> listenableFuture,
                              OnSuccessCallback<T> callback, Context context) {
            Futures.addCallback(
                    listenableFuture,
                    new FutureCallback<T>() {
                        public void onSuccess(T result) {
                            callback.processResult(result);
                        }

                        public void onFailure(@NonNull Throwable thrown) {
                            Log.d("FAILURE", thrown.getMessage());
                            Toast.makeText(context, thrown.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    context.getMainExecutor()
        );
    }

    public interface OnSuccessCallback<T> {
        void processResult(T result);
    }
}
