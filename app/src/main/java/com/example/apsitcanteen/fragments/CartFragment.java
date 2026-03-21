package com.example.apsitcanteen.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private TextView tvSubtotal, tvTotalAmount;
    private LinearLayout layoutEmptyCart;
    private View cardSummary;
    private Button btnPlaceOrder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart = view.findViewById(R.id.rvCart);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
        cardSummary = view.findViewById(R.id.cardSummary);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);

        setupRecyclerView();
        updateUI();

        btnPlaceOrder.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderConfirmationActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btnBrowseMenu).setOnClickListener(v -> {
            // Navigate back to HomeFragment
            if (getActivity() != null) {
                ((com.example.apsitcanteen.MainActivity) getActivity()).findViewById(R.id.nav_home).performClick();
            }
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
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            cardSummary.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            cardSummary.setVisibility(View.VISIBLE);
            
            double total = CartManager.getInstance().getTotalPrice();
            tvSubtotal.setText(getString(R.string.currency_format, (int)total));
            tvTotalAmount.setText(getString(R.string.currency_format, (int)total));
        }
    }
}