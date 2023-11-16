package org.ww.ai.fragment;

import static org.ww.ai.fragment.RenderDetailsFragment.ARG_UID;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.adapter.AbstractRenderResultViewHolder;
import org.ww.ai.adapter.OnGallerySelectionIF;
import org.ww.ai.adapter.RenderHistoryAdapter;
import org.ww.ai.databinding.RenderResultsFragmentBinding;
import org.ww.ai.enumif.ReceiveEventIF;
import org.ww.ai.rds.AppDatabase;

public class RenderResultsFragment extends Fragment implements ReceiveEventIF, OnGallerySelectionIF {

    protected RenderHistoryAdapter mAdapter;
    private RenderResultsFragmentBinding binding;
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
        mRecyclerView = view.findViewById(R.id.render_result_List);
        mGallerySize = AppDatabase.getInstance(requireContext()).renderResultDao().getCount(mIsTrashMode);
        // TODO: check if needed or not: additionalOnViewCreated(view, savedInstanceState);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mAdapter = new RenderHistoryAdapter(requireContext(), displayMetrics,
                this, mGallerySize, false);
        mRecyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), mAdapter.getPerRow());
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        if (mGallerySize == 0) {
            showNothingToDisplayImage();
        }
        test();
    }

    private void test() {
        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.YELLOW);

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.d("ONSWIPED", "direction: " + direction + ", " + viewHolder);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                if (dX > 0) {
                    background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
                } else if (dX < 0) {
                    background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }

                background.draw(c);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
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
        NavController navController = NavHostFragment.findNavController(RenderResultsFragment.this);
        Bundle bundle = new Bundle();
        bundle.putInt(RenderDetailsFragment.ARG_UID, uid);
        navController.navigate(R.id.action_RenderResultsFragment_to_GalleryFullSizeFragment, bundle);
    }

    @Override
    public void receiveEvent(Object... eventObject) {

    }

}
