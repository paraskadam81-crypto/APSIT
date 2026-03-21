package com.example.apsitcanteen;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.FoodItem;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminAddEditItemActivity extends AppCompatActivity {

    private static final String TAG = "AdminAddEditItem";
    private boolean isEditMode = false;
    private String itemId;
    private FirebaseFirestore db;

    private EditText etName, etPrice, etDescription, etImageUrl;
    private AutoCompleteTextView actvCategory;
    private Switch swAvailable;
    private TextInputLayout tilName, tilCategory, tilPrice, tilDescription, tilImageUrl;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_item);

        db = FirebaseFirestore.getInstance();

        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        itemId = getIntent().getStringExtra("itemId");

        initUI();

        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        tvTitle.setText(isEditMode ? "Edit Menu Item" : "Add Menu Item");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String[] categories = {"Snacks", "Meals", "Beverages", "Desserts"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);

        if (isEditMode && itemId != null) {
            loadItemData();
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validateForm()) {
                saveItem();
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void initUI() {
        etName = findViewById(R.id.etItemName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        actvCategory = findViewById(R.id.actvCategory);
        swAvailable = findViewById(R.id.swAvailable);
        etImageUrl = findViewById(R.id.etImageUrl);

        tilName = findViewById(R.id.tilItemName);
        tilCategory = findViewById(R.id.tilCategory);
        tilPrice = findViewById(R.id.tilPrice);
        tilDescription = findViewById(R.id.tilDescription);
        tilImageUrl = findViewById(R.id.tilImageUrl);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadItemData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("menu").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    FoodItem item = documentSnapshot.toObject(FoodItem.class);
                    if (item != null) {
                        etName.setText(item.getName());
                        actvCategory.setText(item.getCategory(), false);
                        etPrice.setText(String.valueOf(item.getPrice()));
                        etDescription.setText(item.getDescription());
                        etImageUrl.setText(item.getImageUrl());
                        swAvailable.setChecked(item.isAvailable());
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (etName.getText().toString().trim().isEmpty()) {
            tilName.setError("Required");
            isValid = false;
        } else tilName.setError(null);

        if (actvCategory.getText().toString().isEmpty()) {
            tilCategory.setError("Required");
            isValid = false;
        } else tilCategory.setError(null);

        if (etPrice.getText().toString().trim().isEmpty()) {
            tilPrice.setError("Required");
            isValid = false;
        } else tilPrice.setError(null);

        return isValid;
    }

    private void saveItem() {
        progressBar.setVisibility(View.VISIBLE);
        String name = etName.getText().toString().trim();
        String category = actvCategory.getText().toString();
        
        double price = 0;
        try {
            price = Double.parseDouble(etPrice.getText().toString());
        } catch (NumberFormatException e) {
            price = 0;
        }
        
        String description = etDescription.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        boolean isAvailable = swAvailable.isChecked();

        // Using a Map for saving to ensure field names match what the app expects
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", name);
        itemMap.put("description", description);
        itemMap.put("category", category);
        itemMap.put("price", price);
        itemMap.put("imageUrl", imageUrl);
        itemMap.put("available", isAvailable);
        itemMap.put("stock", 50); // Default stock

        if (isEditMode) {
            db.collection("menu").document(itemId).set(itemMap)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Update failed", e);
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("menu").add(itemMap)
                    .addOnSuccessListener(documentReference -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Added successfully to collection: menu", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Document added with ID: " + documentReference.getId());
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Add failed", e);
                        Toast.makeText(this, "Add failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
