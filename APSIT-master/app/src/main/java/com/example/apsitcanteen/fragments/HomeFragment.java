package com.example.apsitcanteen.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<FoodItem> masterList = new ArrayList<>();
    private List<FoodItem> filteredList = new ArrayList<>();
    private MenuAdapter adapter;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private String currentCategory = "All";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        RecyclerView rvMenu = view.findViewById(R.id.rvMenu);
        EditText etSearch = view.findViewById(R.id.etSearch);

        // Animation for recycler view
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.pop_in);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setDelay(0.15f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        rvMenu.setLayoutAnimation(controller);

        rvMenu.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MenuAdapter(filteredList, item -> {
            CartManager.getInstance().addItem(item);
            Toast.makeText(getContext(), item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        });
        rvMenu.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupCategoryChips(view);
        fetchMenu();

        return view;
    }

    private void setupCategoryChips(View view) {
        TextView chipAll = view.findViewById(R.id.chipAll);
        TextView chipSnacks = view.findViewById(R.id.chipSnacks);
        TextView chipMeals = view.findViewById(R.id.chipMeals);
        TextView chipBeverages = view.findViewById(R.id.chipBeverages);
        TextView chipDesserts = view.findViewById(R.id.chipDesserts);

        View.OnClickListener listener = v -> {
            TextView clickedChip = (TextView) v;
            currentCategory = clickedChip.getText().toString();
            
            // Reset all chips
            chipAll.setBackgroundResource(R.drawable.bg_category_chip_unselected);
            chipSnacks.setBackgroundResource(R.drawable.bg_category_chip_unselected);
            chipMeals.setBackgroundResource(R.drawable.bg_category_chip_unselected);
            chipBeverages.setBackgroundResource(R.drawable.bg_category_chip_unselected);
            chipDesserts.setBackgroundResource(R.drawable.bg_category_chip_unselected);
            
            chipAll.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            chipSnacks.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            chipMeals.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            chipBeverages.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            chipDesserts.setTextColor(getResources().getColor(R.color.colorTextSecondary));

            // Highlight clicked
            clickedChip.setBackgroundResource(R.drawable.bg_category_chip_selected);
            clickedChip.setTextColor(getResources().getColor(R.color.white));
            
            applyFilters();
        };

        chipAll.setOnClickListener(listener);
        chipSnacks.setOnClickListener(listener);
        chipMeals.setOnClickListener(listener);
        chipBeverages.setOnClickListener(listener);
        chipDesserts.setOnClickListener(listener);
    }

    private void fetchMenu() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("menu").addSnapshotListener((value, error) -> {
            progressBar.setVisibility(View.GONE);
            if (error != null || value == null) return;

            masterList.clear();
            for (QueryDocumentSnapshot doc : value) {
                FoodItem item = doc.toObject(FoodItem.class);
                item.setId(doc.getId());
                masterList.add(item);
            }
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredList.clear();
        for (FoodItem item : masterList) {
            boolean categoryMatch = currentCategory.equals("All") || item.getCategory().equalsIgnoreCase(currentCategory);
            boolean searchMatch = item.getName().toLowerCase().contains(searchQuery);
            if (categoryMatch && searchMatch) {
                filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
