package com.example.apsitcanteen.models;

/**
 * Model class for an item in the cart.
 */
public class CartItem {

    private FoodItem foodItem;
    private int quantity;

    // ✅ Required empty constructor for Firebase
    public CartItem() {}

    public CartItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    // ✅ Added setter for foodItem
    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() {
        return foodItem.getPrice() * quantity;
    }
}