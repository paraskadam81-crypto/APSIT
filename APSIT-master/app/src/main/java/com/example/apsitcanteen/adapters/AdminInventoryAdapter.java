package com.example.apsitcanteen.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.R;
import com.example.apsitcanteen.admin.InventoryItem;
import java.util.List;

public class AdminInventoryAdapter extends RecyclerView.Adapter<AdminInventoryAdapter.InventoryViewHolder> {

    private Context context;
    private List<InventoryItem> itemList;
    private OnInventoryClickListener listener;

    public interface OnInventoryClickListener {
        void onUpdateStock(InventoryItem item, int position);
    }

    public AdminInventoryAdapter(Context context, List<InventoryItem> itemList, OnInventoryClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);

        if (position % 2 != 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F4FAF6"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.tvItemName.setText(item.getItemName());
        holder.tvCategory.setText(item.getCategory());
        
        holder.tvStock.setText(String.valueOf(item.getCurrentStock()));
        holder.tvMinStock.setText(String.valueOf(item.getMinStock()));

        if (item.getCurrentStock() < item.getMinStock()) {
            holder.tvStock.setTextColor(0xFFC0392B);
            holder.tvStatusBadge.setText("Low");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_cancelled);
        } else {
            holder.tvStock.setTextColor(0xFF2D6A4F);
            holder.tvStatusBadge.setText("OK");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed);
        }

        holder.itemView.setOnClickListener(v -> listener.onUpdateStock(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvCategory, tvStock, tvMinStock, tvStatusBadge;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvMinStock = itemView.findViewById(R.id.tvMinStock);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
