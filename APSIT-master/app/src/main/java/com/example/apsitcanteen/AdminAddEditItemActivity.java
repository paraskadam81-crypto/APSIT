package com.example.apsitcanteen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.apsitcanteen.models.FoodItem;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminAddEditItemActivity extends AppCompatActivity {

    private static final String TAG = "AdminAddEditItem";
    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean isEditMode = false;
    private String itemId;
    private FirebaseFirestore db;

    private EditText etName, etPrice, etDescription, etImageUrl;
    private AutoCompleteTextView actvCategory;
    private Switch swAvailable;
    private TextInputLayout tilName, tilCategory, tilPrice, tilDescription, tilImageUrl;
    private ProgressBar progressBar;
    private ImageView ivFoodImage;
    private Uri selectedImageUri;

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

        findViewById(R.id.cardPickImage).setOnClickListener(v -> openImagePicker());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validateForm()) {
                if (selectedImageUri != null) {
                    uploadImageAndSave();
                } else {
                    saveItem();
                }
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
        ivFoodImage = findViewById(R.id.ivFoodImage);

        tilName = findViewById(R.id.tilItemName);
        tilCategory = findViewById(R.id.tilCategory);
        tilPrice = findViewById(R.id.tilPrice);
        tilDescription = findViewById(R.id.tilDescription);
        tilImageUrl = findViewById(R.id.tilImageUrl);
        progressBar = findViewById(R.id.progressBar);
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivFoodImage.setImageURI(selectedImageUri);
        }
    }

    private void uploadImageAndSave() {
        progressBar.setVisibility(View.VISIBLE);
        MediaManager.get().upload(selectedImageUri)
                .unsigned("apsit_canteen")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        etImageUrl.setText(imageUrl);
                        saveItem();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AdminAddEditItemActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch();
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

                        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                            Glide.with(this).load(item.getImageUrl()).placeholder(R.drawable.ic_food_placeholder).into(ivFoodImage);
                        }
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

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", name);
        itemMap.put("description", description);
        itemMap.put("category", category);
        itemMap.put("price", price);
        itemMap.put("imageUrl", imageUrl);
        itemMap.put("available", isAvailable);
        itemMap.put("stock", 50);

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
                        Toast.makeText(this, "Added successfully", Toast.LENGTH_LONG).show();
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
