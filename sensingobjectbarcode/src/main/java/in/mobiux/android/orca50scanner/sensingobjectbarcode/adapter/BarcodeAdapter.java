package in.mobiux.android.orca50scanner.sensingobjectbarcode.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.sensingobjectbarcode.R;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.RecyclerViewHolder> {

    Activity context;
    List<Barcode> barcodes;
    BarcodeClickListener listener;

    public BarcodeAdapter(Activity context, List<Barcode> barcodes) {
        this.context = context;
        this.barcodes = barcodes;
    }

    public void setValues(List<Barcode> values) {
        this.barcodes = values;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_barcode, parent, false);

        return new RecyclerViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        Barcode barcode = barcodes.get(position);

        holder.tvName.setText("" + barcode.getName());
        

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick(barcodes.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return barcodes.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRssi, tvName;
        private ImageView ivStatus;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
        }
    }

    public void setOnItemClickListener(BarcodeClickListener listener) {
        this.listener = listener;
    }

    public interface BarcodeClickListener {
        void onClick(Barcode barcode);
    }
}
