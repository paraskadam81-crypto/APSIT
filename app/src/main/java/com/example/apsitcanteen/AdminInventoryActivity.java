package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apsitcanteen.adapters.AdminInventoryAdapter;
import com.example.apsitcanteen.admin.InventoryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminInventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminInventoryAdapter adapter;
    private List<InventoryItem> inventoryList = new ArrayList<>();
    private View rootLayout;
    private LinearLayout lowStockBanner;
    private TextView tvLowStockAlert;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private ListenerRegistration inventoryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inventory);

        db = FirebaseFirestore.getInstance();

        rootLayout = findViewById(R.id.rootLayout);
        lowStockBanner = findViewById(R.id.lowStockBanner);
        tvLowStockAlert = findViewById(R.id.tvLowStockAlert);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminInventoryAdapter(this, inventoryList,
                (item, position) -> showUpdateStockDialog(item));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnRestockAll).setOnClickListener(v -> restockAllLowItems());

        FloatingActionButton fab = findViewById(R.id.fabAddInventory);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAddInventoryItemActivity.class));
        });

        lowStockBanner.setOnClickListener(v -> {
            for (int i = 0; i < inventoryList.size(); i++) {
                if (inventoryList.get(i).getCurrentStock() < inventoryList.get(i).getMinStock()) {
                    recyclerView.smoothScrollToPosition(i);
                    break;
                }
            }
        });

        listenToInventory();
    }

    private void listenToInventory() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        inventoryListener = db.collection("inventory")
                .addSnapshotListener((value, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (error != null) return;

                    inventoryList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            InventoryItem item = doc.toObject(InventoryItem.class);
                            item.setId(doc.getId());
                            inventoryList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateSummary();
                });
    }

    private void updateSummary() {
        int totalItems = inventoryList.size();
        int lowStockCount = 0;
        for (InventoryItem item : inventoryList) {
            if (item.getCurrentStock() < item.getMinStock()) {
                lowStockCount++;
            }
        }

        ((TextView) findViewById(R.id.tvTotalItems)).setText("Total: " + totalItems);
        ((TextView) findViewById(R.id.tvLowStockCount)).setText("Low Stock: " + lowStockCount);

        if (lowStockCount > 0) {
            lowStockBanner.setVisibility(View.VISIBLE);
            tvLowStockAlert.setText("⚠ " + lowStockCount + " items are running low — tap to restock");
        } else {
            lowStockBanner.setVisibility(View.GONE);
        }
    }

    private void showUpdateStockDialog(InventoryItem item) {
        android.widget.EditText etStock = new android.widget.EditText(this);
        etStock.setPadding(50, 40, 50, 40);
        etStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etStock.setText(String.valueOf(item.getCurrentStock()));

        new AlertDialog.Builder(this)
                .setTitle("Update Stock for " + item.getItemName())
                .setView(etStock)
                .setPositiveButton("Update", (dialog, which) -> {
                    String val = etStock.getText().toString();
                    if (!val.isEmpty()) {
                        int newStock = Integer.parseInt(val);
                        db.collection("inventory").document(item.getId())
                                .update("currentStock", newStock)
                                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void restockAllLowItems() {
        for (InventoryItem item : inventoryList) {
            if (item.getCurrentStock() < item.getMinStock()) {
                int newStock = item.getMinStock() + 10;
                db.collection("inventory").document(item.getId()).update("currentStock", newStock);
            }
        }
        Snackbar.make(rootLayout, "Restock command sent", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inventoryListener != null) inventoryListener.remove();
    }
}
