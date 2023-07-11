package org.ww.ai.activity;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.GalleryFullSizeFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.tools.ShareImageUtil;

public class GalleryFullSizeFragment extends Fragment {

    private int uid;

    private Context containerContext;

    private GalleryFullSizeFragmentBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getInt(RenderDetailsFragment.ARG_UID);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        assert container != null;
        this.containerContext = container.getContext();
        binding = GalleryFullSizeFragmentBinding.inflate(inflater, container, false);
        if (savedInstanceState != null) {
            uid = (int) savedInstanceState.get(RenderDetailsFragment.ARG_UID);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SubsamplingScaleImageView imageView = view.findViewById(R.id.gallery_full_size_image);
        loadImageFromDatabase(view, imageView, uid);

    }

    private void loadImageFromDatabase(View view, SubsamplingScaleImageView imageView, int uid) {
        AppDatabase db = AppDatabase.getInstance(containerContext);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                assert getActivity() != null;
                imageView.setImage(ImageSource.bitmap(IMAGE_UTIL.convertBlobToImage(result.image)));
                ImageView imageViewShare = view.findViewById(R.id.gallery_full_size_share);
                imageViewShare.setVisibility(View.VISIBLE);
                imageViewShare.setOnClickListener(v -> new ShareImageUtil(getActivity()).startShare(result.uid));
            }
        }, containerContext);
    }



    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
