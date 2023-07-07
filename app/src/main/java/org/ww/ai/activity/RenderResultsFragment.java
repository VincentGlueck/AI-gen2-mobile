package org.ww.ai.activity;

import static org.ww.ai.activity.RenderDetailsFragment.ARG_UID;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.RenderResultsFragmentBinding;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.RenderResultAdapter;
import org.ww.ai.ui.SwipeToDeleteCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RenderResultsFragment extends Fragment implements RenderResultAdapter.OnItemClickListener {

    private RenderResultsFragmentBinding binding;
    private Context containerContext;
    private RenderResultAdapter adapter;
    private LinearLayout linearLayout;
    private RecyclerView renderResultView;
    private final Map<Integer, LightWeightDeleteHolder> renderResultLightWeights = new HashMap<>();

    private int uid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getInt(ARG_UID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        assert container != null;
        this.containerContext = container.getContext();

        binding = RenderResultsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.renderResultList.setOnClickListener(view1 -> NavHostFragment.findNavController(
                RenderResultsFragment.this).navigate(R.id.action_RenderResultsFragment_to_MainFragment));

        renderResultView = view.findViewById(R.id.render_result_List);
        adapter = new RenderResultAdapter(containerContext, this);

        renderResultView.setAdapter(adapter);
        renderResultView.setLayoutManager(new LinearLayoutManager(containerContext));
        renderResultView.addItemDecoration(new DividerItemDecoration(renderResultView.getContext(), DividerItemDecoration.VERTICAL));
        getRenderResultsFromDatabase();

        linearLayout = view.findViewById(R.id.render_result_linear_layout);
        enableSwipeToDeleteAndUndo();

    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(containerContext) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                renderResultLightWeights.put(adapter.itemAt(position).uid, new LightWeightDeleteHolder(position, adapter.itemAt(position)));
                adapter.removeResult(position);

                Snackbar snackbar = Snackbar.make(linearLayout, getText(R.string.history_entry_deleted_snackbar), Snackbar.LENGTH_LONG);
                snackbar.setAction(getText(R.string.undo_snackbar), view -> {
                    AtomicInteger lastPosition = new AtomicInteger();
                    renderResultLightWeights.values().forEach(r -> {
                        adapter.restoreResult(r.renderResultLightWeight, r.position);
                        lastPosition.set(r.position);
                    });
                    renderResultView.scrollToPosition(lastPosition.get());
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event == 2) { // auto dismiss, nothing clicked
                            permanentDelete();
                        }
                    }
                });

            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(renderResultView);
    }

    private void permanentDelete() {
        AppDatabase db = AppDatabase.getInstance(containerContext);
        renderResultLightWeights.values().forEach(r -> {
            ListenableFuture<RenderResult> future = db.renderResultDao().getById(r.renderResultLightWeight.uid);
            AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
            asyncDbFuture.processFuture(future, result -> {
                ListenableFuture<Integer> delFuture = db.renderResultDao().deleteRenderResults(List.of(result));
                AsyncDbFuture<Integer> asyncDbFutureDel = new AsyncDbFuture<>();
                asyncDbFutureDel.processFuture(delFuture, i -> {
                }, containerContext);
            }, containerContext);
        });
        renderResultLightWeights.clear();
    }

    private void getRenderResultsFromDatabase() {
        AppDatabase appDatabase = AppDatabase.getInstance(containerContext);
        ListenableFuture<List<RenderResultLightWeight>> listenableFuture = appDatabase.renderResultDao().getAllLightWeights();
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture, renderResults -> {
            adapter.addRenderResults(renderResults);
            if(uid >= 0) {
                int position = adapter.getPositionOfUid(uid);
                if(position >= 0) {
                    renderResultView.scrollToPosition(position);
                    adapter.highLightNewRow(position);
                    adapter.notifyItemChanged(position);
                }
            }
        }, containerContext);
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(RenderResultLightWeight item) {
        NavController navController = NavHostFragment.findNavController(RenderResultsFragment.this);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_UID, item.uid);
        navController.navigate(R.id.action_RenderResultsFragment_to_ShowRenderDetailsFragment, bundle);
    }

    private static class LightWeightDeleteHolder {
        public int position;
        public RenderResultLightWeight renderResultLightWeight;

        public LightWeightDeleteHolder(int position, RenderResultLightWeight renderResultLightWeight) {
            this.position = position;
            this.renderResultLightWeight = renderResultLightWeight;
        }
    }
}
