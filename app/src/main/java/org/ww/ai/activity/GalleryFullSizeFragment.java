package org.ww.ai.activity;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.GalleryFullSizeFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.ui.MetricsUtil;

import java.util.List;

public class GalleryFullSizeFragment extends Fragment {

    private int uid;

    private Context containerContext;

    private MetricsUtil.Screen screen;

    private GalleryFullSizeFragmentBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getInt(RenderDetailsFragment.ARG_UID);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        assert container != null;
        this.containerContext = container.getContext();
        binding = GalleryFullSizeFragmentBinding.inflate(inflater, container, false);
        if (savedInstanceState != null) {
            uid = (int) savedInstanceState.get(RenderDetailsFragment.ARG_UID);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null && getActivity().getWindowManager() != null) {
            screen = MetricsUtil.getScreen(getActivity().getWindowManager());
        }

        ImageView imageView = view.findViewById(R.id.gallery_full_size_image);
        TextView imageDescriptionTextView = view.findViewById(R.id.lbl_gallery_full_size_footer);
        loadImageFromDatabase(imageView, imageDescriptionTextView, uid);
    }

    private void loadImageFromDatabase(ImageView imageView, TextView imageDescriptionTextView, int uid) {
        AppDatabase db = AppDatabase.getInstance(containerContext);
        ListenableFuture<RenderResult> future = db.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            if (result != null) {
                if (screen != null) {
                    imageView.setImageBitmap(IMAGE_UTIL.getScaledBitmap(IMAGE_UTIL.convertBlobToImage(result.image), screen.width));
                } else {
                    imageView.setImageBitmap(IMAGE_UTIL.convertBlobToImage(result.image));
                }
                imageDescriptionTextView.setText(result.queryUsed.length() > 0
                        ? result.queryUsed : result.queryString);
            }
        }, containerContext);
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
