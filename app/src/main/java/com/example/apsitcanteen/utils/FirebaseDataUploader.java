package com.example.apsitcanteen.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;

public class FirebaseDataUploader {
    private static final String TAG = "FirebaseDataUploader";

    public static void uploadAll() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // 50 Items Data: {Name, Category, Price, Description}
        String[][] items = {
            {"Samosa", "Snacks", "15", "Crispy potato filled pastry"},
            {"Vada Pav", "Snacks", "20", "Spicy potato fritter in bun"},
            {"Spring Roll", "Snacks", "35", "Veg filled crispy rolls"},
            {"Bread Pakoda", "Snacks", "25", "Fried bread with potato stuffing"},
            {"Kachori", "Snacks", "20", "Spicy dal filled fried pastry"},
            {"Aloo Bonda", "Snacks", "15", "Deep fried potato dumplings"},
            {"Poha", "Snacks", "25", "Flattened rice with onions and spices"},
            {"Upma", "Snacks", "25", "Savory semolina porridge"},
            {"Misal Pav", "Snacks", "50", "Spicy sprout curry with bread"},
            {"Sabudana Vada", "Snacks", "40", "Sago and potato deep fried patties"},
            {"Onion Bhaji", "Snacks", "30", "Crispy onion fritters"},
            {"Paneer Pakoda", "Snacks", "50", "Deep fried cottage cheese fritters"},
            {"Veg Sandwich", "Snacks", "40", "Fresh vegetable sandwich"},
            {"Cheese Grill Sandwich", "Snacks", "60", "Grilled cheese and vegetable sandwich"},
            {"Corn Cheese Balls", "Snacks", "70", "Deep fried corn and cheese balls"},

            {"Rajma Rice", "Meals", "70", "Red kidney beans with steamed rice"},
            {"Chole Chawal", "Meals", "65", "Spicy chickpeas with rice"},
            {"Paneer Wrap", "Meals", "80", "Soft wrap with paneer and veggies"},
            {"Veg Thali", "Meals", "90", "Full meal with roti, sabzi, rice, dal"},
            {"Special Thali", "Meals", "120", "Premium meal with paneer, sweet, papad"},
            {"Veg Biryani", "Meals", "100", "Aromatic spiced rice with veggies"},
            {"Paneer Butter Masala Combo", "Meals", "110", "Paneer butter masala with 2 rotis"},
            {"Dal Tadka Combo", "Meals", "80", "Yellow dal tadka with jeera rice"},
            {"Pav Bhaji", "Meals", "70", "Spicy mashed veg curry with buttered buns"},
            {"Chole Bhature", "Meals", "80", "Spicy chickpeas with fried bread"},
            {"Veg Kolhapuri Combo", "Meals", "110", "Spicy veg kolhapuri with 2 rotis"},
            {"Palak Paneer Combo", "Meals", "110", "Spinach and paneer curry with 2 rotis"},
            {"Hakka Noodles", "Meals", "80", "Stir-fried veg noodles"},
            {"Manchurian Rice", "Meals", "90", "Veg manchurian with fried rice"},
            {"Veg Fried Rice", "Meals", "80", "Classic stir-fried veg rice"},

            {"Masala Chai", "Beverages", "15", "Indian spiced milk tea"},
            {"Cold Coffee", "Beverages", "40", "Refreshing chilled coffee"},
            {"Mango Shake", "Beverages", "50", "Thick seasonal mango shake"},
            {"Lemonade", "Beverages", "25", "Fresh sweet and sour lime water"},
            {"Buttermilk", "Beverages", "20", "Chilled savory yogurt drink"},
            {"Sweet Lassi", "Beverages", "40", "Thick sweet yogurt drink"},
            {"Oreo Shake", "Beverages", "70", "Chocolatey oreo milkshake"},
            {"Chocolate Milkshake", "Beverages", "60", "Creamy chocolate milkshake"},
            {"Filter Coffee", "Beverages", "30", "Traditional south Indian coffee"},
            {"Hot Coffee", "Beverages", "25", "Instant hot milk coffee"},
            {"Black Tea", "Beverages", "10", "Fresh brewed black tea"},
            {"Green Tea", "Beverages", "20", "Healthy green tea"},

            {"Gulab Jamun", "Desserts", "30", "Sweet milk-solid balls in syrup"},
            {"Brownie", "Desserts", "45", "Rich chocolate walnut brownie"},
            {"Rasgulla", "Desserts", "35", "Soft spongy cottage cheese balls"},
            {"Jalebi", "Desserts", "40", "Crispy sweet syrupy spirals"},
            {"Vanilla Ice Cream", "Desserts", "40", "Classic vanilla scoop"},
            {"Chocolate Ice Cream", "Desserts", "50", "Rich chocolate scoop"},
            {"Fruit Salad", "Desserts", "60", "Fresh seasonal fruit mix"},
            {"Black Forest Pastry", "Desserts", "50", "Chocolate layered cream cake"}
        };

        for (int i = 0; i < items.length; i++) {
            String name = items[i][0];
            String category = items[i][1];
            double price = Double.parseDouble(items[i][2]);
            String desc = items[i][3];
            String id = "item_" + (i + 1);

            // Add to Menu
            Map<String, Object> menuData = new HashMap<>();
            menuData.put("name", name);
            menuData.put("category", category);
            menuData.put("price", price);
            menuData.put("description", desc);
            menuData.put("available", true);
            menuData.put("imageUrl", "");

            batch.set(db.collection("menu").document(id), menuData);

            // Add to Inventory
            Map<String, Object> invData = new HashMap<>();
            invData.put("itemName", name);
            invData.put("category", category);
            invData.put("currentStock", 50);
            invData.put("minStock", 10);
            invData.put("unit", category.equals("Beverages") ? "cups" : "pieces");
            invData.put("costPrice", price * 0.7); // Estimated cost
            invData.put("lastRestocked", "21 Mar 2026");

            batch.set(db.collection("inventory").document(id), invData);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "50 items uploaded successfully to menu and inventory!");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to upload items", e);
        });
    }
}
