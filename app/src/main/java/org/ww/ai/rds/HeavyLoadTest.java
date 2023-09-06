package org.ww.ai.rds;

import static org.ww.ai.tools.RandomSentences.RANDOM_SENTENCES;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.entity.RenderResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeavyLoadTest {

    private static final int HOW_MANY_TO_CREATE = 50;
    private final Context mContext;
    private final List<RenderResult> mExistingRenderResults = new ArrayList<>();

    private final List<RenderResult> mFakeRenderResults = new ArrayList<>();

    public HeavyLoadTest(Context context) {
        mContext = context;
    }

    public void createSomeImages() {
        AppDatabase appDatabase = AppDatabase.getInstance(mContext);
        ListenableFuture<List<RenderResult>> future = appDatabase.renderResultDao().getAll();
        AsyncDbFuture<List<RenderResult>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            mExistingRenderResults.addAll(result);
            processImages();
            writeToDatabase();
        }, mContext);


    }

    private void writeToDatabase() {
        AppDatabase appDatabase = AppDatabase.getInstance(mContext);
        ListenableFuture<List<Long>> listenableFuture = appDatabase.renderResultDao().insertRenderResults(mFakeRenderResults);
        AsyncDbFuture<List<Long>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, result -> {
            Toast.makeText(mContext, "Created " + HOW_MANY_TO_CREATE + " records", Toast.LENGTH_LONG).show();
        }, mContext);
    }

    private void processImages() {
        Random random = new Random();
        for (int n = 0; n < HOW_MANY_TO_CREATE; n++) {
            RenderResult renderResult = mExistingRenderResults.get(random.nextInt(mExistingRenderResults.size()));
            Log.d("USE", "result: " + renderResult);
            mFakeRenderResults.add(cloneAsNewRenderResult(renderResult));
        }
    }

    private RenderResult cloneAsNewRenderResult(RenderResult renderResult) {
        RenderResult result = new RenderResult();
        result.thumbNail = renderResult.thumbNail;
        result.image = renderResult.image;
        result.deleted = false;
        result.enginesUsed = renderResult.enginesUsed;
        result.width = renderResult.width;
        result.height = renderResult.height;
        result.queryUsed = renderResult.queryUsed;
        result.queryString = RANDOM_SENTENCES.getRandomSentence();
        result.createdTime = System.currentTimeMillis();
        return result;
    }
}
