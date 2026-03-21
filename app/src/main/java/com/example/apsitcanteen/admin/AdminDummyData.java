package com.example.apsitcanteen.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central data repository for the Admin Panel.
 * Contains hardcoded dummy data for menu, orders, inventory, and analytics.
 */
public class AdminDummyData {

    // MENU ITEMS
    public static List<AdminMenuItem> getMenuItems() {
        List<AdminMenuItem> items = new ArrayList<>();
        // Snacks
        items.add(new AdminMenuItem("item001", "Samosa",       "Snacks",    15.0, "Crispy potato filled pastry",          50, true));
        items.add(new AdminMenuItem("item002", "Vada Pav",     "Snacks",    20.0, "Spicy potato fritter in bun",          40, true));
        items.add(new AdminMenuItem("item003", "Spring Roll",  "Snacks",    35.0, "Veg filled crispy rolls",              25, true));
        items.add(new AdminMenuItem("item004", "Bread Pakoda", "Snacks",    25.0, "Fried bread with potato stuffing",     30, true));
        // Meals
        items.add(new AdminMenuItem("item005", "Rajma Rice",   "Meals",     70.0, "Red kidney beans with steamed rice",   20, true));
        items.add(new AdminMenuItem("item006", "Chole Chawal", "Meals",     65.0, "Spicy chickpeas with rice",            22, true));
        items.add(new AdminMenuItem("item007", "Paneer Wrap",  "Meals",     80.0, "Soft wrap with paneer and veggies",    15, true));
        items.add(new AdminMenuItem("item008", "Veg Thali",    "Meals",     90.0, "Full meal with roti, sabzi, rice, dal",10, true));
        // Beverages
        items.add(new AdminMenuItem("item009", "Masala Chai",  "Beverages", 15.0, "Indian spiced milk tea",              100, true));
        items.add(new AdminMenuItem("item010", "Cold Coffee",  "Beverages", 40.0, "Refreshing chilled coffee",            30, true));
        items.add(new AdminMenuItem("item011", "Mango Shake",  "Beverages", 50.0, "Thick seasonal mango shake",           20, true));
        // Desserts
        items.add(new AdminMenuItem("item012", "Gulab Jamun",  "Desserts",  30.0, "Sweet milk-solid balls in syrup",      45, true));
        items.add(new AdminMenuItem("item013", "Brownie",      "Desserts",  45.0, "Rich chocolate walnut brownie",        12, true));
        return items;
    }

    // ORDERS
    public static List<AdminOrder> getOrders() {
        List<AdminOrder> orders = new ArrayList<>();
        orders.add(new AdminOrder("ORD1001", "Aryan Sharma",    "S001", "Samosa x2, Masala Chai x1",            45.0,  "Cash on Pickup", "Pending",   "09:15 AM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1002", "Isha Patel",      "S002", "Veg Thali x1",                         90.0,  "Cash on Pickup", "Preparing", "10:30 AM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1003", "Rohan Gupta",     "S003", "Vada Pav x2, Cold Coffee x1",          80.0,  "Cash on Pickup", "Ready",     "11:00 AM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1004", "Sanya Malhotra",  "S004", "Paneer Wrap x1, Mango Shake x1",      130.0,  "Cash on Pickup", "Completed", "12:15 PM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1005", "Kabir Singh",     "S005", "Spring Roll x1",                       35.0,  "Cash on Pickup", "Cancelled", "01:00 PM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1006", "Ananya Pandey",   "S006", "Rajma Rice x2",                       140.0,  "Cash on Pickup", "Pending",   "02:30 PM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1007", "Varun Dhawan",    "S007", "Brownie x2",                           90.0,  "Cash on Pickup", "Completed", "03:45 PM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1008", "Kriti Sanon",     "S008", "Chole Chawal x1, Gulab Jamun x1",     95.0,  "Cash on Pickup", "Preparing", "04:00 PM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1009", "Siddharth Roy",   "S009", "Bread Pakoda x4",                     100.0,  "Cash on Pickup", "Pending",   "05:20 PM", "14 Mar 2026"));
        orders.add(new AdminOrder("ORD1010", "Kiara Advani",    "S010", "Cold Coffee x2",                       80.0,  "Cash on Pickup", "Completed", "06:00 PM", "14 Mar 2026"));
        return orders;
    }

    // INVENTORY
    public static List<InventoryItem> getInventoryItems() {
        List<InventoryItem> inventory = new ArrayList<>();
        List<AdminMenuItem> menu = getMenuItems();
        for (AdminMenuItem item : menu) {
            int currentStock = item.getStock();
            // Low stock overrides
            if (item.getName().equals("Veg Thali"))    currentStock = 3;
            if (item.getName().equals("Brownie"))       currentStock = 4;
            if (item.getName().equals("Paneer Wrap"))   currentStock = 2;

            inventory.add(new InventoryItem(
                    item.getId(),                           // ✅ Now String
                    item.getName(),
                    item.getCategory(),
                    currentStock,
                    5,                                      // minStock threshold
                    getUnitForCategory(item.getCategory()),
                    item.getPrice() * 0.6,                  // dummy cost price
                    "12 Mar 2026"
            ));
        }
        return inventory;
    }

    private static String getUnitForCategory(String category) {
        switch (category) {
            case "Beverages": return "cups";
            case "Meals":     return "plates";
            default:          return "pieces";
        }
    }

    // ANALYTICS DATA
    public static LinkedHashMap<String, Double> getDailyRevenue() {
        LinkedHashMap<String, Double> revenue = new LinkedHashMap<>();
        revenue.put("Mon", 1240.0);
        revenue.put("Tue", 1850.0);
        revenue.put("Wed",  980.0);
        revenue.put("Thu", 2100.0);
        revenue.put("Fri", 1650.0);
        revenue.put("Sat", 2400.0);
        revenue.put("Sun", 1320.0);
        return revenue;
    }

    public static Map<String, Integer> getOrderCountByStatus() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Pending",   3);
        counts.put("Preparing", 2);
        counts.put("Ready",     1);
        counts.put("Completed", 3);
        counts.put("Cancelled", 1);
        return counts;
    }

    public static LinkedHashMap<String, Integer> getTopItemSales() {
        LinkedHashMap<String, Integer> sales = new LinkedHashMap<>();
        sales.put("Samosa",      45);
        sales.put("Masala Chai", 38);
        sales.put("Vada Pav",    32);
        sales.put("Veg Thali",   28);
        sales.put("Cold Coffee", 25);
        return sales;
    }
}