package com.example.apsitcanteen;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class OrderStatusActivity extends AppCompatActivity {

    private String orderId;
    private FirebaseFirestore db;
    private ListenerRegistration statusListener;
    private TextView tvOrderId, tvStatus, tvStatusDesc;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("orderId");

        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvCurrentStatus);
        tvStatusDesc = findViewById(R.id.tvStatusDescription);
        progressBar = findViewById(R.id.progressBar);
        
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
                            updateStatusUI(order.getStatus());
                        }
                    } else {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatusUI(String status) {
        if (status == null) status = "Pending";
        
        if (tvStatus != null) {
            tvStatus.setText("Status: " + status);
            
            // Update background badge based on status
            switch (status) {
                case "Pending":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                    tvStatusDesc.setText("We have received your order and it's being reviewed.");
                    break;
                case "Accepted":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_accepted);
                    tvStatusDesc.setText("Your order has been accepted by the canteen.");
                    break;
                case "Preparing":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_preparing);
                    tvStatusDesc.setText("Chef is preparing your delicious meal!");
                    break;
                case "Ready":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_ready);
                    tvStatusDesc.setText("Your order is ready! Please pick it up from the counter.");
                    break;
                case "Completed":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                    tvStatusDesc.setText("Order has been picked up. Enjoy your meal!");
                    break;
                case "Cancelled":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelled);
                    tvStatusDesc.setText("Sorry, your order was cancelled.");
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) statusListener.remove();
    }
}
