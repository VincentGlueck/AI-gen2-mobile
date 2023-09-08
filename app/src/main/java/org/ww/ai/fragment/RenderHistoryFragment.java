package org.ww.ai.fragment;

import static org.ww.ai.fragment.RenderDetailsFragment.ARG_UID;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.adapter.AbstractRenderResultViewHolder;
import org.ww.ai.adapter.OnGallerySelectionIF;
import org.ww.ai.adapter.RenderResultAdapter;
import org.ww.ai.databinding.RenderResultsFragmentBinding;
import org.ww.ai.enumif.ReceiveEventIF;
import org.ww.ai.rds.AppDatabase;

public class RenderHistoryFragment extends Fragment implements ReceiveEventIF, OnGallerySelectionIF {

    private RenderResultsFragmentBinding binding;

    protected RenderResultAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected boolean mIsTrashMode;
    protected int mGallerySize;
    private int mUid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUid = getArguments().getInt(ARG_UID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = RenderResultsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.gallery_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mGallerySize = AppDatabase.getInstance(requireContext()).renderResultDao().getCount(mIsTrashMode);
        // TODO: check if needed or not: additionalOnViewCreated(view, savedInstanceState);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mAdapter = new RenderResultAdapter(requireContext(), displayMetrics,
                this, mGallerySize, mIsTrashMode);
        mRecyclerView.setAdapter(mAdapter);
        if (mGallerySize == 0) {
            showNothingToDisplayImage();
        }
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    protected void showNothingToDisplayImage() {
        LinearLayout linearLayout = (LinearLayout) mRecyclerView.getParent();
        linearLayout.removeAllViews();
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_result,
                linearLayout, false);
        linearLayout.addView(emptyView);
        linearLayout.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void thumbSelected(boolean selected, AbstractRenderResultViewHolder holder, int position) {

    }

    @Override
    public void onDeleteDone() {

    }

    @Override
    public void onImageClickListener(int uid) {

    }

    @Override
    public void receiveEvent(Object... eventObject) {

    }

}
