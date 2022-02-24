package in.mobiux.android.orca50scanner.stocklitev2.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.stocklitev2.R;
import in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.RecyclerViewHolder> {

    Activity context;
    List<Inventory> inventories;
    InventoryClickListener listener;
    private final RFIDUtils rfidUtils;

    public InventoryAdapter(Activity context, List<Inventory> inventories) {
        this.context = context;
        this.inventories = inventories;
        rfidUtils = RFIDUtils.getInstance(context);
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

        String name = inventory.getFormattedEPC();

        RFIDUtils.DisplayRule dpRule = rfidUtils.getDisplayRule();

        if (dpRule == RFIDUtils.DisplayRule.D1) {

        } else if (dpRule == RFIDUtils.DisplayRule.D2) {
            name = name.substring(name.length() - 9, name.length() - 1);
        } else if (dpRule == RFIDUtils.DisplayRule.D21) {
            name = name.substring(name.length() - 5, name.length() - 1);
        } else if (dpRule == RFIDUtils.DisplayRule.D3) {
            name = name.substring(name.length() - 5, name.length() - 1);
        }

        holder.tvRFID.setText("" + rfidDisplayName(name));
        holder.tvRSSI.setText("" + inventory.getRssi() + "dbm" + "\n\t(" + inventory.getQuantity() + ")");
        holder.tvName.setText("" + inventory.getName());
    }

    @Override
    public int getItemCount() {
        return inventories.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRFID, tvRSSI, tvName;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRFID = itemView.findViewById(R.id.tvRFID);
            tvRSSI = itemView.findViewById(R.id.tvRSSI);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }

    public void setOnItemClickListener(InventoryClickListener listener) {
        this.listener = listener;
    }

    public interface InventoryClickListener {
        void onClick(Inventory inventory);
    }

    public String rfidDisplayName(String rfid) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rfid.length(); i++) {
            if (i % 2 == 0) {
                sb.append(" ");
            }
            sb.append(rfid.charAt(i));
        }

        return sb.toString();
    }
}

