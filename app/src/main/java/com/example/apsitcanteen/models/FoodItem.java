package com.example.apsitcanteen.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class FoodItem {
    private String id;
    private String name;
    private String description;
    private String category;
    private double price;
    private String imageUrl;
    private boolean available;

    public FoodItem() {}

    public FoodItem(String id, String name, String description, String category, double price, String imageUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    // Constructor for dummy data/local resources
    public FoodItem(String id, String name, String description, String category, double price, int imageResourceId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = String.valueOf(imageResourceId);
        this.available = true;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("available")
    public boolean isAvailable() { return available; }
    
    @PropertyName("available")
    public void setAvailable(boolean available) { this.available = available; }
}
