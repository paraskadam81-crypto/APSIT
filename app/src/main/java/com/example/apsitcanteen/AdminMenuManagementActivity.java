package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.adapters.AdminMenuAdapter;
import com.example.apsitcanteen.models.FoodItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminMenuManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminMenuAdapter adapter;
    private List<FoodItem> masterList = new ArrayList<>();
    private List<FoodItem> filteredList = new ArrayList<>();
    private String currentCategory = "All";
    private String searchQuery = "";

    private FirebaseFirestore db;
    private ListenerRegistration menuListener;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private static final int REQ_ADD_ITEM = 101;
    private static final int REQ_EDIT_ITEM = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu_management);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.rvMenuItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminMenuAdapter(this, filteredList, new AdminMenuAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(FoodItem item, int position) {
                Intent intent = new Intent(AdminMenuManagementActivity.this, AdminAddEditItemActivity.class);
                intent.putExtra("isEditMode", true);
                intent.putExtra("itemId", item.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(FoodItem item, int position) {
                showDeleteDialog(item);
            }

            @Override
            public void onStatusToggle(FoodItem item, boolean isAvailable) {
                db.collection("menu").document(item.getId())
                        .update("available", isAvailable)
                        .addOnFailureListener(e -> Toast.makeText(AdminMenuManagementActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
            }
        });
        recyclerView.setAdapter(adapter);

        listenToMenu();

        EditText etSearch = findViewById(R.id.etSearch);
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

        setupCategoryChips();

        FloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(AdminMenuManagementActivity.this, AdminAddEditItemActivity.class));
        });
    }

    private void listenToMenu() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        menuListener = db.collection("menu")
                .addSnapshotListener((value, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (error != null) return;

                    masterList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            item.setId(doc.getId());
                            masterList.add(item);
                        }
                    }
                    applyFilters();
                });
    }

    private void setupCategoryChips() {
        String[] categories = {"All", "Snacks", "Meals", "Beverages", "Desserts"};
        LinearLayout chipContainer = findViewById(R.id.chipContainer);

        for (String category : categories) {
            TextView chip = (TextView) getLayoutInflater()
                    .inflate(R.layout.layout_category_chip, chipContainer, false);
            chip.setText(category);
            updateChipStyle(chip, category.equals(currentCategory));

            chip.setOnClickListener(v -> {
                currentCategory = category;
                for (int i = 0; i < chipContainer.getChildCount(); i++) {
                    TextView c = (TextView) chipContainer.getChildAt(i);
                    updateChipStyle(c, c.getText().toString().equals(currentCategory));
                }
                applyFilters();
            });
            chipContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_fab_gold);
            chip.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_status_inactive);
            chip.setTextColor(getResources().getColor(R.color.colorTextSecondary));
        }
    }

    private void applyFilters() {
        filteredList.clear();
        for (FoodItem item : masterList) {
            boolean matchesCategory = currentCategory.equals("All") ||
                    item.getCategory().equalsIgnoreCase(currentCategory);
            boolean matchesSearch = item.getName().toLowerCase().contains(searchQuery);
            if (matchesCategory && matchesSearch) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void showDeleteDialog(FoodItem item) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + item.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("menu").document(item.getId()).delete();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (menuListener != null) menuListener.remove();
    }
}
