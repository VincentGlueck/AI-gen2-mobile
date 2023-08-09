package org.ww.ai.fragment;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.ResultsGalleryFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.MetricsUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GalleryFragment extends Fragment {

    private static final int THUMBS_PER_ROW = 3;
    private ResultsGalleryFragmentBinding mBinding;
    private LinearLayout mLinearLayout;
    private Context mContainerContext;
    private ViewGroup mViewGroup;
    private MetricsUtil.Screen mScreen;
    private List<RenderResultLightWeight> mRenderResults;
    private boolean deleteMode = false;
    private MenuProvider mMenuProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.mViewGroup = container;
        assert container != null;
        this.mContainerContext = container.getContext();
        mBinding = ResultsGalleryFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mLinearLayout = view.findViewById(R.id.results_gallery_linear_layout);
        getRenderResultsFromDatabase(mViewGroup);
        if (getActivity() != null && getActivity().getWindowManager() != null) {
            mScreen = MetricsUtil.getScreen(getActivity().getWindowManager());
        }
    }

    private void getRenderResultsFromDatabase(final ViewGroup viewGroup) {
        AppDatabase appDatabase = AppDatabase.getInstance(mContainerContext);
        ListenableFuture<List<RenderResultLightWeight>> listenableFuture = appDatabase.renderResultDao().getAllLightWeights();
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture,
                r -> createGallery(viewGroup, r), mContainerContext);
    }

    private void createGallery(@NonNull ViewGroup parent,
                               @NonNull List<RenderResultLightWeight> renderResults) {
        mRenderResults = renderResults;
        LinearLayout rowLayout = null;
        int count = 0;
        for (RenderResultLightWeight lightWeight : renderResults) {
            if (rowLayout == null) {
                rowLayout = createRow(parent);
            }
            LinearLayout layoutHolder = createImageView(lightWeight.thumbNail, rowLayout);
            count++;

            ImageView imageView = layoutHolder.findViewById(R.id.single_gallery_image_view);
            final CheckBox checkBox = layoutHolder.findViewById(R.id.check_single_entry);
            imageView.setOnClickListener(v -> onImageClickListener(lightWeight.uid));
            imageView.setOnLongClickListener(l -> {
                lightWeight.flagChecked = !lightWeight.flagChecked;
                checkBox.setChecked(lightWeight.flagChecked);
                updateToolbar();
                return true;
            });

            checkBox.setOnCheckedChangeListener((v, isChecked) -> {
                lightWeight.flagChecked = isChecked;
                updateToolbar();
            });
            if (count >= THUMBS_PER_ROW) {
                mLinearLayout.addView(rowLayout);
                count = 0;
                rowLayout = null;
            }
        }
        if (rowLayout != null) {
            mLinearLayout.addView(rowLayout);
        }
        if (renderResults.isEmpty()) {
            View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_result,
                    mLinearLayout, false);
            mLinearLayout.addView(emptyView);
        }
    }

    private void updateToolbar() {
        if (mRenderResults != null) {
            boolean deleteChecked = mRenderResults.stream().anyMatch(r -> r.flagChecked);
            if (!deleteMode && deleteChecked) {
                addMenuToolbar();
            }
        } else if (deleteMode) {
            removeMenuToolbar();
        }
    }

    public void removeMenuToolbar() {
        requireActivity().removeMenuProvider(mMenuProvider);
        deleteMode = false;
    }

    private void addMenuToolbar() {
        MenuHost menuHost = requireActivity();
        mMenuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.gallerymenu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_delete) {
                    Log.d("DELETE", "***pressed***");
                }
                return false;
            }
        };
        menuHost.addMenuProvider(mMenuProvider);
        deleteMode = true;
    }

    private LinearLayout createImageView(byte[] thumbNail, LinearLayout rowLayout) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(getActivity()).inflate(R.layout.single_gallery_image, rowLayout, false);
        ImageView imageView = linearLayout.findViewById(R.id.single_gallery_image_view);
        Glide.with(mContainerContext)
                .asBitmap()
                .load(IMAGE_UTIL.convertBlobToImage(thumbNail))
                .apply(RequestOptions.centerCropTransform())
                .into(imageView);
        linearLayout.setPadding(0, 0, 4, 4);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        if (mScreen != null) {
            params.width = (mScreen.width - 4 * THUMBS_PER_ROW) / THUMBS_PER_ROW;
            if (IMAGE_UTIL.getImageBounds(imageView).height() < params.width) {
                params.height = (mScreen.width - 4 * THUMBS_PER_ROW) / THUMBS_PER_ROW;
            }
        } else {
            params.width = 192;
            params.height = 192;
        }
        rowLayout.addView(linearLayout, params);
        linearLayout.setClickable(true);
        linearLayout.setLongClickable(true);
        return linearLayout;
    }

    private void onImageClickListener(int uid) {
        NavController navController = NavHostFragment.findNavController(GalleryFragment.this);
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
        if (mMenuProvider != null) {
            requireActivity().removeMenuProvider(mMenuProvider);
        }
        mBinding = null;
    }
}
