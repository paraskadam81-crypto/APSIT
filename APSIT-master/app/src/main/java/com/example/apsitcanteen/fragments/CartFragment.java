package com.example.apsitcanteen.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.OrderConfirmationActivity;
import com.example.apsitcanteen.R;
import com.example.apsitcanteen.adapters.CartAdapter;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.utils.CartManager;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart = view.findViewById(R.id.rvCart);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        cardTotal = view.findViewById(R.id.cardTotal);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
        progressBar = view.findViewById(R.id.progressBar);

        setupRecyclerView();
        updateUI();

        btnPlaceOrder.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderConfirmationActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(CartManager.getInstance().getCartItems(), new CartAdapter.OnCartQuantityChangeListener() {
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
        adapter.notifyDataSetChanged();
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
            tvTotalPrice.setText("₹" + (int)total);
        }
    }
}