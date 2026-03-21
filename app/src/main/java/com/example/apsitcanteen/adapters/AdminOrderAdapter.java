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
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.Order;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        int statusColor;
        int badgeBg;
        String status = order.getStatus() != null ? order.getStatus() : "Pending";
        
        switch (status) {
            case "Pending": 
                statusColor = 0xFFF0C040; 
                badgeBg = R.drawable.bg_badge_pending;
                break;
            case "Accepted":
            case "Preparing": 
                statusColor = 0xFF378ADD; 
                badgeBg = R.drawable.bg_badge_preparing;
                break;
            case "Ready": 
                statusColor = 0xFF2D6A4F; 
                badgeBg = R.drawable.bg_badge_ready;
                break;
            case "Completed": 
                statusColor = 0xFF1B4332; 
                badgeBg = R.drawable.bg_badge_completed;
                break;
            default: 
                statusColor = Color.GRAY;
                badgeBg = R.drawable.bg_badge_pending;
        }

        holder.viewAccent.setBackgroundColor(statusColor);
        holder.tvOrderId.setText("#" + order.getOrderId().substring(0, Math.min(order.getOrderId().length(), 6)));
        holder.tvStudentInfo.setText(order.getStudentName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        holder.tvDateTime.setText(sdf.format(new Date(order.getTimestamp())));
        
        StringBuilder summary = new StringBuilder();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                if (summary.length() > 0) summary.append(", ");
                summary.append(item.getFoodItem().getName()).append(" x").append(item.getQuantity());
            }
        }
        holder.tvItemsSummary.setText(summary.toString());
        holder.tvAmount.setText("₹" + (int)order.getTotalPrice());
        
        holder.tvStatusBadge.setText(status);
        holder.tvStatusBadge.setBackgroundResource(badgeBg);

        holder.btnView.setOnClickListener(v -> listener.onOrderClick(order));
        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        View viewAccent;
        TextView tvOrderId, tvStudentInfo, tvDateTime, tvItemsSummary, tvAmount, tvStatusBadge;
        View btnView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAccent = itemView.findViewById(R.id.viewAccent);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStudentInfo = itemView.findViewById(R.id.tvStudentInfo);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvItemsSummary = itemView.findViewById(R.id.tvItemsSummary);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
