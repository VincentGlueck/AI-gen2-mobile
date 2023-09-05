package org.ww.ai.adapter;

import static org.ww.ai.ui.Animations.ANIMATIONS;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.rds.RecyclerViewPagingCache;

import java.util.stream.Collectors;

public class RenderHistoryAdapter extends GenericThumbnailAdapter<RenderResultViewHolder> {

    private static final long FADE_TIME = 200L;

    public RenderHistoryAdapter(Context context,
                                DisplayMetrics displayMetrics,
                                OnGallerySelectionIF onGalleryThumbSelection,
                                int count,
                                boolean useTrash) {
        super(context, displayMetrics, onGalleryThumbSelection, count, useTrash);
    }

    @Override
    protected void displayThumbnail(@NonNull RecyclerViewPagingCache.PagingEntry pagingEntry) {
        // TODO similar to GalleryAdapter's implementation
    }

    @NonNull
    @Override
    public RenderResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View singleGalleryView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_gallery_image, parent, false);
        return new RenderResultViewHolder(singleGalleryView);
    }

    @Override
    public void onBindViewHolder(@NonNull RenderResultViewHolder holder, int position) {
        super.onViewRecycled(holder);
        holder.thumbNail.startAnimation(
                ANIMATIONS.getAlphaAnimation(1.0f, 0.0f, FADE_TIME, true)
        );
        holder.checkBox.setVisibility(mSelectionMode ? View.VISIBLE : View.GONE);
        int absPosition = holder.getAbsoluteAdapterPosition();
        if (absPosition != RecyclerView.NO_POSITION) {
            holder.position = absPosition;
        }
        holder.checkBox.setChecked(mSelectedThumbs.stream().map(s -> s.position)
                .collect(Collectors.toList()).contains(holder.position));
    }
}
