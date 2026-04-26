package com.example.apsitcanteen.admin;

import java.io.Serializable;

/**
 * Model class for Orders in the Admin Panel.
 */
public class AdminOrder implements Serializable {

    private String orderId;
    private String studentName;
    private String studentId;
    private String itemsSummary;
    private double totalAmount;
    private String paymentMode;
    private String status; // Pending, Preparing, Ready, Completed, Cancelled
    private String orderTime;
    private String date;
    private String trackingNote;

    // ✅ Required empty constructor for Firebase
    public AdminOrder() {}

    public AdminOrder(String orderId, String studentName, String studentId,
                      String itemsSummary, double totalAmount, String paymentMode,
                      String status, String orderTime, String date) {
        this.orderId      = orderId;
        this.studentName  = studentName;
        this.studentId    = studentId;
        this.itemsSummary = itemsSummary;
        this.totalAmount  = totalAmount;
        this.paymentMode  = paymentMode;
        this.status       = status;
        this.orderTime    = orderTime;
        this.date         = date;
        this.trackingNote = "";
    }

    // Getters and Setters (unchanged)
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getItemsSummary() { return itemsSummary; }
    public void setItemsSummary(String itemsSummary) { this.itemsSummary = itemsSummary; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTrackingNote() { return trackingNote; }
    public void setTrackingNote(String trackingNote) { this.trackingNote = trackingNote; }
}