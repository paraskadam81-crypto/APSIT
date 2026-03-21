package com.example.apsitcanteen;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.Order;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private Order order;
    private String orderId;
    private FirebaseFirestore db;

    private TextView tvStatusBadge;
    private LinearLayout statusButtonContainer;
    private View rootLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        db = FirebaseFirestore.getInstance();
        rootLayout = findViewById(R.id.rootLayout);
        progressBar = findViewById(R.id.progressBar);
        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null) {
            finish();
            return;
        }

        loadOrderDetails();
    }

    private void loadOrderDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        db.collection("orders").document(orderId).addSnapshotListener((value, error) -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (error != null || value == null || !value.exists()) return;

            order = value.toObject(Order.class);
            order.setOrderId(value.getId());

            setupToolbar();
            fillStudentInfo();
            fillItemsTable();
            setupStatusUpdateSection();
        });
    }

    private void setupToolbar() {
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        tvTitle.setText("Order #" + orderId.substring(0, Math.min(orderId.length(), 6)));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void fillStudentInfo() {
        ((TextView) findViewById(R.id.tvStudentName)).setText(order.getStudentName());
        ((TextView) findViewById(R.id.tvStudentId)).setText(order.getStudentId());
        ((TextView) findViewById(R.id.tvPaymentMode)).setText("Online/Cash");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        ((TextView) findViewById(R.id.tvOrderDateTime)).setText(sdf.format(new Date(order.getTimestamp())));
    }

    private void fillItemsTable() {
        LinearLayout tableContainer = findViewById(R.id.itemsTableContainer);
        tableContainer.removeAllViews();

        int rowCount = 0;
        for (CartItem item : order.getItems()) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_order_detail_row, tableContainer, false);
            if (rowCount % 2 != 0) {
                row.setBackgroundColor(Color.parseColor("#F4FAF6"));
            }
            
            ((TextView) row.findViewById(R.id.tvItemName)).setText(item.getFoodItem().getName());
            ((TextView) row.findViewById(R.id.tvItemQty)).setText(String.valueOf(item.getQuantity()));
            ((TextView) row.findViewById(R.id.tvItemPrice)).setText("₹" + (int)item.getTotalPrice());

            tableContainer.addView(row);
            rowCount++;
        }

        ((TextView) findViewById(R.id.tvTotalAmount)).setText("Total: ₹" + (int)order.getTotalPrice());
    }

    private void setupStatusUpdateSection() {
        tvStatusBadge = findViewById(R.id.tvCurrentStatusBadge);
        statusButtonContainer = findViewById(R.id.statusButtonContainer);
        refreshStatusUI();
    }

    private void refreshStatusUI() {
        tvStatusBadge.setText(order.getStatus());
        setStatusBadgeBackground(tvStatusBadge, order.getStatus());

        String[] statuses = {"Pending", "Accepted", "Preparing", "Ready", "Completed"};
        statusButtonContainer.removeAllViews();

        for (String status : statuses) {
            TextView btn = (TextView) getLayoutInflater().inflate(R.layout.layout_status_toggle_button, statusButtonContainer, false);
            btn.setText(status);
            
            if (status.equalsIgnoreCase(order.getStatus())) {
                btn.setBackgroundResource(R.drawable.bg_status_active);
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundResource(R.drawable.bg_status_inactive);
                btn.setTextColor(Color.parseColor("#5A5A5A"));
            }

            btn.setOnClickListener(v -> updateOrderStatus(status));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            params.setMargins(4, 0, 4, 0);
            btn.setLayoutParams(params);
            statusButtonContainer.addView(btn);
        }
    }

    private void updateOrderStatus(String status) {
        db.collection("orders").document(orderId).update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(rootLayout, "Status updated to " + status, Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    private void setStatusBadgeBackground(TextView tv, String status) {
        switch (status) {
            case "Pending": tv.setBackgroundResource(R.drawable.bg_badge_pending); break;
            case "Accepted": tv.setBackgroundResource(R.drawable.bg_badge_preparing); break;
            case "Preparing": tv.setBackgroundResource(R.drawable.bg_badge_preparing); break;
            case "Ready": tv.setBackgroundResource(R.drawable.bg_badge_ready); break;
            case "Completed": tv.setBackgroundResource(R.drawable.bg_badge_completed); break;
            default: tv.setBackgroundResource(R.drawable.bg_badge_cancelled); break;
        }
    }
}
