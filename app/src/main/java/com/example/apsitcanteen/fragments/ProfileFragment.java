package com.example.apsitcanteen.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.apsitcanteen.LandingActivity;
import com.example.apsitcanteen.R;
import com.example.apsitcanteen.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvInitials;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvInitials = view.findViewById(R.id.tvInitials);

        loadUserProfile();

        view.findViewById(R.id.rowOrders).setOnClickListener(v -> {
            if (getActivity() != null) {
                ((com.example.apsitcanteen.MainActivity) getActivity()).findViewById(R.id.nav_orders).performClick();
            }
        });

        view.findViewById(R.id.rowHelp).setOnClickListener(v -> showComingSoon());
        view.findViewById(R.id.rowAbout).setOnClickListener(v -> showComingSoon());

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvName.setText(user.getName());
                            tvEmail.setText(user.getEmail());
                            if (user.getName() != null && !user.getName().isEmpty()) {
                                tvInitials.setText(user.getName().substring(0, 1).toUpperCase());
                            }
                        }
                    }
                });
    }

    private void showComingSoon() {
        Toast.makeText(getContext(), "Coming soon", Toast.LENGTH_SHORT).show();
    }
}
