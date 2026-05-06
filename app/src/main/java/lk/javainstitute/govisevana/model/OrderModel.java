package lk.javainstitute.govisevana.model;

import java.util.List;

public class OrderModel {
    private String orderId;
    private String userPhone;
    private String fullName;
    private String city;
    private String address;
    private double totalAmount;
    private String status;

    private String trackingNumber;
    private long timestamp;
    private List<CartItemModel> items;

    public OrderModel() {}

    public OrderModel(String orderId, String userPhone, String fullName, String city, String address,
                      double totalAmount, String status,String trackingNumber, long timestamp, List<CartItemModel> items) {
        this.orderId = orderId;
        this.userPhone = userPhone;
        this.fullName = fullName;
        this.city = city;
        this.address = address;
        this.totalAmount = totalAmount;
        this.status = status;
        this.trackingNumber = trackingNumber;
        this.timestamp = timestamp;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<CartItemModel> getItems() {
        return items;
    }
}
