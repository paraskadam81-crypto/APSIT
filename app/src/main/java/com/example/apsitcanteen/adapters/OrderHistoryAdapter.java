package com.example.apsitcanteen.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.OrderStatusActivity;
import com.example.apsitcanteen.R;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.Order;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orders;

    public OrderHistoryAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvOrderId.setText("#" + order.getOrderId().substring(0, Math.min(order.getOrderId().length(), 6)));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(order.getTimestamp())));
        
        holder.tvAmount.setText("₹" + (int)order.getTotalPrice());
        holder.tvStatus.setText(order.getStatus());

        String status = order.getStatus() != null ? order.getStatus() : "Pending";
        
        // Reset animation
        holder.tvStatus.clearAnimation();

        switch (status) {
            case "Pending":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                startPulseAnimation(holder.tvStatus);
                break;
            case "Accepted":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_accepted);
                startPulseAnimation(holder.tvStatus);
                break;
            case "Preparing":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_preparing);
                startPulseAnimation(holder.tvStatus);
                break;
            case "Ready":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_ready);
                startPulseAnimation(holder.tvStatus);
                break;
            case "Completed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                break;
            case "Cancelled":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelled);
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                break;
        }

        StringBuilder itemsSummary = new StringBuilder();
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                CartItem item = order.getItems().get(i);
                itemsSummary.append(item.getQuantity()).append(" x ").append(item.getFoodItem().getName());
                if (i < order.getItems().size() - 1) itemsSummary.append(", ");
            }
        }
        holder.tvItemsSummary.setText(itemsSummary.toString());

        // Tapping on the order card opens the tracking screen
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderStatusActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            v.getContext().startActivity(intent);
        });
    }

    private void startPulseAnimation(View view) {
        Animation pulse = AnimationUtils.loadAnimation(view.getContext(), R.anim.pulse);
        if (pulse != null) view.startAnimation(pulse);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvDate, tvItemsSummary, tvAmount, tvStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvItemsSummary = itemView.findViewById(R.id.tvOrderItems);
            tvAmount = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}
