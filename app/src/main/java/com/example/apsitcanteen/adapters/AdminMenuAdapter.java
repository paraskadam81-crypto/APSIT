package com.example.apsitcanteen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.R;
import com.example.apsitcanteen.models.FoodItem;
import java.util.List;

public class AdminMenuAdapter extends RecyclerView.Adapter<AdminMenuAdapter.MenuViewHolder> {

    private Context context;
    private List<FoodItem> itemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(FoodItem item, int position);
        void onDeleteClick(FoodItem item, int position);
        void onStatusToggle(FoodItem item, boolean isAvailable);
    }

    public AdminMenuAdapter(Context context, List<FoodItem> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        FoodItem item = itemList.get(position);

        int accentColor = 0xFFD4A017;
        if (item.getCategory() != null) {
            switch (item.getCategory()) {
                case "Snacks": accentColor = 0xFFD4A017; break;
                case "Meals": accentColor = 0xFF2D6A4F; break;
                case "Beverages": accentColor = 0xFF378ADD; break;
                case "Desserts": accentColor = 0xFFC0392B; break;
            }
        }
        
        if (holder.viewAccent != null) holder.viewAccent.setBackgroundColor(accentColor);

        holder.tvName.setText(item.getName());
        holder.tvCategoryBadge.setText(item.getCategory());
        holder.tvCategoryBadge.setBackgroundColor(accentColor);
        holder.tvPrice.setText("₹" + (int)item.getPrice());
        
        if (holder.tvStock != null) holder.tvStock.setText(item.isAvailable() ? "Available" : "Unavailable");

        holder.swAvailable.setOnCheckedChangeListener(null);
        holder.swAvailable.setChecked(item.isAvailable());
        holder.swAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setAvailable(isChecked);
            listener.onStatusToggle(item, isChecked);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(item, holder.getAdapterPosition()));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        View viewAccent;
        TextView tvName, tvCategoryBadge, tvPrice, tvStock;
        Switch swAvailable;
        View btnEdit, btnDelete;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAccent = itemView.findViewById(R.id.viewAccent);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            swAvailable = itemView.findViewById(R.id.swAvailable);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
