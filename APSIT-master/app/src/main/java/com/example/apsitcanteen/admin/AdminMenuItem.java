package com.example.apsitcanteen.admin;

import java.io.Serializable;

/**
 * Model class for Menu Items in the Admin Panel.
 * Implements Serializable to allow passing between activities via Intent.
 */
public class AdminMenuItem implements Serializable {

    private String id;        // ✅ Changed from int to String (Firebase key)
    private String name;
    private String category;
    private double price;
    private String description;
    private int stock;
    private boolean isAvailable;

    // ✅ Required empty constructor for Firebase
    public AdminMenuItem() {}

    // ✅ Updated constructor with String id
    public AdminMenuItem(String id, String name, String category, double price,
                         String description, int stock, boolean isAvailable) {
        this.id          = id;
        this.name        = name;
        this.category    = category;
        this.price       = price;
        this.description = description;
        this.stock       = stock;
        this.isAvailable = isAvailable;
    }

    // ✅ Updated Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}