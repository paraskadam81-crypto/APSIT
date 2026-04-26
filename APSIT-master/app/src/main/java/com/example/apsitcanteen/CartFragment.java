package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apsitcanteen.adapters.CartAdapter;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.utils.CartManager;

// ✅ Firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Fragment to display and manage the user's shopping cart.
 */
public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private TextView tvTotalPrice, tvEmpty;
    private View cardTotal;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    // ✅ Firebase variables
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // ✅ Firebase init
        mAuth    = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        rvCart = view.findViewById(R.id.rvCart);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        cardTotal = view.findViewById(R.id.cardTotal);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
        progressBar = view.findViewById(R.id.progressBar);

        setupRecyclerView();
        updateUI();

        // ✅ Place Order button — fetch student name then go to confirmation
        btnPlaceOrder.setOnClickListener(v -> {

            // Check cart is not empty
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(getContext(),
                        "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check user is logged in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(),
                        "Please login first!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Fetch student name from Firebase users node
            String userId = currentUser.getUid();
            usersRef.child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String studentName = "Student";
                            String studentId   = "S000";

                            if (snapshot.exists()) {
                                String name  = snapshot.child("name").getValue(String.class);
                                String phone = snapshot.child("phone").getValue(String.class);
                                if (name  != null) studentName = name;
                                if (phone != null) studentId   = phone;
                            }

                            // ✅ Pass student info to OrderConfirmationActivity
                            Intent intent = new Intent(getActivity(),
                                    OrderConfirmationActivity.class);
                            intent.putExtra("userId",      userId);
                            intent.putExtra("studentName", studentName);
                            intent.putExtra("studentId",   studentId);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Still proceed even if fetch fails
                            Intent intent = new Intent(getActivity(),
                                    OrderConfirmationActivity.class);
                            intent.putExtra("userId",      userId);
                            intent.putExtra("studentName", "Student");
                            intent.putExtra("studentId",   "S000");
                            startActivity(intent);
                        }
                    });
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(CartManager.getInstance().getCartItems(),
                new CartAdapter.OnCartQuantityChangeListener() {
                    @Override
                    public void onIncrease(CartItem item) {
                        CartManager.getInstance().increaseQuantity(item.getFoodItem());
                        refreshCart();
                    }

                    @Override
                    public void onDecrease(CartItem item) {
                        CartManager.getInstance().decreaseQuantity(item.getFoodItem());
                        refreshCart();
                    }

                    @Override
                    public void onRemove(CartItem item) {
                        CartManager.getInstance().removeItem(item.getFoodItem());
                        refreshCart();
                    }
                });
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(adapter);
    }

    private void refreshCart() {
        adapter.updateList(CartManager.getInstance().getCartItems());
        updateUI();
    }

    private void updateUI() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            cardTotal.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            cardTotal.setVisibility(View.VISIBLE);

            double total = CartManager.getInstance().getTotalPrice();
            tvTotalPrice.setText("₹"    + (int) total);
        }
    }
}