package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private Button btnAdminLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnAdminLogin.setOnClickListener(v -> {
            if (validateInput()) {
                String email = tilEmail.getEditText().getText().toString().trim();
                String password = tilPassword.getEditText().getText().toString().trim();
                loginAdmin(email, password);
            }
        });
    }

    private void loginAdmin(String email, String password) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnAdminLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkRoleAndNavigate(mAuth.getCurrentUser().getUid());
                    } else {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        btnAdminLogin.setEnabled(true);
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkRoleAndNavigate(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && "Admin".equalsIgnoreCase(user.getRole())) {
                            startActivity(new Intent(this, AdminDashboardActivity.class));
                            finishAffinity();
                        } else {
                            mAuth.signOut();
                            btnAdminLogin.setEnabled(true);
                            Toast.makeText(this, "Not an admin account", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mAuth.signOut();
                        btnAdminLogin.setEnabled(true);
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnAdminLogin.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput() {
        String email = tilEmail.getEditText().getText().toString().trim();
        String password = tilPassword.getEditText().getText().toString().trim();
        boolean isValid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            isValid = false;
        } else tilEmail.setError(null);

        if (password.length() < 6) {
            tilPassword.setError("Min 6 characters");
            isValid = false;
        } else tilPassword.setError(null);

        return isValid;
    }
}
