package org.ww.ai.fragment;

import static org.ww.ai.rds.PagingCache.PAGING_CACHE;
import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.rds.ifenum.ThumbnailCallbackIF;
import org.ww.ai.ui.MetricsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ThumbnailCallbackImpl implements ThumbnailCallbackIF {

    private final ViewGroup mParent;
    private final MetricsUtil.Screen mScreen;
    private final Context mContext;
    private final LinearLayout mRootLayout;
    private final Set<String> mSelectedSet;
    private final AtomicBoolean mUseDummies = new AtomicBoolean(false);
    private final ThumbnailSelectionCallbackIF mThumbnailSelectionCallback;
    private final List<RowHolder> mRowHolderList;
    private int count = 0;
    private LinearLayout mRowLayout;
    private int mRowHeight;

    public ThumbnailCallbackImpl(@NonNull Context context, @NonNull ViewGroup parent,
                                 @NonNull LinearLayout rootLayout, @NonNull Set<String> selectedSet,
                                 @NonNull ThumbnailSelectionCallbackIF thumbnailSelectionCallbackIF,
                                 @NonNull MetricsUtil.Screen screen) {
        mContext = context;
        mParent = parent;
        mRootLayout = rootLayout;
        mSelectedSet = selectedSet;
        mThumbnailSelectionCallback = thumbnailSelectionCallbackIF;
        mScreen = screen;
        mRowHolderList = new ArrayList<>();
    }

    @Override
    public synchronized void setThumbnail(@NonNull RenderResultLightWeight lightWeight) {
        if (mRowLayout == null) {
            mRowLayout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.single_gallery_row, mParent, false);
            mRowHolderList.add(new RowHolder());
        }
        LinearLayout singleImageLayout = createImageView(lightWeight.thumbNail != null
                ? lightWeight.thumbNail : null);
        count++;
        // TODO: initSingleImageView

        ImageViewHolder imageViewHolder = new ImageViewHolder(lightWeight.uid,
                singleImageLayout);
        mRowHolderList.get(mRowHolderList.size() - 1).imageViewHolders.add(imageViewHolder);
        mRowHolderList.get(mRowHolderList.size() - 1).avail = !mUseDummies.get();
        if (count >= THUMBS_PER_ROW && mRowLayout != null) {
            mRootLayout.addView(mRowLayout);
            count = 0;
            mRowLayout = null;
        }
        mThumbnailSelectionCallback.initSingleImageView(lightWeight, singleImageLayout);
        mThumbnailSelectionCallback.finishedRender(lightWeight, singleImageLayout);
    }

    @Override
    public void processCleanup() {
        if (mRowLayout != null) {
            mRootLayout.addView(mRowLayout);
            mRowLayout = null;
        }
    }

    @Override
    public boolean isUseDummyImages() {
        return mUseDummies.get();
    }

    @Override
    public void setUseDummyImages(boolean flagUseDummies) {
        mUseDummies.set(flagUseDummies);
    }

    @Override
    public void onScrollPositionChanged(int scrollY, int oldScrollY) {
        int yOffset = 0;
        for (int n = 0; n < mRowHolderList.size(); n++) {
            if ((yOffset + mRowHeight) > scrollY && yOffset < (mScreen.height + scrollY)) {
                loadMissingThumbs(mRowHolderList.get(n));
            }
            yOffset += mRowHeight;
        }
    }

    private void loadMissingThumbs(RowHolder rowHolder) {
        if (!rowHolder.avail) {
            asyncLoadThumbnail(rowHolder);
            rowHolder.avail = true;
        }
    }

    private synchronized void asyncLoadThumbnail(final RowHolder rowHolder) {
        if (rowHolder.imageViewHolders.isEmpty()) {
            return;
        }
        PAGING_CACHE.setUseDummies(false);
        final AtomicInteger idx = new AtomicInteger(0);
        idx.set(rowHolder.imageViewHolders.size() - 1);
        AppDatabase appDatabase = PAGING_CACHE.getAppDatabase();
        List<String> idList = rowHolder.imageViewHolders.stream()
                .map(h -> String.valueOf(h.uid)).collect(Collectors.toList());
        ListenableFuture<List<RenderResultLightWeight>> future = appDatabase
                .renderResultDao().getLightWeightByIds(idList);
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            PAGING_CACHE.addAll(result);
            result.forEach(r -> {
                for (int n = 0; n < rowHolder.imageViewHolders.size(); n++) {
                    ImageView imageView = rowHolder.imageViewHolders.get(n)
                            .imgLinearLayout.findViewById(R.id.single_gallery_image_view);
                    if (r.uid == rowHolder.imageViewHolders.get(n).uid && imageView != null) {
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transform(new CenterCrop(),
                                new RoundedCorners(32));
                        Glide.with(mContext)
                                .asBitmap()
                                .load(IMAGE_UTIL.convertBlobToImage(r.thumbNail))
                                .apply(requestOptions)
                                .into(imageView);
                    }
                }
            });
        }, mContext);
    }

    @Override
    public void notifyRowHeight(int height) {
        mRowHeight = height;
    }

    @Override
    public LinearLayout getLinearLayoutByUid(int uid) {
        for (RowHolder h : mRowHolderList) {
            Optional<ImageViewHolder> optional = h.imageViewHolders.stream().filter(ih -> ih.uid == uid).findAny();
            if (optional.isPresent()) {
                return optional.get().imgLinearLayout;
            }
        }
        return null;
    }

    @Override
    public void setCheckBoxesVisibilty(boolean visible) {
        List<RowHolder> list = mRowHolderList.stream().filter(h -> h.avail)
                .collect(Collectors.toList());
        list.forEach(l -> l.imageViewHolders.forEach(h -> {
            h.imgLinearLayout
                    .findViewById(R.id.check_single_entry)
                    .setVisibility(visible ? View.VISIBLE : View.GONE);
        }));
    }

    @Override
    public boolean isAnyCheckBoxChecked() {
        AtomicBoolean result = new AtomicBoolean(false);
        List<RowHolder> list = mRowHolderList.stream().filter(h -> h.avail)
                .collect(Collectors.toList());
        list.forEach(l -> {
            boolean anyChecked = l.imageViewHolders.stream().map(h -> (CheckBox) h.imgLinearLayout.findViewById(R.id.check_single_entry)).anyMatch(CompoundButton::isChecked);
            if(anyChecked) {
                result.set(true);
            }
        });
        return result.get();
    }

    private LinearLayout createImageView(final byte[] thumbNail) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(mContext).inflate(R.layout.single_gallery_image, mRowLayout, false);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(32));
        ImageView imageView = linearLayout.findViewById(R.id.single_gallery_image_view);
        if (!mUseDummies.get()) {
            if (thumbNail != null) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(IMAGE_UTIL.convertBlobToImage(thumbNail))
                        .apply(requestOptions)
                        .into(imageView);
            } else {
                Glide.with(mContext)
                        .load(R.drawable.no_image)
                        .apply(requestOptions)
                        .into(imageView);
            }
        }
        linearLayout.setPadding(0, 0, 4, 4);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.width = (mScreen.width - 4 * THUMBS_PER_ROW) / THUMBS_PER_ROW;
        if (IMAGE_UTIL.getImageBounds(imageView).height() < params.width) {
            params.height = (mScreen.width - 4 * THUMBS_PER_ROW) / THUMBS_PER_ROW;
        }
        mRowLayout.addView(linearLayout, params);
        linearLayout.setClickable(!mUseDummies.get());
        linearLayout.setLongClickable(!mUseDummies.get());
        return linearLayout;
    }


    private static class RowHolder {
        public List<ImageViewHolder> imageViewHolders = new ArrayList<>();
        public boolean avail;

    }

    private static class ImageViewHolder {
        public int uid;
        public LinearLayout imgLinearLayout;

        public ImageViewHolder(int uid, LinearLayout linearLayout) {
            this.uid = uid;
            this.imgLinearLayout = linearLayout;
        }
    }
}