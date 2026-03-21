package com.example.apsitcanteen.utils;

import com.example.apsitcanteen.R;
import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.FoodItem;
import com.example.apsitcanteen.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to provide dummy data for the app.
 */
public class DummyData {

    public static List<FoodItem> getMenuItems() {
        List<FoodItem> items = new ArrayList<>();

        // Snacks
        items.add(new FoodItem("item001", "Samosa",       "Crispy pastry with potato filling",        "Snacks",    15.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item002", "Vada Pav",     "The classic Mumbai burger",                 "Snacks",    20.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item003", "Spring Roll",  "Crunchy veg spring rolls",                  "Snacks",    35.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item004", "Bread Pakoda", "Fried bread with potato stuffing",          "Snacks",    25.0, R.drawable.ic_food_placeholder));

        // Meals
        items.add(new FoodItem("item005", "Rajma Rice",   "Home-style kidney beans with rice",         "Meals",     70.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item006", "Chole Chawal", "Spicy chickpeas with steamed rice",         "Meals",     65.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item007", "Paneer Wrap",  "Grilled wrap with paneer and veggies",      "Meals",     80.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item008", "Veg Thali",    "Complete meal with roti, dal, sabzi, rice", "Meals",     90.0, R.drawable.ic_food_placeholder));

        // Beverages
        items.add(new FoodItem("item009", "Masala Chai",  "Hot aromatic spiced tea",                   "Beverages", 15.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item010", "Cold Coffee",  "Refreshing chilled coffee",                 "Beverages", 40.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item011", "Mango Shake",  "Creamy mango milkshake",                    "Beverages", 50.0, R.drawable.ic_food_placeholder));

        // Desserts
        items.add(new FoodItem("item012", "Gulab Jamun",  "Sweet syrup-soaked milk balls",             "Desserts",  30.0, R.drawable.ic_food_placeholder));
        items.add(new FoodItem("item013", "Brownie",      "Rich chocolate walnut brownie",             "Desserts",  45.0, R.drawable.ic_food_placeholder));

        return items;
    }

    public static List<Order> getDummyOrders() {
        List<Order> orders = new ArrayList<>();

        List<CartItem> items1 = new ArrayList<>();
        items1.add(new CartItem(
                new FoodItem("item001", "Samosa", "", "", 15.0, 0), 2));
        orders.add(new Order("#1023", "24 Oct, 12:30 PM", items1, 30.0, "Completed"));

        List<CartItem> items2 = new ArrayList<>();
        items2.add(new CartItem(
                new FoodItem("item005", "Rajma Rice", "", "", 70.0, 0), 1));
        orders.add(new Order("#1022", "23 Oct, 01:15 PM", items2, 70.0, "Completed"));

        List<CartItem> items3 = new ArrayList<>();
        items3.add(new CartItem(
                new FoodItem("item010", "Cold Coffee", "", "", 40.0, 0), 2));
        orders.add(new Order("#1021", "22 Oct, 11:00 AM", items3, 80.0, "Cancelled"));

        List<CartItem> items4 = new ArrayList<>();
        items4.add(new CartItem(
                new FoodItem("item002", "Vada Pav", "", "", 20.0, 0), 3));
        orders.add(new Order("#1020", "21 Oct, 04:45 PM", items4, 60.0, "Completed"));

        List<CartItem> items5 = new ArrayList<>();
        items5.add(new CartItem(
                new FoodItem("item008", "Veg Thali", "", "", 90.0, 0), 1));
        orders.add(new Order("#1019", "20 Oct, 01:00 PM", items5, 90.0, "Completed"));

        return orders;
    }
}