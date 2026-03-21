package com.example.apsitcanteen;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class CanteenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
