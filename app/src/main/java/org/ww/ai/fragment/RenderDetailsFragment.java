package org.ww.ai.fragment;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.activity.ReceiveImageActivity;
import org.ww.ai.databinding.RenderDetailsFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.tools.ShareImageUtil;
import org.ww.ai.ui.DialogUtil;
import org.ww.ai.ui.ImageUtil;
import org.ww.ai.ui.inclues.ShowRenderModelsLinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class RenderDetailsFragment extends Fragment {

    public static final String ARG_UID = "uid";
    private int uid;

    private RenderDetailsFragmentBinding mBinding;

    private Context mContainerContext;

    private ActivityResultLauncher<Intent> mReceiveActivityResultLauncher;

    private RenderResult mOrriginalRenderResult;

    private View mLocalView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getInt(ARG_UID);
        }
        mReceiveActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                Uri imageUri = data.getData();
                byte[] bytes = getImageFromGallery(imageUri);
                if (bytes != null) {
                    promptReplace(bytes, mOrriginalRenderResult.image.length);
                }
            }
        });
    }

    private void promptReplace(final byte[] bytes, int lengthOld) {
        assert getContext() != null;
        String msg = getContext().getResources().getString(R.string.prompt_replace_image, lengthOld, bytes.length);
        DialogUtil.DIALOG_UTIL.showPrompt(getContext(),
                mOrriginalRenderResult.queryString,
                msg,
                R.string.btn_yes,
                (dialog, which) -> updateImage(bytes),
                R.string.btn_no,
                (dialog, which) -> {
                },
                R.drawable.question
        );
    }

    private byte[] getImageFromGallery(Uri imageUri) {
        if (imageUri != null && getActivity() != null && getActivity().getContentResolver() != null) {
            Bitmap bitmap;
            try (InputStream in = getActivity().getContentResolver().openInputStream(imageUri)) {
                bitmap = BitmapFactory.decodeStream(in);
                return IMAGE_UTIL.convertImageToBlob(IMAGE_UTIL.getScaledBitmap(
                        bitmap, ReceiveImageActivity.MAX_IMAGE_SIZE));
            } catch (IOException e) {
                Toast.makeText(getContext(), "ERR: unable to load image: " + imageUri, Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        assert container != null;
        this.mContainerContext = container.getContext();
        mBinding = RenderDetailsFragmentBinding.inflate(inflater, container, false);
        if (savedInstanceState != null) {
            //noinspection DataFlowIssue
            uid = (int) savedInstanceState.get(ARG_UID);
        }
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mLocalView = view;
        if (uid > Integer.MIN_VALUE) {
            loadRenderResultFromDatabase(view, uid);
        }
    }

    private void loadRenderResultFromDatabase(View view, int uid) {
        AppDatabase db = AppDatabase.getInstance(mContainerContext);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                mOrriginalRenderResult = result;
                fillContentViewFromResult(view, result);
            }
        }, mContainerContext);
    }

    private void fillContentViewFromResult(View view, RenderResult result) {
        setImageViewFromBytes(view, result.image);
        TextView titleTextView = view.findViewById(R.id.history_title);
        titleTextView.setText(result.queryString);
        Button btnShare = view.findViewById(R.id.btn_share_render_results);
        btnShare.setOnClickListener(v -> new ShareImageUtil(getActivity()).startShare(result.uid, true));

        Button btnReplace = view.findViewById(R.id.btn_replace_render_results);
        btnReplace.setOnClickListener(v -> replaceImage());

        EditText textViewWhatWasRendered = view.findViewById(R.id.what_was_rendered_value);
        textViewWhatWasRendered.setText(result.queryString);
        EditText textViewWhatWasUsed = view.findViewById(R.id.what_was_rendered_query_value);
        textViewWhatWasUsed.setText(result.queryUsed);
        TextView textViewDate = view.findViewById(R.id.what_was_rendered_date);
        textViewDate.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(new Date(result.createdTime)));

        if(result.enginesUsed != null && !result.enginesUsed.isEmpty()) {
            ViewGroup engineLayout = view.findViewById(R.id.render_details_include);
            ShowRenderModelsLinearLayout enginesUsedView = new ShowRenderModelsLinearLayout(mContainerContext);
            enginesUsedView.init(mContainerContext, engineLayout, result.enginesUsed);
            engineLayout.addView(enginesUsedView);
        }

    }


    private void replaceImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
        mReceiveActivityResultLauncher.launch(intent);
    }

    private void updateImage(byte[] bytes) {
        mOrriginalRenderResult.image = bytes;
        Bitmap thumbNail = IMAGE_UTIL.getScaledBitmap(IMAGE_UTIL.convertBlobToImage(bytes), ImageUtil.THUMB_NAIL_SIZE);
        mOrriginalRenderResult.thumbNail = IMAGE_UTIL.convertImageToBlob(thumbNail);
        AppDatabase db = AppDatabase.getInstance(getActivity());
        assert getActivity() != null;
        ListenableFuture<Integer> future = db.renderResultDao().updateRenderResults(List.of(mOrriginalRenderResult));
        AsyncDbFuture<Integer> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, i -> {
            if (i == 1) {
                setImageViewFromBytes(mLocalView, bytes);
            } else {
                Toast.makeText(getActivity(), "Error updating image", Toast.LENGTH_LONG).show();
            }
        }, getActivity());
    }

    private void setImageViewFromBytes(View view, byte[] bytes) {
        ImageView imageView = view.findViewById(R.id.history_bitmap);
        assert getActivity() != null;
        IMAGE_UTIL.setFittingImageViewFromBitmap(getActivity(), imageView, bytes);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }
}