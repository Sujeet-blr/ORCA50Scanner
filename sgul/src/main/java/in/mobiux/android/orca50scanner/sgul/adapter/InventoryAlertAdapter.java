package in.mobiux.android.orca50scanner.sgul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.sgul.R;
import in.mobiux.android.orca50scanner.sgul.api.model.Inventory;

public class InventoryAlertAdapter extends RecyclerView.Adapter<InventoryAlertAdapter.RecyclerViewHolder> {

    private Context context;
    private List<Inventory> list = new ArrayList<>();
    private OnItemClickListener clickListener;
    public int checkCount = 0;

    public InventoryAlertAdapter(Context context, List<Inventory> list) {
        this.context = context;
        this.list = list;
        this.checkCount = 0;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_with_checkbox, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull InventoryAlertAdapter.RecyclerViewHolder holder, int position) {

        Inventory inventory = list.get(position);
        holder.tvName.setText(inventory.getName());
        holder.tvBarcode.setText(inventory.getModel());
        holder.tvRfidLabel.setText(inventory.getRfidLabelName());
        holder.tvRFID.setText(inventory.getFormattedEPC());
        holder.tvLab.setText(inventory.getLaboratoryName());
        holder.checkBox.setChecked(inventory.isChecked());

        if (inventory.isChecked()) {
            checkCount++;
        } else {
            checkCount--;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkBox.setChecked(!inventory.isChecked());

                if (clickListener != null) {
                    clickListener.onItemClick(position, inventory);
                }
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                inventory.setChecked(b);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName, tvBarcode, tvRfidLabel, tvRFID, tvLab;
        private CheckBox checkBox;

        public RecyclerViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvRfidLabel = itemView.findViewById(R.id.tvRfidLabel);
            tvRFID = itemView.findViewById(R.id.tvRFID);
            checkBox = itemView.findViewById(R.id.checkbox);
            tvLab = itemView.findViewById(R.id.tvLab);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Inventory a);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }
}
