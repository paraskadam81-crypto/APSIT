package com.example.apsitcanteen.models;

import com.google.firebase.firestore.Exclude;
import java.util.List;

public class Order {
    private String orderId;
    private String studentId;
    private String studentName;
    private List<CartItem> items;
    private double totalPrice;
    private String status; // Pending, Accepted, Preparing, Ready, Completed
    private long timestamp;
    private String estimatedTime;
    private String paymentId;

    public Order() {}

    public Order(String orderId, String studentId, String studentName, List<CartItem> items, double totalPrice, String status, long timestamp) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Constructor with Payment ID
    public Order(String orderId, String studentId, String studentName, List<CartItem> items, double totalPrice, String status, long timestamp, String paymentId) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = timestamp;
        this.paymentId = paymentId;
    }

    // Constructor for DummyData
    public Order(String orderId, String studentName, List<CartItem> items, double totalPrice, String status) {
        this.orderId = orderId;
        this.studentName = studentName;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    @Exclude
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}
