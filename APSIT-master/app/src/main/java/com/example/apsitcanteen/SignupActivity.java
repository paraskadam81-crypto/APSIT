package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apsitcanteen.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputLayout tilName, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> {
            if (!validateInput()) return;

            String name = tilName.getEditText().getText().toString().trim();
            String email = tilEmail.getEditText().getText().toString().trim();
            String password = tilPassword.getEditText().getText().toString().trim();

            signupUser(name, email, password);
        });

        tvLogin.setOnClickListener(v -> finish());
    }

    private void signupUser(String name, String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnSignUp.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        User user = new User(userId, name, email, "Student");

                        db.collection("users").document(userId).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnSignUp.setEnabled(true);
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSignUp.setEnabled(true);
                        Toast.makeText(this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput() {
        String name = tilName.getEditText().getText().toString().trim();
        String email = tilEmail.getEditText().getText().toString().trim();
        String password = tilPassword.getEditText().getText().toString().trim();
        String confirmPassword = tilConfirmPassword.getEditText().getText().toString().trim();
        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError("Required");
            isValid = false;
        } else tilName.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            isValid = false;
        } else tilEmail.setError(null);

        if (password.length() < 6) {
            tilPassword.setError("Min 6 characters");
            isValid = false;
        } else tilPassword.setError(null);

        if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords don't match");
            isValid = false;
        } else tilConfirmPassword.setError(null);

        return isValid;
    }
}
