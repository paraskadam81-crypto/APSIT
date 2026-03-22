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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            if (orderId != null) {
                CartManager.getInstance().clearCart();
            }
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
                        List<CartItem> items = new ArrayList<>(CartManager.getInstance().getCartItems());
                        fetchReferencesAndExecute(user, items, total);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to reach server", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchReferencesAndExecute(User user, List<CartItem> items, double total) {
        List<Task<QuerySnapshot>> inventoryTasks = new ArrayList<>();
        List<Task<QuerySnapshot>> menuTasks = new ArrayList<>();

        for (CartItem item : items) {
            String name = item.getFoodItem().getName();
            inventoryTasks.add(db.collection("inventory").whereEqualTo("itemName", name).get());
            menuTasks.add(db.collection("menu").whereEqualTo("name", name).get());
        }

        Task<List<QuerySnapshot>> invTask = Tasks.whenAllSuccess(inventoryTasks);
        Task<List<QuerySnapshot>> menuTask = Tasks.whenAllSuccess(menuTasks);

        Tasks.whenAllComplete(invTask, menuTask).addOnCompleteListener(t -> {
            if (!invTask.isSuccessful() || !menuTask.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.error_order_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, DocumentReference> invRefs = new HashMap<>();
            Map<String, DocumentReference> menuRefs = new HashMap<>();

            List<QuerySnapshot> invResults = invTask.getResult();
            List<QuerySnapshot> menuResults = menuTask.getResult();

            for (int i = 0; i < items.size(); i++) {
                String name = items.get(i).getFoodItem().getName();
                if (!invResults.get(i).isEmpty()) {
                    invRefs.put(name, invResults.get(i).getDocuments().get(0).getReference());
                }
                if (!menuResults.get(i).isEmpty()) {
                    menuRefs.put(name, menuResults.get(i).getDocuments().get(0).getReference());
                }
            }

            executeTransaction(user, items, total, invRefs, menuRefs);
        });
    }

    private void executeTransaction(User user, List<CartItem> items, double total,
                                    Map<String, DocumentReference> invRefs,
                                    Map<String, DocumentReference> menuRefs) {
        db.runTransaction(transaction -> {
            List<String> errors = new ArrayList<>();
            Map<String, Integer> currentStocks = new HashMap<>();

            for (CartItem item : items) {
                String name = item.getFoodItem().getName();
                DocumentReference invRef = invRefs.get(name);

                if (invRef == null) {
                    errors.add(getString(R.string.error_out_of_stock, name));
                    continue;
                }

                DocumentSnapshot invSnap = transaction.get(invRef);
                long stock = 0;
                if (invSnap.exists() && invSnap.contains("currentStock")) {
                    stock = invSnap.getLong("currentStock");
                }
                
                if (stock < item.getQuantity()) {
                    if (stock <= 0) {
                        errors.add(getString(R.string.error_out_of_stock, name));
                    } else {
                        errors.add(getString(R.string.error_insufficient_stock, (int)stock, name));
                    }
                }
                currentStocks.put(name, (int)stock);
            }

            if (!errors.isEmpty()) {
                throw new FirebaseFirestoreException(android.text.TextUtils.join("\n", errors), 
                        FirebaseFirestoreException.Code.ABORTED);
            }

            // All valid, proceed with order and deduction
            DocumentReference orderRef = db.collection("orders").document();
            Order order = new Order(orderRef.getId(), user.getUserId(), user.getName(), items, total, "Pending", System.currentTimeMillis());
            transaction.set(orderRef, order);

            for (CartItem item : items) {
                String name = item.getFoodItem().getName();
                int newStock = currentStocks.get(name) - item.getQuantity();
                transaction.update(invRefs.get(name), "currentStock", newStock);
                
                if (newStock <= 0 && menuRefs.containsKey(name)) {
                    transaction.update(menuRefs.get(name), "available", false);
                }
            }
            return orderRef.getId();
        }).addOnSuccessListener(id -> {
            orderId = id;
            progressBar.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.tvOrderId)).setText("#" + orderId);
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            if (e instanceof FirebaseFirestoreException && 
                ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {
                showStockErrorDialog(e.getMessage());
            } else {
                Toast.makeText(this, R.string.error_order_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStockErrorDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_unavailable_title)
                .setMessage(message)
                .setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                    ((TextView) findViewById(R.id.tvOrderId)).setText("Order Blocked");
                })
                .setCancelable(false)
                .show();
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
