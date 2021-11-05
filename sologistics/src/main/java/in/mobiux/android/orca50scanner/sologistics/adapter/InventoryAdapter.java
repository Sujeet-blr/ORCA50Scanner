package in.mobiux.android.orca50scanner.sologistics.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.sologistics.R;

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

        holder.tvRFID.setText("" + inventory.getEpc());
    }

    @Override
    public int getItemCount() {
        return inventories.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRFID;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRFID = itemView.findViewById(R.id.tvRFID);
        }
    }

    public void setOnItemClickListener(InventoryClickListener listener) {
        this.listener = listener;
    }

    public interface InventoryClickListener {
        void onClick(Inventory inventory);
    }
}
