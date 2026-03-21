package com.example.apsitcanteen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * First screen of the app. Allows users to choose between Student or Admin login.
 */
public class LandingActivity extends AppCompatActivity {

    private View logoContainer;
    private View buttonContainer;
    private Button btnStudentLogin, btnAdminLogin;
    private TextView tvCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        logoContainer = findViewById(R.id.logoContainer);
        buttonContainer = findViewById(R.id.buttonContainer);
        btnStudentLogin = findViewById(R.id.btnStudentLogin);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        // Navigation to Student Login
        btnStudentLogin.setOnClickListener(v -> {
            startActivity(new Intent(LandingActivity.this, StudentLoginActivity.class));
        });

        // Navigation to Admin Login
        btnAdminLogin.setOnClickListener(v -> {
            startActivity(new Intent(LandingActivity.this, AdminLoginActivity.class));
        });

        // Navigation to Signup
        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LandingActivity.this, SignupActivity.class));
        });

        startEntranceAnimations();
    }

    private void startEntranceAnimations() {
        // Logo and Title fade in
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(600);
        fadeIn.setFillAfter(true);
        logoContainer.startAnimation(fadeIn);

        // Buttons slide up
        Animation slideUp = new TranslateAnimation(0, 0, 150, 0);
        slideUp.setDuration(400);
        slideUp.setStartOffset(600); // Start after fade in
        slideUp.setFillAfter(true);
        buttonContainer.startAnimation(slideUp);
    }
}