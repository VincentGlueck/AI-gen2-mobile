package org.ww.ai.fragment;

import static org.ww.ai.ui.Animations.ANIMATIONS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.adapter.GalleryAdapter;
import org.ww.ai.adapter.OnGalleryThumbSelectionIF;
import org.ww.ai.adapter.RenderResultViewHolder;
import org.ww.ai.databinding.GalleryFragmentBinding;
import org.ww.ai.enumif.ReceiveEventIF;
import org.ww.ai.rds.AppDatabase;

public class GalleryFragment extends Fragment implements ReceiveEventIF, OnGalleryThumbSelectionIF {

    private static final float SCALE_SELECTED = 0.88f;
    private static final long FADE_TIME = 300L;
    private RecyclerView mRecyclerView;
    private GalleryAdapter mAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        org.ww.ai.databinding.GalleryFragmentBinding mBinding =
                GalleryFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.gallery_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        int gallerySize = AppDatabase.getInstance(requireContext()).renderResultDao().getCount();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); // set Horizontal Orientation
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new GalleryAdapter(requireContext(), this, gallerySize, false);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void animateOne(View view, boolean decreaseSize) {
        float from = SCALE_SELECTED;
        float to = 1.0f;
        if (decreaseSize) {
            float f = from;
            from = to;
            to = f;
        }
        final Animation animation = ANIMATIONS.getScaleAnimation(from, to, FADE_TIME, true);
        if (view != null) {
            view.startAnimation(animation);
        }
    }


    @Override
    public void receiveEvent(Object... eventObject) {

    }

    @Override
    public void thumbSelected(boolean selected, RenderResultViewHolder holder, int position) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        assert linearLayoutManager != null;
        int first = linearLayoutManager.findFirstVisibleItemPosition();
        int last = linearLayoutManager.findLastVisibleItemPosition();
        for (int n = first; n <= last; n++) {
            LinearLayout childLayout = (LinearLayout) linearLayoutManager.getChildAt(n);
            if(childLayout == null) {
                continue;
            }
            View galleryImage = childLayout.findViewById(R.id.single_gallery_image_view);
            CheckBox checkBox = childLayout.findViewById(R.id.check_single_entry);
            checkBox.setVisibility(mAdapter.isSelectionMode() ? View.VISIBLE : View.GONE);
            animateOne(galleryImage, mAdapter.isSelectionMode());
        }
    }
}
