package org.ww.ai.fragment;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.ResultsGalleryFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.tools.ShareImageUtil;
import org.ww.ai.ui.MetricsUtil;

import java.util.List;

public class ResultsGalleryFragment extends Fragment {

    private static final int THUMBS_PER_ROW = 3;

    private ResultsGalleryFragmentBinding binding;

    private LinearLayout linearLayout;

    private Context containerContext;

    private ViewGroup container;
    private MetricsUtil.Screen screen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.container = container;
        assert container != null;
        this.containerContext = container.getContext();
        binding = ResultsGalleryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        linearLayout = view.findViewById(R.id.results_gallery_linear_layout);
        getRenderResultsFromDatabase(container);
        if(getActivity() != null && getActivity().getWindowManager() != null) {
            screen = MetricsUtil.getScreen(getActivity().getWindowManager());
        }
    }

    private void getRenderResultsFromDatabase(final ViewGroup viewGroup) {
        AppDatabase appDatabase = AppDatabase.getInstance(containerContext);
        ListenableFuture<List<RenderResultLightWeight>> listenableFuture = appDatabase.renderResultDao().getAllLightWeights();
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture,
                r -> createGallery(viewGroup, linearLayout, r), containerContext);
    }

    private void createGallery(@NonNull ViewGroup parent, @NonNull LinearLayout view,
                               @NonNull List<RenderResultLightWeight> renderResults) {
        LinearLayout rowLayout = null;
        int count = 0;
        for(RenderResultLightWeight lightWeight : renderResults) {
            if(rowLayout == null) {
                 rowLayout = createRow(parent);
            }
            ImageView imageView = createImageView(lightWeight.thumbNail, rowLayout);
            count++;
            addListeners(lightWeight, imageView);
            if(count >= THUMBS_PER_ROW) {
                view.addView(rowLayout);
                count = 0;
                rowLayout = null;
            }
        }
        if(rowLayout != null) {
            view.addView(rowLayout);
        }
        if(renderResults.isEmpty()) {
            View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_result,
                    view, false);
            view.addView(emptyView);
        }
    }

    private ShapeableImageView createImageView(byte[] thumbNail, LinearLayout rowLayout) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(4));
        ShapeableImageView imageView = (ShapeableImageView) LayoutInflater
                .from(getActivity()).inflate(R.layout.single_gallery_image, rowLayout, false);
        Glide.with(containerContext)
                .asBitmap()
                .load(IMAGE_UTIL.convertBlobToImage(thumbNail))
                .apply(requestOptions)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
        imageView.setPadding(4, 4, 4, 4);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        if(screen != null) {
            params.width = screen.width / THUMBS_PER_ROW;
            if(IMAGE_UTIL.getImageBounds(imageView).height() < params.width) {
                params.height = screen.width / THUMBS_PER_ROW;
            }
        } else {
            params.width = 192;
            params.height = 192;
        }
        rowLayout.addView(imageView, params);
        return imageView;
    }

    private void addListeners(final RenderResultLightWeight lightWeight, final ImageView imageView) {
        imageView.setOnClickListener(v -> onImageClickListener(lightWeight.uid));
        imageView.setOnLongClickListener(l -> {
            new ShareImageUtil(getActivity()).startShare(lightWeight.uid);
            return true;
        });
    }

    private void onImageClickListener(int uid) {
        NavController navController = NavHostFragment.findNavController(ResultsGalleryFragment.this);
        Bundle bundle = new Bundle();
        bundle.putInt(RenderDetailsFragment.ARG_UID, uid);
        navController.navigate(R.id.action_ResultsGalleryFragment_to_GalleryFullSizeFragment, bundle);
    }

    private LinearLayout createRow(ViewGroup parent) {
        return (LinearLayout) LayoutInflater.from(getActivity())
                .inflate(R.layout.single_gallery_row, parent, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
