package com.example.apsitcanteen.adapters;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.apsitcanteen.R;
import com.example.apsitcanteen.models.FoodItem;
import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<FoodItem> foodList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddClick(FoodItem foodItem);
    }

    public MenuAdapter(List<FoodItem> foodList, OnItemClickListener listener) {
        this.foodList = new ArrayList<>(foodList);
        this.listener = listener;
    }

    public void updateList(List<FoodItem> newList) {
        this.foodList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_card, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDescription.setText(item.getDescription());
        holder.tvPrice.setText("₹" + (int)item.getPrice());
        
        // Load image using Glide
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .centerCrop()
                    .into(holder.ivFood);
        } else {
            holder.ivFood.setImageResource(R.drawable.ic_food_placeholder);
        }

        // Handling availability: grey out if not available
        if (item.isAvailable()) {
            holder.itemView.setAlpha(1.0f);
            holder.ivFood.clearColorFilter();
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setText(R.string.add_to_cart);
        } else {
            holder.itemView.setAlpha(0.5f);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.ivFood.setColorFilter(filter);
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setText("Unavailable");
        }
        
        holder.btnAdd.setOnClickListener(v -> {
            if (item.isAvailable()) {
                listener.onAddClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice;
        ImageView ivFood;
        Button btnAdd;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivFood = itemView.findViewById(R.id.ivFood);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
