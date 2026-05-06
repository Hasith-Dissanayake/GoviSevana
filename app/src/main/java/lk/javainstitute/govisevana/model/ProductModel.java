package lk.javainstitute.govisevana.model;

import java.util.List;

public class ProductModel {
    private String productId;
    private String title;
    private String description;
    private int quantity;
    private double price;
    private List<String> imageUrls;
    private String farmerName;
    private String farmerPhone;
    private boolean isApproved;

    private String lowercaseTitle;


    public ProductModel() {

    }

    public ProductModel(String productId, String title, String description, int quantity, double price, List<String> imageUrls, String farmerName, String farmerPhone, boolean isApproved) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.imageUrls = imageUrls;
        this.farmerName = farmerName;
        this.farmerPhone = farmerPhone;
        this.isApproved = isApproved;

        this.lowercaseTitle = title.toLowerCase();
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerPhone() {
        return farmerPhone;
    }

    public void setFarmerPhone(String farmerPhone) {
        this.farmerPhone = farmerPhone;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}
