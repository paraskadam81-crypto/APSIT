package com.example.apsitcanteen;

import android.app.Activity;
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
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderConfirmationActivity extends AppCompatActivity implements PaymentResultListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private String orderId;
    private double totalAmount;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        Checkout.preload(getApplicationContext());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        setupItemsList();

        totalAmount = CartManager.getInstance().getTotalPrice();
        ((TextView) findViewById(R.id.tvTotalAmount)).setText(
                String.format(Locale.getDefault(), "₹%d", (int) totalAmount));

        fetchUserAndStartPayment();

        findViewById(R.id.btnTrackOrder).setOnClickListener(v -> {
            if (orderId != null) {
                Intent intent = new Intent(OrderConfirmationActivity.this, OrderStatusActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnBackToMenu).setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void fetchUserAndStartPayment() {
        if (mAuth.getCurrentUser() == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        preCheckInventoryAndStartPayment();
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

    private void preCheckInventoryAndStartPayment() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        List<Task<QuerySnapshot>> inventoryTasks = new ArrayList<>();
        for (CartItem item : items) {
            String name = item.getFoodItem().getName();
            inventoryTasks.add(db.collection("inventory").whereEqualTo("itemName", name).get());
        }

        Tasks.whenAllSuccess(inventoryTasks).addOnSuccessListener(results -> {
            List<String> errors = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                QuerySnapshot snapshot = (QuerySnapshot) results.get(i);
                
                if (snapshot == null || snapshot.isEmpty()) {
                    errors.add(item.getFoodItem().getName() + " is out of stock");
                    continue;
                }

                DocumentSnapshot doc = snapshot.getDocuments().get(0);
                long stock = 0;
                if (doc.contains("currentStock")) {
                    stock = doc.getLong("currentStock");
                }
                
                if (stock < item.getQuantity()) {
                    if (stock <= 0) {
                        errors.add(item.getFoodItem().getName() + " is out of stock");
                    } else {
                        errors.add("Sorry, only " + stock + " quantity available for " + item.getFoodItem().getName());
                    }
                }
            }

            if (!errors.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                showStockErrorDialog(android.text.TextUtils.join("\n", errors));
            } else {
                startPayment();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error checking inventory: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_SWyjR5vSiX1xcF");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "APSIT Canteen");
            options.put("description", "Order Payment");
            options.put("currency", "INR");
            options.put("amount", (int) (totalAmount * 100));

            JSONObject prefill = new JSONObject();
            prefill.put("email", currentUser.getEmail());
            options.put("prefill", prefill);

            checkout.open(this, options);
        } catch (Exception e) {
            Log.e("Razorpay", "Error in starting Razorpay Checkout", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Payment initialization error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        placeOrder(razorpayPaymentId);
    }

    @Override
    public void onPaymentError(int code, String response) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Payment failed: " + response, Toast.LENGTH_LONG).show();
        ((TextView) findViewById(R.id.tvOrderId)).setText("Payment Failed");
    }

    private void placeOrder(String paymentId) {
        progressBar.setVisibility(View.VISIBLE);
        List<CartItem> items = new ArrayList<>(CartManager.getInstance().getCartItems());
        fetchReferencesAndExecute(currentUser, items, totalAmount, paymentId);
    }

    private void fetchReferencesAndExecute(User user, List<CartItem> items, double total, String paymentId) {
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
                Toast.makeText(this, "Order processing failed", Toast.LENGTH_SHORT).show();
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

            executeTransaction(user, items, total, invRefs, menuRefs, paymentId);
        });
    }

    private void executeTransaction(User user, List<CartItem> items, double total,
                                    Map<String, DocumentReference> invRefs,
                                    Map<String, DocumentReference> menuRefs,
                                    String paymentId) {
        db.runTransaction(transaction -> {
            List<String> errors = new ArrayList<>();
            Map<String, Integer> currentStocks = new HashMap<>();

            for (CartItem item : items) {
                String name = item.getFoodItem().getName();
                DocumentReference invRef = invRefs.get(name);

                if (invRef == null) {
                    errors.add(name + " is out of stock");
                    continue;
                }

                DocumentSnapshot invSnap = transaction.get(invRef);
                long stock = 0;
                if (invSnap.exists() && invSnap.contains("currentStock")) {
                    stock = invSnap.getLong("currentStock");
                }
                
                if (stock < item.getQuantity()) {
                    if (stock <= 0) {
                        errors.add(name + " is out of stock");
                    } else {
                        errors.add("Only " + stock + " items left for " + name);
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
            Order order = new Order(orderRef.getId(), user.getUserId(), user.getName(), items, total, "Pending", System.currentTimeMillis(), paymentId);
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
            CartManager.getInstance().clearCart();
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            if (e instanceof FirebaseFirestoreException && 
                ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {
                showStockErrorDialog(e.getMessage());
            } else {
                Toast.makeText(this, "Order placement failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStockErrorDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Item Unavailable")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
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
