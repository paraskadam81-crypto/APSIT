package com.example.apsitcanteen.utils;

import com.example.apsitcanteen.models.CartItem;
import com.example.apsitcanteen.models.FoodItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(FoodItem foodItem) {
        for (CartItem item : cartItems) {
            if (item.getFoodItem().getId().equals(foodItem.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(foodItem, 1));
    }

    public void removeItem(FoodItem foodItem) {
        cartItems.removeIf(item -> item.getFoodItem().getId().equals(foodItem.getId()));
    }

    public void increaseQuantity(FoodItem foodItem) {
        for (CartItem item : cartItems) {
            if (item.getFoodItem().getId().equals(foodItem.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
    }

    public void decreaseQuantity(FoodItem foodItem) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getFoodItem().getId().equals(foodItem.getId())) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    cartItems.remove(i);
                }
                return;
            }
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
