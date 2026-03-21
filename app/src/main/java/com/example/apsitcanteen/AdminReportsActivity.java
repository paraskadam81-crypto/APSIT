package com.example.apsitcanteen;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.Order;
import com.example.apsitcanteen.views.BarChartView;
import com.example.apsitcanteen.views.HorizontalBarChartView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminReportsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private BarChartView barChart;
    private HorizontalBarChartView horizontalChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        barChart = findViewById(R.id.revenueBarChart);
        horizontalChart = findViewById(R.id.topItemsChart);

        findViewById(R.id.btnExport).setOnClickListener(v ->
                Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show());

        loadReportsFromFirestore();
    }

    private void loadReportsFromFirestore() {
        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        Order order = doc.toObject(Order.class);
                        order.setOrderId(doc.getId());
                        orders.add(order);
                    }

                    displaySummary(orders);
                    setupRevenueChart(orders);
                    setupStatusBreakdown(orders);
                    setupTopItemsChart(orders);
                    displayRecentTransactions(orders);
                });
    }

    private void displaySummary(List<Order> orders) {
        double totalRevenue = 0;
        for (Order order : orders) {
            if ("Completed".equalsIgnoreCase(order.getStatus())) {
                totalRevenue += order.getTotalPrice();
            }
        }

        int totalOrders = orders.size();
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        Map<String, Integer> itemSales = getItemSalesMap(orders);
        String topItem = "N/A";
        int maxSales = -1;
        for (Map.Entry<String, Integer> entry : itemSales.entrySet()) {
            if (entry.getValue() > maxSales) {
                maxSales = entry.getValue();
                topItem = entry.getKey();
            }
        }

        ((TextView) findViewById(R.id.tvTotalRevenue)).setText("₹" + (int) totalRevenue);
        ((TextView) findViewById(R.id.tvTotalOrdersCount)).setText(String.valueOf(totalOrders));
        ((TextView) findViewById(R.id.tvAvgOrderValue)).setText("₹" + (int) avgOrderValue);
        ((TextView) findViewById(R.id.tvTopSellingItem)).setText(topItem);
    }

    private void setupRevenueChart(List<Order> orders) {
        LinkedHashMap<String, Double> dailyRevenue = new LinkedHashMap<>();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) dailyRevenue.put(day, 0.0);

        Calendar cal = Calendar.getInstance();
        for (Order order : orders) {
            if (!"Completed".equalsIgnoreCase(order.getStatus())) continue;
            cal.setTimeInMillis(order.getTimestamp());
            String day = days[cal.get(Calendar.DAY_OF_WEEK) - 1];
            dailyRevenue.put(day, dailyRevenue.get(day) + order.getTotalPrice());
        }

        if (barChart != null) barChart.setData(dailyRevenue);
    }

    private void setupStatusBreakdown(List<Order> orders) {
        Map<String, Integer> counts = new HashMap<>();
        String[] statuses = {"Pending", "Accepted", "Preparing", "Ready", "Completed"};
        for (String s : statuses) counts.put(s, 0);
        counts.put("Cancelled", 0);

        for (Order order : orders) {
            String status = order.getStatus();
            if (status != null && counts.containsKey(status)) {
                counts.put(status, counts.get(status) + 1);
            }
        }

        int total = orders.size();
        if (total > 0) {
            setSegmentWeight(R.id.viewPending, counts.get("Pending"), total);
            setSegmentWeight(R.id.viewPreparing, counts.get("Preparing") + counts.get("Accepted"), total);
            setSegmentWeight(R.id.viewReady, counts.get("Ready"), total);
            setSegmentWeight(R.id.viewCompleted, counts.get("Completed"), total);
            setSegmentWeight(R.id.viewCancelled, counts.get("Cancelled"), total);
        }

        updateStatusLabel(R.id.tvPendingLabel, "Pending", counts.get("Pending"));
        updateStatusLabel(R.id.tvPreparingLabel, "Preparing", counts.get("Preparing") + counts.get("Accepted"));
        updateStatusLabel(R.id.tvReadyLabel, "Ready", counts.get("Ready"));
        updateStatusLabel(R.id.tvCompletedLabel, "Completed", counts.get("Completed"));
        updateStatusLabel(R.id.tvCancelledLabel, "Cancelled", counts.get("Cancelled"));
    }

    private void updateStatusLabel(int id, String label, int count) {
        View v = findViewById(id);
        if (v instanceof TextView) {
            ((TextView) v).setText(label + " (" + count + ")");
        }
    }

    private void setupTopItemsChart(List<Order> orders) {
        Map<String, Integer> itemSales = getItemSalesMap(orders);
        LinkedHashMap<String, Integer> top5 = new LinkedHashMap<>();
        itemSales.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .forEach(e -> top5.put(e.getKey(), e.getValue()));

        if (horizontalChart != null) horizontalChart.setData(top5);
    }

    private Map<String, Integer> getItemSalesMap(List<Order> orders) {
        Map<String, Integer> itemSales = new HashMap<>();
        for (Order order : orders) {
            if (order.getItems() != null) {
                for (CartItem item : order.getItems()) {
                    String name = item.getFoodItem().getName();
                    itemSales.put(name, itemSales.getOrDefault(name, 0) + item.getQuantity());
                }
            }
        }
        return itemSales;
    }

    private void displayRecentTransactions(List<Order> orders) {
        LinearLayout container = findViewById(R.id.transactionsContainer);
        if (container == null) return;
        container.removeAllViews();

        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        for (int i = 0; i < orders.size() && count < 7; i++, count++) {
            Order order = orders.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.item_transaction_row, container, false);

            if (count % 2 != 0) row.setBackgroundColor(Color.parseColor("#F4FAF6"));

            ((TextView) row.findViewById(R.id.tvId)).setText("#" + order.getOrderId().substring(0, Math.min(order.getOrderId().length(), 4)));
            ((TextView) row.findViewById(R.id.tvName)).setText(order.getStudentName());
            ((TextView) row.findViewById(R.id.tvAmount)).setText("₹" + (int) order.getTotalPrice());

            TextView tvStatus = row.findViewById(R.id.tvStatus);
            tvStatus.setText(order.getStatus());
            setStatusBadge(tvStatus, order.getStatus());

            container.addView(row);
        }
    }

    private void setSegmentWeight(int viewId, int count, int total) {
        View v = findViewById(viewId);
        if (v == null) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.weight = (float) count / total;
        v.setLayoutParams(params);
        v.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private void setStatusBadge(TextView tv, String status) {
        if (status == null) return;
        switch (status) {
            case "Pending": tv.setBackgroundResource(R.drawable.bg_badge_pending); break;
            case "Accepted":
            case "Preparing": tv.setBackgroundResource(R.drawable.bg_badge_preparing); break;
            case "Ready": tv.setBackgroundResource(R.drawable.bg_badge_ready); break;
            case "Completed": tv.setBackgroundResource(R.drawable.bg_badge_completed); break;
            default: tv.setBackgroundResource(R.drawable.bg_badge_cancelled); break;
        }
    }
}
