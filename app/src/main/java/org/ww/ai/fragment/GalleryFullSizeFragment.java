package org.ww.ai.fragment;

import static org.ww.ai.event.EventBroker.EVENT_BROKER;
import static org.ww.ai.tools.ExecutorUtil.EXECUTOR_UTIL;
import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.GalleryFullSizeFragmentBinding;
import org.ww.ai.enumif.EventTypes;
import org.ww.ai.prefs.Preferences;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.tools.ShareImageUtil;

import java.util.List;

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
            Object obj = savedInstanceState.get(RenderDetailsFragment.ARG_UID);
            uid = obj == null ? -1 : Integer.parseInt(obj.toString());
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.gallery_full_size_image);
        loadImageFromDatabase(view, imageView, uid);
    }

    private void loadImageFromDatabase(View view, ImageView imageView, int uid) {
        AppDatabase db = AppDatabase.getInstance(containerContext);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                ImageView imageViewShare = view.findViewById(R.id.gallery_full_size_share);
                ImageView imageViewDelete = view.findViewById(R.id.gallery_full_size_delete);
                CheckBox checkIncludeText = view.findViewById(R.id.gallery_full_size_share_with_text);
                if(result.image == null) {
                    Glide.with(containerContext)
                            .load(R.drawable.no_image)
                            .override(400)
                            .into(imageView);
                    imageViewShare.setVisibility(View.GONE);
                    imageViewDelete.setVisibility(View.GONE);
                    checkIncludeText.setVisibility(View.GONE);
                } else {
                    Glide.with(containerContext)
                            .asBitmap()
                            .load(IMAGE_UTIL.convertBlobToImage(result.image))
                            .into(imageView);
                    imageViewShare.setVisibility(View.VISIBLE);
                    imageViewShare.setOnClickListener(v -> new ShareImageUtil(getActivity())
                            .startShare(result.uid, checkIncludeText.isChecked()));
                    imageViewDelete.setOnClickListener(l -> deleteImage(uid));
                }
            }
        }, containerContext);
    }

    private void softDeleteFuture(AppDatabase db, ListenableFuture<RenderResult> future) {
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            result.deleted = true;
            ListenableFuture<Integer> softDelFuture = db.renderResultDao().updateRenderResults(List.of(result));
            AsyncDbFuture<Integer> asyncDbFuture1 = new AsyncDbFuture<>();
            asyncDbFuture1.processFuture(softDelFuture, i -> {
            }, containerContext);
        }, containerContext);

    }

    private void hardDeleteFuture(AppDatabase db, ListenableFuture<RenderResult> future) {
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            ListenableFuture<Integer> delFuture = db.renderResultDao().deleteRenderResults(List.of(result));
            AsyncDbFuture<Integer> asyncDbFutureDel = new AsyncDbFuture<>();
            asyncDbFutureDel.processFuture(delFuture, i -> {
            }, containerContext);
        }, containerContext);
    }

    private void deleteImage(final int uid) {
        AppDatabase db = AppDatabase.getInstance(containerContext);
        final boolean useTrash = Preferences.getInstance(containerContext).getBoolean(Preferences.PREF_USE_TRASH);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        if (useTrash) {
            softDeleteFuture(db, future);
        } else {
            hardDeleteFuture(db, future);
        }
        getParentFragmentManager().popBackStackImmediate();
        EXECUTOR_UTIL.execute(() -> EVENT_BROKER.notifyReceivers(EventTypes.SINGLE_IMAGE_DELETED, uid));
    }


    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
