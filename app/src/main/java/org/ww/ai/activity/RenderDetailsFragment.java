package org.ww.ai.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.RenderDetailsFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.ifenum.RenderModel;
import org.ww.ai.ui.ImageUtil;

import java.text.DateFormat;
import java.util.Date;

public class RenderDetailsFragment extends Fragment {

    public static final String ARG_UID = "uid";
    private int uid;

    private RenderDetailsFragmentBinding binding;

    private Context containerContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getInt(ARG_UID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        assert container != null;
        this.containerContext = container.getContext();
        binding = RenderDetailsFragmentBinding.inflate(inflater, container, false);
        if(savedInstanceState != null) {
            uid = (int) savedInstanceState.get(ARG_UID);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(uid > Integer.MIN_VALUE) {
            loadRenderResultFromDatabase(view, uid);
        }
    }

    private void loadRenderResultFromDatabase(View view, int uid) {
        AppDatabase db = AppDatabase.getInstance(containerContext);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if(result != null) {
                fillContentViewFromResult(view, result);
            }
        }, containerContext);
    }

    private void fillContentViewFromResult(View view, RenderResult result) {
        ImageView imageView = view.findViewById(R.id.history_bitmap);
        Bitmap bitmap = ImageUtil.IMAGE_UTIL.convertBlobToImage(result.image);
        Toast.makeText(containerContext, "bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight(), Toast.LENGTH_LONG).show();
        imageView.setImageBitmap(bitmap);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}