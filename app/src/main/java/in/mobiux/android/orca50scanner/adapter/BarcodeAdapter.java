package in.mobiux.android.orca50scanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.Barcode;

/**
 * Created by SUJEET KUMAR on 21-May-21.
 */
public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.RecyclerViewHolder> {

    private Context context;
    private List<Barcode> barcodes;

    public BarcodeAdapter(Context context, List<Barcode> barcodes) {
        this.context = context;
        this.barcodes = barcodes;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_barcode, parent, false);
        return new RecyclerViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull BarcodeAdapter.RecyclerViewHolder holder, int position) {

        holder.tvId.setText("" + (position + 1));
        holder.tvName.setText(barcodes.get(position).toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return barcodes.size();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvId, tvName;

        public RecyclerViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);

            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}


