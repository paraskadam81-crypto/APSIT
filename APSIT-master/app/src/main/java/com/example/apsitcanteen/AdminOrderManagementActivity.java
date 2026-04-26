package com.example.apsitcanteen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.adapters.AdminOrderAdapter;
import com.example.apsitcanteen.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderManagementActivity extends AppCompatActivity {

    private List<Order> masterList = new ArrayList<>();
    private String currentStatusFilter = "All";
    private String searchQuery = "";

    private FirebaseFirestore db;
    private ListenerRegistration ordersListener;
    private ProgressBar progressBar;
    private LinearLayout ordersContainer;

    private TextView tvCountPending, tvCountPreparing, tvCountReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_management);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);
        ordersContainer = findViewById(R.id.ordersContainer);

        tvCountPending = findViewById(R.id.tvCountPending);
        tvCountPreparing = findViewById(R.id.tvCountPreparing);
        tvCountReady = findViewById(R.id.tvCountReady);

        tvCountPending.setOnClickListener(v -> selectTab("Pending"));
        tvCountPreparing.setOnClickListener(v -> selectTab("Preparing"));
        tvCountReady.setOnClickListener(v -> selectTab("Ready"));

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupStatusFilters();
        listenToOrders();
    }

    private void selectTab(String status) {
        currentStatusFilter = status;
        applyFilters();
        updateSummaryPills();
        
        // Update the bottom chip filters to match (if applicable)
        LinearLayout filterContainer = findViewById(R.id.filterContainer);
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            TextView c = (TextView) filterContainer.getChildAt(i);
            updateChipStyle(c, c.getText().toString().equalsIgnoreCase(currentStatusFilter));
        }
    }

    private void listenToOrders() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        ordersListener = db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    masterList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId());
                            masterList.add(order);
                        }
                    }
                    applyFilters();
                    updateSummaryPills();
                });
    }

    private void setupStatusFilters() {
        String[] statuses = {"All", "Pending", "Accepted", "Preparing", "Ready", "Completed", "Cancelled"};
        LinearLayout filterContainer = findViewById(R.id.filterContainer);

        for (String status : statuses) {
            TextView chip = (TextView) getLayoutInflater()
                    .inflate(R.layout.layout_category_chip, filterContainer, false);
            chip.setText(status);
            updateChipStyle(chip, status.equals(currentStatusFilter));

            chip.setOnClickListener(v -> {
                currentStatusFilter = status;
                for (int i = 0; i < filterContainer.getChildCount(); i++) {
                    TextView c = (TextView) filterContainer.getChildAt(i);
                    updateChipStyle(c, c.getText().toString().equals(currentStatusFilter));
                }
                applyFilters();
                updateSummaryPills();
            });
            filterContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_fab_gold);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundResource(R.drawable.bg_status_inactive);
            chip.setTextColor(Color.parseColor("#757575"));
        }
    }

    private void updateSummaryPills() {
        int pending = 0;
        int preparing = 0;
        int ready = 0;

        for (Order order : masterList) {
            if ("Pending".equalsIgnoreCase(order.getStatus())) pending++;
            else if ("Preparing".equalsIgnoreCase(order.getStatus())) preparing++;
            else if ("Ready".equalsIgnoreCase(order.getStatus())) ready++;
        }

        updateTabStyle(tvCountPending, "Pending", pending);
        updateTabStyle(tvCountPreparing, "Preparing", preparing);
        updateTabStyle(tvCountReady, "Ready", ready);
    }

    private void updateTabStyle(TextView tv, String status, int count) {
        tv.setText(status + " (" + count + ")");
        boolean isSelected = status.equalsIgnoreCase(currentStatusFilter);
        
        if (isSelected) {
            tv.setTextColor(Color.WHITE);
            switch (status) {
                case "Pending": tv.setBackgroundResource(R.drawable.bg_badge_pending); break;
                case "Preparing": tv.setBackgroundResource(R.drawable.bg_badge_preparing); break;
                case "Ready": tv.setBackgroundResource(R.drawable.bg_badge_ready); break;
            }
        } else {
            tv.setTextColor(Color.parseColor("#333333"));
            tv.setBackgroundResource(R.drawable.bg_tab_unselected);
        }
    }

    private void applyFilters() {
        if (ordersContainer == null) return;
        ordersContainer.removeAllViews();
        
        String[] statuses;
        if (currentStatusFilter.equals("All")) {
            statuses = new String[]{"Pending", "Accepted", "Preparing", "Ready", "Completed", "Cancelled"};
        } else {
            statuses = new String[]{currentStatusFilter};
        }

        for (String status : statuses) {
            List<Order> statusOrders = new ArrayList<>();
            for (Order order : masterList) {
                String orderStatus = order.getStatus() != null ? order.getStatus() : "Pending";
                if (orderStatus.equalsIgnoreCase(status)) {
                    boolean matchesSearch = order.getOrderId().toLowerCase().contains(searchQuery) ||
                            (order.getStudentName() != null && order.getStudentName().toLowerCase().contains(searchQuery));
                    if (matchesSearch) {
                        statusOrders.add(order);
                    }
                }
            }
            addStatusSection(status, statusOrders);
        }
    }

    private void addStatusSection(String status, List<Order> orders) {
        View headerView = getLayoutInflater().inflate(R.layout.layout_admin_status_header, ordersContainer, false);
        TextView tvTitle = headerView.findViewById(R.id.tvStatusTitle);
        TextView tvCount = headerView.findViewById(R.id.tvStatusCount);
        
        tvTitle.setText(status.toUpperCase());
        tvCount.setText(String.valueOf(orders.size()));
        
        int color;
        switch (status) {
            case "Pending": color = 0xFFF39C12; break; // Orange
            case "Accepted": color = 0xFF3498DB; break; // Blue
            case "Preparing": color = 0xFF9B59B6; break; // Purple
            case "Ready": color = 0xFF27AE60; break; // Green
            case "Completed": color = 0xFF1B4332; break; // Dark Green
            case "Cancelled": color = 0xFFE74C3C; break; // Red
            default: color = 0xFF7F8C8D;
        }
        headerView.setBackgroundColor(color);
        ordersContainer.addView(headerView);
        
        if (orders.isEmpty()) {
            TextView tvNoOrders = new TextView(this);
            tvNoOrders.setText("No " + status.toLowerCase() + " orders");
            tvNoOrders.setPadding(48, 32, 48, 32);
            tvNoOrders.setTextColor(Color.GRAY);
            tvNoOrders.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvNoOrders.setGravity(android.view.Gravity.CENTER);
            ordersContainer.addView(tvNoOrders);
        } else {
            AdminOrderAdapter adapter = new AdminOrderAdapter(this, orders, order -> {
                Intent intent = new Intent(this, AdminOrderDetailActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                startActivity(intent);
            });
            
            RecyclerView rv = new RecyclerView(this);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(adapter);
            rv.setNestedScrollingEnabled(false);
            ordersContainer.addView(rv);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) ordersListener.remove();
    }
}
