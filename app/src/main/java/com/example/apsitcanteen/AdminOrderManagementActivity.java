package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private List<Order> masterList = new ArrayList<>();
    private List<Order> filteredList = new ArrayList<>();
    private String currentStatusFilter = "All";
    private String searchQuery = "";

    private FirebaseFirestore db;
    private ListenerRegistration ordersListener;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_management);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.rvOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this, filteredList, order -> {
            Intent intent = new Intent(this, AdminOrderDetailActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

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
        String[] statuses = {"All", "Pending", "Accepted", "Preparing", "Ready", "Completed"};
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
            });
            filterContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_fab_gold);
            chip.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_status_inactive);
            chip.setTextColor(getResources().getColor(R.color.colorTextSecondary));
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

        View tvPending = findViewById(R.id.tvCountPending);
        if (tvPending instanceof TextView) ((TextView) tvPending).setText(pending + " Pending");
        
        View tvPreparing = findViewById(R.id.tvCountPreparing);
        if (tvPreparing instanceof TextView) ((TextView) tvPreparing).setText(preparing + " Preparing");
        
        View tvReady = findViewById(R.id.tvCountReady);
        if (tvReady instanceof TextView) ((TextView) tvReady).setText(ready + " Ready");
    }

    private void applyFilters() {
        filteredList.clear();
        for (Order order : masterList) {
            boolean matchesStatus = currentStatusFilter.equals("All") ||
                    order.getStatus().equalsIgnoreCase(currentStatusFilter);
            boolean matchesSearch = order.getOrderId().toLowerCase().contains(searchQuery) ||
                    order.getStudentName().toLowerCase().contains(searchQuery);
            if (matchesStatus && matchesSearch) {
                filteredList.add(order);
            }
        }
        adapter.notifyDataSetChanged();
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) ordersListener.remove();
    }
}
