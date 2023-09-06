package org.ww.ai.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.adapter.OnGallerySelectionIF;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.ui.DialogUtil;

import java.util.List;

public class TrashBinFragment extends GalleryFragment {

    private TextView mEmptyTrashView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsTrashMode = true;
    }

    @Override
    protected void additionalOnViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.additionalOnViewCreated(view, savedInstanceState);
        mEmptyTrashView = view.findViewById(R.id.btn_empty_trash);
        mEmptyTrashView.setVisibility(View.VISIBLE);
        if (mGallerySize > 0) {
            mEmptyTrashView.setOnClickListener(l -> {
                DialogUtil.DIALOG_UTIL.showPrompt(
                        getContext(),
                        R.string.lbl_trash_bin_title,
                        R.string.empty_trash_prompt,
                        R.string.btn_yes,
                        (dialog, which) -> emptyTrash(),
                        R.string.btn_no,
                        (dialog, which) -> {
                        },
                        R.drawable.warning
                );
            });
        } else {
            mEmptyTrashView.setEnabled(false);
            mEmptyTrashView.setTextColor(getResources().getColor(R.color.lightGrayTransparent, null));
        }
    }

    protected int getMenuResourceId() {
        return R.menu.trashbinmenu;
    }

    @Override
    protected void handleMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_restore) {
            List<String> uids = mAdapter.getSelectedUids();
            if (uids != null && !uids.isEmpty()) {
                performRestore(uids);
            }
        }
    }

    private void performRestore(List<String> uids) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        ListenableFuture<Integer> future = db.renderResultDao().updateDeleteFlag(uids, false);
        AsyncDbFuture<Integer> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, count -> {
            OnGallerySelectionIF onGalleryThumbSelection = mAdapter.getOnGalleryThumbSelection();
            mAdapter.afterAsyncDelete(count);
            onGalleryThumbSelection.onDeleteDone();
            removeMenuToolbar();
        }, requireContext());
    }

    @Override
    protected void showNothingToDisplayImage() {
        LinearLayout linearLayout = (LinearLayout) mRecyclerView.getParent();
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_trash,
                linearLayout, false);
        linearLayout.addView(emptyView);

        mEmptyTrashView.setEnabled(false);
        mEmptyTrashView.setTextColor(getResources().getColor(R.color.lightGrayTransparent, null));
    }

    private void emptyTrash() {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        ListenableFuture<Integer> listenableFuture =
                appDatabase.renderResultDao().emptyTrash();
        AsyncDbFuture<Integer> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture,
                r -> {
                    ((ViewGroup) mRecyclerView.getParent()).removeAllViews();
                    showNothingToDisplayImage();
                }, requireContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeMenuToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.refresh();
    }
}
