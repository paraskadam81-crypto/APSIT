package com.example.apsitcanteen;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import java.util.HashMap;
import java.util.Map;

public class CanteenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        // Initialize Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dcijqkvhn");
        MediaManager.init(this, config);
    }
}
