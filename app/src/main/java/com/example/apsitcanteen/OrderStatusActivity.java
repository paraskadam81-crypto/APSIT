package com.example.apsitcanteen;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class OrderStatusActivity extends AppCompatActivity {

    private String orderId;
    private FirebaseFirestore db;
    private ListenerRegistration statusListener;
    
    private TextView tvOrderId, tvOrderItems, tvTotalAmount, tvEstTime;
    private ImageView dot1, dot2, dot3, dot4, dot5;
    private View line1, line2, line3, line4;
    private TextView tvStage1, tvStage2, tvStage3, tvStage4, tvStage5;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("orderId");

        // Initialize Views
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderItems = findViewById(R.id.tvOrderItems);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvEstTime = findViewById(R.id.tvEstTime);
        progressBar = findViewById(R.id.progressBar);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        dot5 = findViewById(R.id.dot5);

        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
        line4 = findViewById(R.id.line4);

        tvStage1 = findViewById(R.id.tvStage1);
        tvStage2 = findViewById(R.id.tvStage2);
        tvStage3 = findViewById(R.id.tvStage3);
        tvStage4 = findViewById(R.id.tvStage4);
        tvStage5 = findViewById(R.id.tvStage5);

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        if (orderId != null) {
            tvOrderId.setText("Order ID: #" + orderId.substring(0, Math.min(orderId.length(), 8)));
            listenToOrderStatus();
        } else {
            Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void listenToOrderStatus() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        statusListener = db.collection("orders").document(orderId)
                .addSnapshotListener((value, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null && value.exists()) {
                        Order order = value.toObject(Order.class);
                        if (order != null) {
                            updateUI(order);
                        }
                    }
                });
    }

    private void updateUI(Order order) {
        // Update Summary
        StringBuilder items = new StringBuilder();
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                CartItem item = order.getItems().get(i);
                items.append(item.getQuantity()).append(" x ").append(item.getFoodItem().getName());
                if (i < order.getItems().size() - 1) items.append(", ");
            }
        }
        tvOrderItems.setText(items.toString());
        tvTotalAmount.setText("₹" + (int)order.getTotalPrice());

        // Update Estimated Time
        int minutes = 15;
        if (order.getTotalPrice() > 500) minutes = 30;
        else if (order.getTotalPrice() > 200) minutes = 20;
        tvEstTime.setText("Estimated Preparation Time: ~" + minutes + " mins");

        // Update Timeline
        resetTimeline();
        String status = order.getStatus() != null ? order.getStatus() : "Pending";
        
        // Stages: Pending -> Accepted -> Preparing -> Ready -> Completed
        highlightStage(1, true); // Order Placed is always done if order exists
        
        if (status.equals("Accepted") || status.equals("Preparing") || status.equals("Ready") || status.equals("Completed")) {
            highlightStage(2, status.equals("Accepted"));
            line1.setBackgroundColor(getResources().getColor(R.color.successGreen));
        }
        
        if (status.equals("Preparing") || status.equals("Ready") || status.equals("Completed")) {
            highlightStage(2, true);
            highlightStage(3, status.equals("Preparing"));
            line2.setBackgroundColor(getResources().getColor(R.color.successGreen));
        }

        if (status.equals("Ready") || status.equals("Completed")) {
            highlightStage(3, true);
            highlightStage(4, status.equals("Ready"));
            line3.setBackgroundColor(getResources().getColor(R.color.successGreen));
        }

        if (status.equals("Completed")) {
            highlightStage(4, true);
            highlightStage(5, true);
            line4.setBackgroundColor(getResources().getColor(R.color.successGreen));
        }
        
        if (status.equals("Pending")) {
            highlightStage(1, false); // Pending means current is stage 1 but not "done" yet? 
            // Actually prompt says ✅ Order Placed. So stage 1 is current when pending.
            highlightStage(1, false); 
        }
    }

    private void highlightStage(int stage, boolean isCompleted) {
        ImageView dot = null;
        TextView text = null;
        
        switch (stage) {
            case 1: dot = dot1; text = tvStage1; break;
            case 2: dot = dot2; text = tvStage2; break;
            case 3: dot = dot3; text = tvStage3; break;
            case 4: dot = dot4; text = tvStage4; break;
            case 5: dot = dot5; text = tvStage5; break;
        }

        if (dot != null && text != null) {
            if (isCompleted) {
                dot.setImageResource(R.drawable.ic_check_circle);
                dot.setColorFilter(getResources().getColor(R.color.successGreen));
            } else {
                dot.setImageResource(R.drawable.circle_background);
                dot.setColorFilter(getResources().getColor(R.color.successGreen));
            }
            text.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void resetTimeline() {
        ImageView[] dots = {dot1, dot2, dot3, dot4, dot5};
        TextView[] texts = {tvStage1, tvStage2, tvStage3, tvStage4, tvStage5};
        View[] lines = {line1, line2, line3, line4};

        for (ImageView dot : dots) {
            dot.setImageResource(R.drawable.circle_background);
            dot.setColorFilter(getResources().getColor(R.color.gray_light));
        }
        for (TextView text : texts) {
            text.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            text.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        for (View line : lines) {
            line.setBackgroundColor(getResources().getColor(R.color.gray_light));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) statusListener.remove();
    }
}
