package com.example.apsitcanteen;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
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

        setupToolbar();
        setupNavigation();
        loadStatsAndRecentOrders();
    }

    private void setupToolbar() {
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
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

                    int totalOrders = 0;
                    double totalRevenue = 0;
                    String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

                    Map<String, Integer> statusCounts = new HashMap<>();
                    String[] statuses = {"Pending", "Accepted", "Preparing", "Ready", "Completed", "Cancelled"};
                    for (String s : statuses) statusCounts.put(s, 0);

                    List<Order> recentOrders = new ArrayList<>();
                    int count = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        Order order = doc.toObject(Order.class);
                        order.setOrderId(doc.getId());

                        totalOrders++;
                        totalRevenue += order.getTotalPrice();

                        String status = order.getStatus();
                        if (statusCounts.containsKey(status)) {
                            statusCounts.put(status, statusCounts.get(status) + 1);
                        }

                        if (count < 4) {
                            recentOrders.add(order);
                            count++;
                        }
                    }

                    updateUI(totalOrders, (int)totalRevenue, statusCounts, recentOrders);
                });
    }

    private void updateUI(int totalOrders, int totalRevenue, Map<String, Integer> statusCounts, List<Order> recentOrders) {
        // Animate main cards
        animateNumber((TextView) findViewById(R.id.tvTodayOrders), totalOrders);
        animateRevenue((TextView) findViewById(R.id.tvTodayRevenue), totalRevenue);
        
        // Bounce pop for main cards
        applyAnimation(findViewById(R.id.cardTotalOrders), R.anim.bounce_pop, 0);
        applyAnimation(findViewById(R.id.cardTotalRevenue), R.anim.bounce_pop, 200);

        // Low Stock
        db.collection("menu").whereLessThan("stock", 5).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int lowStockCount = queryDocumentSnapshots.size();
            TextView tvLowStock = findViewById(R.id.tvLowStock);
            animateNumber(tvLowStock, lowStockCount);
            
            View cardLowStock = findViewById(R.id.cardLowStock);
            applyAnimation(cardLowStock, R.anim.bounce_pop, 400);
            if (lowStockCount > 0) {
                cardLowStock.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
            }
        });

        // Status Cards with alternating slide-in
        updateStatusCard(R.id.cardStatusPending, R.id.tvPendingCount, statusCounts.get("Pending"), R.anim.slide_in_left, 100);
        updateStatusCard(R.id.cardStatusAccepted, R.id.tvAcceptedCount, statusCounts.get("Accepted"), R.anim.slide_in_right, 200);
        updateStatusCard(R.id.cardStatusPreparing, R.id.tvPreparingCount, statusCounts.get("Preparing"), R.anim.slide_in_left, 300);
        updateStatusCard(R.id.cardStatusReady, R.id.tvReadyCount, statusCounts.get("Ready"), R.anim.slide_in_right, 400);
        updateStatusCard(R.id.cardStatusCompleted, R.id.tvCompletedCount, statusCounts.get("Completed"), R.anim.slide_in_left, 500);
        updateStatusCard(R.id.cardStatusCancelled, R.id.tvCancelledCount, statusCounts.get("Cancelled"), R.anim.slide_in_right, 600);
        
        updateRecentOrdersUI(recentOrders);
    }

    private void updateStatusCard(int cardId, int tvId, int count, int animId, int delay) {
        View card = findViewById(cardId);
        TextView tv = findViewById(tvId);
        animateNumber(tv, count);
        applyAnimation(card, animId, delay);
    }

    private void applyAnimation(View view, int animId, int delay) {
        Animation anim = AnimationUtils.loadAnimation(this, animId);
        anim.setStartOffset(delay);
        view.startAnimation(anim);
    }

    private void animateNumber(TextView textView, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
        animator.setDuration(1500);
        animator.addUpdateListener(animation -> textView.setText(animation.getAnimatedValue().toString()));
        animator.start();
    }

    private void animateRevenue(TextView textView, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
        animator.setDuration(1500);
        animator.addUpdateListener(animation -> textView.setText("₹" + animation.getAnimatedValue().toString()));
        animator.start();
    }

    private void updateRecentOrdersUI(List<Order> recentOrders) {
        LinearLayout container = findViewById(R.id.recentOrdersContainer);
        container.removeAllViews();

        for (int i = 0; i < recentOrders.size(); i++) {
            Order order = recentOrders.get(i);
            View row = getLayoutInflater().inflate(R.layout.item_admin_order_summary, container, false);

            ((TextView) row.findViewById(R.id.tvOrderId)).setText("#" + order.getOrderId().substring(0, Math.min(order.getOrderId().length(), 6)));
            ((TextView) row.findViewById(R.id.tvStudentName)).setText(order.getStudentName());
            ((TextView) row.findViewById(R.id.tvAmount)).setText("₹" + (int) order.getTotalPrice());

            TextView tvStatus = row.findViewById(R.id.tvStatusBadge);
            tvStatus.setText(order.getStatus());
            tvStatus.setTextColor(Color.WHITE);
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);

            switch (order.getStatus()) {
                case "Pending": tvStatus.setBackgroundResource(R.drawable.bg_badge_pending); break;
                case "Accepted": tvStatus.setBackgroundResource(R.drawable.bg_badge_accepted); break;
                case "Preparing": tvStatus.setBackgroundResource(R.drawable.bg_badge_preparing); break;
                case "Ready": tvStatus.setBackgroundResource(R.drawable.bg_badge_ready); break;
                case "Completed": tvStatus.setBackgroundResource(R.drawable.bg_badge_completed); break;
                case "Cancelled": tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelled); break;
            }
            
            container.addView(row);
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
