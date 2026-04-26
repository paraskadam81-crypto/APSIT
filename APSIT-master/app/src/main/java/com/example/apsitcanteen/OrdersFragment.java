package com.example.apsitcanteen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apsitcanteen.adapters.OrderHistoryAdapter;
import com.example.apsitcanteen.utils.DummyData;

/**
 * Fragment to display past order history.
 */
public class OrdersFragment extends Fragment {

    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        rvOrderHistory = view.findViewById(R.id.rvOrders);
        
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(DummyData.getDummyOrders());
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderHistory.setAdapter(adapter);
    }
}