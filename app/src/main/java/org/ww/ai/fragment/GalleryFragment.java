package org.ww.ai.fragment;

import static org.ww.ai.adapter.GenericThumbnailAdapter.SCALE_SELECTED;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.adapter.AbstractRenderResultViewHolder;
import org.ww.ai.adapter.GalleryAdapter;
import org.ww.ai.adapter.OnGallerySelectionIF;
import org.ww.ai.databinding.GalleryFragmentBinding;
import org.ww.ai.enumif.ReceiveEventIF;
import org.ww.ai.prefs.Preferences;
import org.ww.ai.rds.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements ReceiveEventIF, OnGallerySelectionIF {

    protected RecyclerView mRecyclerView;
    protected GalleryAdapter mAdapter;
    protected boolean mIsTrashMode;
    private MenuProvider mMenuProvider;
    private boolean mDeleteMode;
    protected int mGallerySize;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        org.ww.ai.databinding.GalleryFragmentBinding binding =
                GalleryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    protected void additionalOnViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // trash bin does something here...
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.gallery_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mGallerySize = AppDatabase.getInstance(requireContext()).renderResultDao().getCount(mIsTrashMode);
        additionalOnViewCreated(view, savedInstanceState);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mAdapter = new GalleryAdapter(requireContext(), displayMetrics,
                this, mGallerySize, mIsTrashMode);
        mRecyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), mAdapter.getPerRow());
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        if(mGallerySize == 0) {
            showNothingToDisplayImage();
        }
    }

    protected int getMenuResourceId() {
        return R.menu.gallerymenu;
    }

    private void addMenuToolbar() {
        if (mMenuProvider != null) {
            requireActivity().removeMenuProvider(mMenuProvider);
        }
        MenuHost menuHost = requireActivity();
        mMenuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(getMenuResourceId(), menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                handleMenuItemSelected(menuItem);
                return false;
            }
        };
        menuHost.addMenuProvider(mMenuProvider);
    }


    private void performDelete() {
        final boolean useTrash = Preferences.getInstance(requireContext())
                .getBoolean(Preferences.PREF_USE_TRASH);
        mAdapter.deleteSelected(mDeleteMode, mIsTrashMode);
        if (mAdapter.getSelectedThumbs().isEmpty()) {
            showNothingToDisplayImage();
        } else {
            mAdapter.deleteSelected(useTrash, mIsTrashMode);
        }
    }

    protected void showNothingToDisplayImage() {
        LinearLayout linearLayout = (LinearLayout) mRecyclerView.getParent();
        linearLayout.removeAllViews();
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_result,
                linearLayout, false);
        linearLayout.addView(emptyView);
        linearLayout.setBackgroundColor(Color.BLACK);
    }


    protected void updateToolbar() {
        if (!mDeleteMode && mAdapter.isAnySelected()) {
            addMenuToolbar();
            mDeleteMode = true;
        }
        if (!mAdapter.isAnySelected() && mDeleteMode) {
            removeMenuToolbar();
        }
    }

    protected void removeMenuToolbar() {
        requireActivity().removeMenuProvider(mMenuProvider);
        mMenuProvider = null;
        mDeleteMode = false;
    }

    protected void handleMenuItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_delete) {
            performDelete();
        }
    }


    @Override
    public void receiveEvent(Object... eventObject) {
        Log.d("RECEIVE", "I got something, it's: "
                + (eventObject.length == 0 ? "empty :-(" : eventObject));
    }

    @Override
    public void thumbSelected(boolean selected, AbstractRenderResultViewHolder holder, int position) {
        updateThumbNailUI();
    }

    private void updateThumbNailUI() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        assert linearLayoutManager != null;
        int first = linearLayoutManager.findFirstVisibleItemPosition();
        int last = linearLayoutManager.findLastVisibleItemPosition() - first;
        if (mAdapter.getFromX() == null) {
            mAdapter.setFromX((float) (mAdapter.getThumbWidth()) * SCALE_SELECTED);
            mAdapter.setFromY((float) (mAdapter.getThumbHeight()) * SCALE_SELECTED);
        }
        float fromX = mAdapter.isSelectionMode() ? 1.0f : mAdapter.getFromX() / (float) mAdapter.getThumbWidth();
        float fromY = mAdapter.isSelectionMode() ? 1.0f : mAdapter.getFromY() / (float) mAdapter.getThumbHeight();
        float toX = mAdapter.isSelectionMode() ? mAdapter.getFromX() / (float) mAdapter.getThumbWidth() : 1.0f;
        float toY = mAdapter.isSelectionMode() ? mAdapter.getFromY() / (float) mAdapter.getThumbHeight() : 1.0f;
        AnimatorSet animatorSet = new AnimatorSet();
        List<ObjectAnimator> animators = new ArrayList<>();
        for (int n = 0; n <= last; n++) {
            LinearLayout childLayout = (LinearLayout) linearLayoutManager.getChildAt(n);
            if (childLayout == null) {
                continue;
            }
            View galleryImage = childLayout.findViewById(R.id.single_gallery_image_view);
            CheckBox checkBox = childLayout.findViewById(R.id.check_single_entry);
            checkBox.setVisibility(mAdapter.isSelectionMode() ? View.VISIBLE : View.GONE);
            animators.add(ObjectAnimator.ofFloat(galleryImage, "scaleX", fromX, toX));
            animators.add(ObjectAnimator.ofFloat(galleryImage, "scaleY", fromY, toY));
        }
        Animator[] animatorsArray = animators.toArray(new Animator[0]);
        animatorSet.playTogether(animatorsArray);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAdapter.notifyItemRangeChanged(first, last);
            }
        });
        updateToolbar();
    }

    @Override
    public void onImageClickListener(int uid) {
        if(uid >= 0) {
            NavController navController = NavHostFragment.findNavController(GalleryFragment.this);
            Bundle bundle = new Bundle();
            bundle.putInt(RenderDetailsFragment.ARG_UID, uid);
            navController.navigate(R.id.action_GalleryFragment_to_GalleryFullSizeFragment, bundle);
        }
    }

    @Override
    public void onDeleteDone() {
        updateToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.refresh();
    }
}
