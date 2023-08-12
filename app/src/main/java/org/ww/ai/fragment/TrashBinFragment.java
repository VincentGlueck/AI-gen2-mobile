package org.ww.ai.fragment;

import static android.graphics.Paint.UNDERLINE_TEXT_FLAG;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.ui.DialogUtil;

public class TrashBinFragment extends GalleryFragment {

    private TextView mTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowTrash = true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = view.findViewById(R.id.empty_trash);
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setPaintFlags(mTextView.getPaintFlags() | UNDERLINE_TEXT_FLAG);
        mTextView.setOnClickListener(l -> {
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
    }

    private void emptyTrash() {
        Log.d("TRASH", "would now send everything to hell...");
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        ListenableFuture<Integer> listenableFuture =
                appDatabase.renderResultDao().emptyTrash();
        AsyncDbFuture<Integer> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture,
                r -> {
                    mLinearLayout.removeAllViews();
                    showNothingToDisplayImage();
                }, requireContext());
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.trashbinmenu;
    }

    @Override
    protected void handleMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_restore) {
            mSelectedSet = getSelectedSet();
            if (mSelectedSet != null && !mSelectedSet.isEmpty()) {
                performRestore();
            }
        }
    }

    private void performRestore() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        mSelectedSet.forEach(r -> {
            ListenableFuture<RenderResult> future = db.renderResultDao().getById(Integer.parseInt(r));
            softDeleteFuture(db, future, false);
        });
        removeDeletedViewsFromParent();
        if (mRenderResults.isEmpty()) {
            showNothingToDisplayImage();
        } else {
            mTextView.setEnabled(true);
            mTextView.setTextColor(getResources().getColor(R.color.link, null));
        }
    }

    @Override
    protected void showNothingToDisplayImage() {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_trash,
                mLinearLayout, false);
        mLinearLayout.addView(emptyView);
        mTextView.setEnabled(false);
        mTextView.setTextColor(getResources().getColor(R.color.lightGrayTransparent, null));
    }

    private void onClick(DialogInterface dialog, int which) {
        emptyTrash();
    }
}
