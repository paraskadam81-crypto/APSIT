package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.Order;
import com.example.apsitcanteen.models.User;
import com.example.apsitcanteen.utils.CartManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;

public class OrderConfirmationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        setupItemsList();

        double total = CartManager.getInstance().getTotalPrice();
        ((TextView) findViewById(R.id.tvTotalAmount)).setText(
                getString(R.string.currency_format, (int) total));

        placeOrder(total);

        findViewById(R.id.btnTrackOrder).setOnClickListener(v -> {
            if (orderId != null) {
                Intent intent = new Intent(OrderConfirmationActivity.this, OrderStatusActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnBackToMenu).setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
            Intent intent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void placeOrder(double total) {
        if (mAuth.getCurrentUser() == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        List<CartItem> items = CartManager.getInstance().getCartItems();
                        Order order = new Order(null, userId, user.getName(), items, total, "Pending", System.currentTimeMillis());

                        db.collection("orders").add(order)
                                .addOnSuccessListener(documentReference -> {
                                    progressBar.setVisibility(View.GONE);
                                    orderId = documentReference.getId();
                                    ((TextView) findViewById(R.id.tvOrderId)).setText("#" + orderId);
                                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                    
                                    // Deduct ordered items from inventory
                                    deductInventory(items);
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    /**
     * Deducts the ordered quantities from the 'inventory' collection.
     * Uses a WriteBatch for atomicity and FieldValue.increment for safety.
     */
    private void deductInventory(List<CartItem> items) {
        if (items == null || items.isEmpty()) return;

        WriteBatch batch = db.batch();
        List<Task<QuerySnapshot>> queryTasks = new ArrayList<>();
        List<CartItem> validItems = new ArrayList<>();

        // 1. Create a query task for each item to find its corresponding inventory document by name
        for (CartItem item : items) {
            if (item.getFoodItem() != null && item.getFoodItem().getName() != null) {
                validItems.add(item);
                queryTasks.add(db.collection("inventory")
                        .whereEqualTo("itemName", item.getFoodItem().getName())
                        .get());
            }
        }

        if (queryTasks.isEmpty()) return;

        // 2. Wait for all document lookups to complete
        Tasks.whenAllComplete(queryTasks).addOnCompleteListener(allTasks -> {
            boolean hasUpdates = false;
            for (int i = 0; i < queryTasks.size(); i++) {
                Task<QuerySnapshot> task = queryTasks.get(i);
                CartItem cartItem = validItems.get(i);

                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Match found: get the first matching inventory document
                    DocumentSnapshot inventoryDoc = task.getResult().getDocuments().get(0);
                    int quantityToDeduct = cartItem.getQuantity();

                    // Atomically deduct the quantity.
                    batch.update(inventoryDoc.getReference(), "currentStock", FieldValue.increment(-quantityToDeduct));
                    hasUpdates = true;
                } else {
                    Log.w("InventoryDeduction", "Inventory item not found for: " + 
                            (cartItem.getFoodItem() != null ? cartItem.getFoodItem().getName() : "Unknown"));
                }
            }

            // 3. Commit the batch if there are updates
            if (hasUpdates) {
                batch.commit().addOnFailureListener(e -> {
                    Log.e("InventoryDeduction", "Failed to commit inventory batch updates", e);
                });
            }
        });
    }

    private void setupItemsList() {
        LinearLayout layoutItems = findViewById(R.id.layoutItemsSummary);
        for (CartItem item : CartManager.getInstance().getCartItems()) {
            TextView tv = new TextView(this);
            tv.setText(item.getQuantity() + " x " + item.getFoodItem().getName());
            tv.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            tv.setPadding(0, 4, 0, 4);
            layoutItems.addView(tv);
        }
    }
}
