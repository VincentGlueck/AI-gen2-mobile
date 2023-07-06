package org.ww.ai.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.ResultsGalleryFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.util.List;

public class ResultsGalleryFragment extends Fragment {

    private ResultsGalleryFragmentBinding binding;

    private ScrollView scrollView;

    private Context containerContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        assert container != null;
        this.containerContext = container.getContext();

        binding = ResultsGalleryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        scrollView = view.findViewById(R.id.results_gallery_fragment);
        getRenderResultsFromDatabase();
    }

    private void getRenderResultsFromDatabase() {
        AppDatabase appDatabase = AppDatabase.getInstance(containerContext);
        ListenableFuture<List<RenderResultLightWeight>> listenableFuture = appDatabase.renderResultDao().getAllLightWeights();
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, this::createGallery, containerContext);
    }

    private void createGallery(List<RenderResultLightWeight> renderResults) {
        renderResults.forEach(r -> {
            TextView textView = new TextView(containerContext);
            textView.setText(r.queryString);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.height = 48;
            params.width = 192;
            textView.setLayoutParams(params);
            scrollView.addView(textView);
        });
    }
}
