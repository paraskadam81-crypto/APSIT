package com.example.apsitcanteen;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.admin.InventoryItem;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminAddInventoryItemActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etName, etCurrentStock, etMinStock, etUnit, etCostPrice;
    private AutoCompleteTextView actvCategory;
    private TextInputLayout tilName, tilCategory, tilCurrentStock, tilMinStock, tilUnit;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_inventory);

        db = FirebaseFirestore.getInstance();

        initUI();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String[] categories = {"Snacks", "Meals", "Beverages", "Desserts", "Raw Materials"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validateForm()) {
                saveInventoryItem();
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void initUI() {
        etName = findViewById(R.id.etItemName);
        actvCategory = findViewById(R.id.actvCategory);
        etCurrentStock = findViewById(R.id.etCurrentStock);
        etMinStock = findViewById(R.id.etMinStock);
        etUnit = findViewById(R.id.etUnit);
        etCostPrice = findViewById(R.id.etCostPrice);

        tilName = findViewById(R.id.tilItemName);
        tilCategory = findViewById(R.id.tilCategory);
        tilCurrentStock = findViewById(R.id.tilCurrentStock);
        tilMinStock = findViewById(R.id.tilMinStock);
        tilUnit = findViewById(R.id.tilUnit);
        progressBar = findViewById(R.id.progressBar);
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

        if (etCurrentStock.getText().toString().isEmpty()) {
            tilCurrentStock.setError("Required");
            isValid = false;
        } else tilCurrentStock.setError(null);

        if (etMinStock.getText().toString().isEmpty()) {
            tilMinStock.setError("Required");
            isValid = false;
        } else tilMinStock.setError(null);

        if (etUnit.getText().toString().trim().isEmpty()) {
            tilUnit.setError("Required");
            isValid = false;
        } else tilUnit.setError(null);

        return isValid;
    }

    private void saveInventoryItem() {
        progressBar.setVisibility(View.VISIBLE);
        
        String name = etName.getText().toString().trim();
        String category = actvCategory.getText().toString();
        int currentStock = Integer.parseInt(etCurrentStock.getText().toString());
        int minStock = Integer.parseInt(etMinStock.getText().toString());
        String unit = etUnit.getText().toString().trim();
        
        double costPrice = 0;
        try {
            String cp = etCostPrice.getText().toString();
            if (!cp.isEmpty()) costPrice = Double.parseDouble(cp);
        } catch (Exception e) {}

        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        InventoryItem item = new InventoryItem(null, name, category, currentStock, minStock, unit, costPrice, today);

        db.collection("inventory").add(item)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Inventory item added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
