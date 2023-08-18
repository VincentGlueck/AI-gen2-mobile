package org.ww.ai.tools;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum ExecutorUtil {
    EXECUTOR_UTIL;

    public void execute(final ExecutionIF execution) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            execution.runInBackground();
            handler.post(execution::onExecutionFinished);
        });
    }


    public void execute(final SimpleExecutionIF simpleExecution) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(simpleExecution::runInBackground);
    }

    public interface SimpleExecutionIF {
        void runInBackground();
    }

    public interface ExecutionIF {
        void runInBackground();

        void onExecutionFinished();
    }

}
