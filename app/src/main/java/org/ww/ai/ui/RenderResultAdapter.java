package org.ww.ai.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.rds.entity.RenderResult;

import java.util.ArrayList;
import java.util.List;

public class RenderResultAdapter  extends RecyclerView.Adapter<RenderResultAdapter.ViewHolder> {

    private final List<RenderResult> localDataSet;

    public RenderResultAdapter(List<RenderResult> localDataSet) {
        this.localDataSet = localDataSet;
    }

    public RenderResultAdapter() {
        this.localDataSet = new ArrayList<>();
    }

    public void addRenderResults(List<RenderResult> renderResults) {
        if(renderResults == null || renderResults.isEmpty()) {
            Log.d("ADD_RENDER_RESULTS", "Attempt to add null or empty list of RenderResults");
            return;
        }
        localDataSet.addAll(renderResults);
    }

    @NonNull
    @Override
    public RenderResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.render_result_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RenderResultAdapter.ViewHolder viewHolder, int position) {
        viewHolder.getTextView().setText(localDataSet.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.render_result_title);
            textView.setOnClickListener(onClick -> {
                Toast.makeText(getTextView().getContext(), "Click", Toast.LENGTH_LONG).show();
            });
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
