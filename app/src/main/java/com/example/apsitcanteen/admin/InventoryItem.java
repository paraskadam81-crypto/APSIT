package com.example.apsitcanteen.admin;

/**
 * Model class for Inventory Items in the Admin Panel.
 */
public class InventoryItem {

    private String id;          // ✅ Changed from int to String (Firebase key)
    private String itemName;
    private String category;
    private int currentStock;
    private int minStock;
    private String unit;
    private double costPrice;
    private String lastRestocked;

    // ✅ Required empty constructor for Firebase
    public InventoryItem() {}

    // ✅ Updated constructor with String id
    public InventoryItem(String id, String itemName, String category, int currentStock,
                         int minStock, String unit, double costPrice, String lastRestocked) {
        this.id            = id;
        this.itemName      = itemName;
        this.category      = category;
        this.currentStock  = currentStock;
        this.minStock      = minStock;
        this.unit          = unit;
        this.costPrice     = costPrice;
        this.lastRestocked = lastRestocked;
    }

    // ✅ Updated Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public String getLastRestocked() { return lastRestocked; }
    public void setLastRestocked(String lastRestocked) { this.lastRestocked = lastRestocked; }
}