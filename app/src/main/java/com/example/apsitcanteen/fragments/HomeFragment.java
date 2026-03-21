package com.example.apsitcanteen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.R;
import com.example.apsitcanteen.adapters.MenuAdapter;
import com.example.apsitcanteen.models.FoodItem;
import com.example.apsitcanteen.utils.CartManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvMenu;
    private MenuAdapter adapter;
    private List<FoodItem> fullMenuList = new ArrayList<>();
    private TextView chipAll, chipSnacks, chipMeals, chipBeverages, chipDesserts;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private FirebaseFirestore db;
    private ListenerRegistration menuListener;
    private String currentCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        
        rvMenu = view.findViewById(R.id.rvMenu);
        chipAll = view.findViewById(R.id.chipAll);
        chipSnacks = view.findViewById(R.id.chipSnacks);
        chipMeals = view.findViewById(R.id.chipMeals);
        chipBeverages = view.findViewById(R.id.chipBeverages);
        chipDesserts = view.findViewById(R.id.chipDesserts);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupRecyclerView();
        setupCategoryFilters();
        listenToMenuUpdates();

        return view;
    }

    private void listenToMenuUpdates() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        menuListener = db.collection("menu")
                .addSnapshotListener((value, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    fullMenuList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            item.setId(doc.getId());
                            fullMenuList.add(item);
                        }
                    }
                    applyFilter();
                });
    }

    private void setupRecyclerView() {
        adapter = new MenuAdapter(new ArrayList<>(), foodItem -> {
            if (!foodItem.isAvailable()) {
                Toast.makeText(getContext(), "Item currently unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
            CartManager.getInstance().addItem(foodItem);
            if (getView() != null) {
                Snackbar.make(getView(), "Item added to cart", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getResources().getColor(R.color.successGreen))
                        .setTextColor(getResources().getColor(R.color.white))
                        .show();
            }
        });
        rvMenu.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvMenu.setAdapter(adapter);
    }

    private void setupCategoryFilters() {
        chipAll.setOnClickListener(v -> { currentCategory = "All"; updateFilterUI(chipAll); applyFilter(); });
        chipSnacks.setOnClickListener(v -> { currentCategory = "Snacks"; updateFilterUI(chipSnacks); applyFilter(); });
        chipMeals.setOnClickListener(v -> { currentCategory = "Meals"; updateFilterUI(chipMeals); applyFilter(); });
        chipBeverages.setOnClickListener(v -> { currentCategory = "Beverages"; updateFilterUI(chipBeverages); applyFilter(); });
        chipDesserts.setOnClickListener(v -> { currentCategory = "Desserts"; updateFilterUI(chipDesserts); applyFilter(); });
    }

    private void updateFilterUI(TextView selectedChip) {
        resetChips();
        selectedChip.setBackgroundResource(R.drawable.bg_gold_chip_selected);
        selectedChip.setTextColor(getResources().getColor(R.color.colorTextOnAccent));
    }

    private void applyFilter() {
        List<FoodItem> filteredList = new ArrayList<>();
        for (FoodItem item : fullMenuList) {
            if (currentCategory.equals("All") || item.getCategory().equalsIgnoreCase(currentCategory)) {
                filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
        
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void resetChips() {
        int unselectedBg = R.drawable.bg_gold_chip_unselected;
        int unselectedText = getResources().getColor(R.color.colorTextSecondary);

        chipAll.setBackgroundResource(unselectedBg);
        chipAll.setTextColor(unselectedText);
        chipSnacks.setBackgroundResource(unselectedBg);
        chipSnacks.setTextColor(unselectedText);
        chipMeals.setBackgroundResource(unselectedBg);
        chipMeals.setTextColor(unselectedText);
        chipBeverages.setBackgroundResource(unselectedBg);
        chipBeverages.setTextColor(unselectedText);
        chipDesserts.setBackgroundResource(unselectedBg);
        chipDesserts.setTextColor(unselectedText);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (menuListener != null) menuListener.remove();
    }
}
