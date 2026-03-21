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
    private TextView tvOrderId, tvStatus;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("orderId");

        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvCurrentStatus);
        
        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        if (orderId != null) {
            tvOrderId.setText("Order ID: " + orderId);
            listenToOrderStatus();
        }
    }

    private void listenToOrderStatus() {
        statusListener = db.collection("orders").document(orderId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && value.exists()) {
                        Order order = value.toObject(Order.class);
                        if (order != null) {
                            updateStatusUI(order.getStatus());
                        }
                    }
                });
    }

    private void updateStatusUI(String status) {
        if (tvStatus != null) {
            tvStatus.setText("Status: " + status);
        }
        Toast.makeText(this, "Order Status: " + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) statusListener.remove();
    }
}
