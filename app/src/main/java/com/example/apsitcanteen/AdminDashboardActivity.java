package com.example.apsitcanteen;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration ordersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        // Add fade-in animation to the root view
        View root = findViewById(R.id.scrollView);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(500);
        root.startAnimation(fadeIn);

        setupToolbar();
        loadStatsAndRecentOrders();
        setupNavigation();
    }

    private void setupToolbar() {
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadStatsAndRecentOrders() {
        ordersListener = db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    int todayOrdersCount = 0;
                    double todayRevenue = 0;
                    String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

                    Map<String, Integer> statusCounts = new HashMap<>();
                    String[] statuses = {"Pending", "Accepted", "Preparing", "Ready", "Completed"};
                    for (String s : statuses) statusCounts.put(s, 0);

                    List<Order> recentOrders = new ArrayList<>();
                    int count = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        Order order = doc.toObject(Order.class);
                        order.setOrderId(doc.getId());

                        String orderDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(order.getTimestamp()));
                        if (todayDate.equals(orderDate)) {
                            todayOrdersCount++;
                            todayRevenue += order.getTotalPrice();
                        }

                        String status = order.getStatus();
                        if (statusCounts.containsKey(status)) {
                            statusCounts.put(status, statusCounts.get(status) + 1);
                        }

                        if (count < 4) {
                            recentOrders.add(order);
                            count++;
                        }
                    }

                    updateUI(todayOrdersCount, (int)todayRevenue, statusCounts, recentOrders);
                });
    }

    private void updateUI(int todayOrders, int todayRevenue, Map<String, Integer> statusCounts, List<Order> recentOrders) {
        animateNumber((TextView) findViewById(R.id.tvTodayOrders), todayOrders);
        animateRevenue((TextView) findViewById(R.id.tvTodayRevenue), todayRevenue);
        animateNumber((TextView) findViewById(R.id.tvPendingOrders), statusCounts.get("Pending"));
        
        // Simulating low stock for demo purposes or fetch from real inventory count
        animateNumber((TextView) findViewById(R.id.tvLowStock), 3); 

        updateStatusBar(R.id.pbPending, R.id.tvPendingCount, statusCounts.get("Pending"));
        updateStatusBar(R.id.pbPreparing, R.id.tvPreparingCount, statusCounts.get("Preparing"));
        updateStatusBar(R.id.pbReady, R.id.tvReadyCount, statusCounts.get("Ready"));
        updateStatusBar(R.id.pbCompleted, R.id.tvCompletedCount, statusCounts.get("Completed"));
        
        updateRecentOrdersUI(recentOrders);
    }

    private void animateNumber(TextView textView, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> textView.setText(animation.getAnimatedValue().toString()));
        animator.start();
    }

    private void animateRevenue(TextView textView, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> textView.setText("₹" + animation.getAnimatedValue().toString()));
        animator.start();
    }

    private void updateStatusBar(int pbId, int tvId, int count) {
        ((TextView) findViewById(tvId)).setText(String.valueOf(count));
        ProgressBar pb = findViewById(pbId);
        pb.setMax(20); 
        pb.setProgress(count);
    }

    private void updateRecentOrdersUI(List<Order> recentOrders) {
        LinearLayout container = findViewById(R.id.recentOrdersContainer);
        container.removeAllViews();

        for (Order order : recentOrders) {
            View row = getLayoutInflater().inflate(R.layout.item_admin_order_summary, container, false);

            ((TextView) row.findViewById(R.id.tvOrderId)).setText("#" + order.getOrderId().substring(0, Math.min(order.getOrderId().length(), 6)));
            ((TextView) row.findViewById(R.id.tvStudentName)).setText(order.getStudentName());
            ((TextView) row.findViewById(R.id.tvAmount)).setText("₹" + (int) order.getTotalPrice());

            TextView tvStatus = row.findViewById(R.id.tvStatusBadge);
            tvStatus.setText(order.getStatus());
            setStatusBadgeStyle(tvStatus, order.getStatus());

            row.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminOrderDetailActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                startActivity(intent);
            });

            container.addView(row);
        }
    }

    private void setStatusBadgeStyle(TextView tv, String status) {
        switch (status) {
            case "Pending": tv.setBackgroundResource(R.drawable.bg_badge_pending); break;
            case "Accepted":
            case "Preparing": tv.setBackgroundResource(R.drawable.bg_badge_preparing); break;
            case "Ready": tv.setBackgroundResource(R.drawable.bg_badge_ready); break;
            case "Completed": tv.setBackgroundResource(R.drawable.bg_badge_completed); break;
            default: tv.setBackgroundResource(R.drawable.bg_badge_cancelled); break;
        }
    }

    private void setupNavigation() {
        findViewById(R.id.cardMenuManagement).setOnClickListener(v -> startActivity(new Intent(this, AdminMenuManagementActivity.class)));
        findViewById(R.id.cardOrderManagement).setOnClickListener(v -> startActivity(new Intent(this, AdminOrderManagementActivity.class)));
        findViewById(R.id.tvViewAllOrders).setOnClickListener(v -> startActivity(new Intent(this, AdminOrderManagementActivity.class)));
        findViewById(R.id.cardInventory).setOnClickListener(v -> startActivity(new Intent(this, AdminInventoryActivity.class)));
        findViewById(R.id.cardReports).setOnClickListener(v -> startActivity(new Intent(this, AdminReportsActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) ordersListener.remove();
    }
}
