package in.mobiux.android.orca50scanner.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.reader.model.Inventory;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.RecyclerViewHolder> {

    Activity context;
    List<Inventory> inventories;
    InventoryClickListener listener;

    public InventoryAdapter(Activity context, List<Inventory> inventories) {
        this.context = context;
        this.inventories = inventories;
    }

    public void setValues(List<Inventory> values) {
        this.inventories = values;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);

        return new RecyclerViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        Inventory inventory = inventories.get(position);

        holder.tvRssi.setText("" + inventory.getRssi());
        holder.tvName.setText("" + inventory.getEpc());

        if (inventory.isScanStatus()) {
            holder.ivStatus.setImageResource(R.drawable.ic_check_circle_24);
        } else {
            holder.ivStatus.setImageResource(R.drawable.icon_cancel);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick(inventories.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventories.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRssi, tvName;
        private ImageView ivStatus;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRssi = itemView.findViewById(R.id.tvRSSI);
            tvName = itemView.findViewById(R.id.tvName);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            ivStatus.setVisibility(View.GONE);
        }
    }

    public void setOnItemClickListener(InventoryClickListener listener) {
        this.listener = listener;
    }

    public interface InventoryClickListener {
        void onClick(Inventory inventory);
    }
}
