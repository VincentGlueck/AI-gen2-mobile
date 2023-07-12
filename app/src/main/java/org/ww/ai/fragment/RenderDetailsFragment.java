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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.activity.ReceiveImageActivity;
import org.ww.ai.databinding.RenderDetailsFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.ifenum.RenderModel;
import org.ww.ai.tools.ShareImageUtil;
import org.ww.ai.ui.DialogUtil;
import org.ww.ai.ui.ImageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class RenderDetailsFragment extends Fragment {

    public static final String ARG_UID = "uid";
    private int uid;

    private RenderDetailsFragmentBinding binding;

    private Context containerContext;

    private ActivityResultLauncher<Intent> receiveActivityResultLauncher;

    private RenderResult originalRenderResult;

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getInt(ARG_UID);
        }

        receiveActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                Uri imageUri = data.getData();
                byte[] bytes = getImageFromGallery(imageUri);
                if (bytes != null) {
                    promptReplace(bytes, originalRenderResult.image.length);
                }
            }
        });
    }

    private void promptReplace(final byte[] bytes, int lengthOld) {
        assert getContext() != null;
        String msg = getContext().getResources().getString(R.string.prompt_replace_image, lengthOld, bytes.length);
        DialogUtil.DIALOG_UTIL.showPrompt(getContext(),
                originalRenderResult.queryString,
                msg,
                R.string.btn_yes,
                (dialog, which) -> {
                    updateImage(bytes);
                },
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
        this.containerContext = container.getContext();
        binding = RenderDetailsFragmentBinding.inflate(inflater, container, false);
        if (savedInstanceState != null) {
            uid = (int) savedInstanceState.get(ARG_UID);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (uid > Integer.MIN_VALUE) {
            loadRenderResultFromDatabase(view, uid);
        }
    }

    private void loadRenderResultFromDatabase(View view, int uid) {
        AppDatabase db = AppDatabase.getInstance(containerContext);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                originalRenderResult = result;
                fillContentViewFromResult(view, result);
            }
        }, containerContext);
    }

    private void fillContentViewFromResult(View view, RenderResult result) {
        setImageViewFromBytes(view, result.image);
        TextView titleTextView = view.findViewById(R.id.history_title);
        titleTextView.setText(result.queryString);
        Button btnShare = view.findViewById(R.id.btn_share_render_results);
        btnShare.setOnClickListener(v -> new ShareImageUtil(getActivity()).startShare(result.uid));

        Button btnReplace = view.findViewById(R.id.btn_replace_render_results);
        btnReplace.setOnClickListener(v -> replaceImage());

        EditText textViewWhatWasRendered = view.findViewById(R.id.what_was_rendered_value);
        textViewWhatWasRendered.setText(result.queryString);
        EditText textViewWhatWasUsed = view.findViewById(R.id.what_was_rendered_query_value);
        textViewWhatWasUsed.setText(result.queryUsed);
        TextView textViewCredits = view.findViewById(R.id.what_was_rendered_credits);
        textViewCredits.setText(String.valueOf(result.credits));
        TextView textViewDate = view.findViewById(R.id.what_was_rendered_date);
        textViewDate.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(new Date(result.createdTime)));
        Spinner spinnerRenderedBy = view.findViewById(R.id.what_was_rendered_engine_spinner);
        ArrayAdapter<String> renderedByAdapter = new ArrayAdapter<>(containerContext,
                android.R.layout.simple_spinner_item, RenderModel.getAvailableModels());
        renderedByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRenderedBy.setAdapter(renderedByAdapter);
        spinnerRenderedBy.setSelection(result.renderEngine.ordinal());
        spinnerRenderedBy.setEnabled(false);
    }


    private void replaceImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
        receiveActivityResultLauncher.launch(intent);
    }

    private void updateImage(byte[] bytes) {
        originalRenderResult.image = bytes;
        Bitmap thumbNail = IMAGE_UTIL.getScaledBitmap(IMAGE_UTIL.convertBlobToImage(bytes), ImageUtil.THUMB_NAIL_SIZE);
        originalRenderResult.thumbNail = IMAGE_UTIL.convertImageToBlob(thumbNail);
        AppDatabase db = AppDatabase.getInstance(getActivity());
        assert getActivity() != null;
        ListenableFuture<Integer> future = db.renderResultDao().updateRenderResults(List.of(originalRenderResult));
        AsyncDbFuture<Integer> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, i -> {
            if (i == 1) {
                setImageViewFromBytes(view, bytes);
            } else {
                Toast.makeText(getActivity(), "Error updating image", Toast.LENGTH_LONG).show();
            }
        }, getActivity());
    }

    private void setImageViewFromBytes(View view, byte[] bytes) {
        SubsamplingScaleImageView imageView = view.findViewById(R.id.history_bitmap);
        assert getActivity() != null;
        IMAGE_UTIL.setFittingImageViewFromBitmap(getActivity(), imageView, bytes);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}