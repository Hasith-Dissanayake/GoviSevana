package lk.javainstitute.govisevana.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItemModel implements Parcelable {
    private String productId;
    private String title;
    private double price;
    private int quantity;
    private String imageUrl;
    private String farmerId;

    public CartItemModel() {}

    public CartItemModel(String productId, String title, double price, int quantity, String imageUrl, String farmerId) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.farmerId = farmerId;
    }

    protected CartItemModel(Parcel in) {
        productId = in.readString();
        title = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        imageUrl = in.readString();
        farmerId = in.readString();
    }

    public static final Creator<CartItemModel> CREATOR = new Creator<CartItemModel>() {
        @Override
        public CartItemModel createFromParcel(Parcel in) {
            return new CartItemModel(in);
        }

        @Override
        public CartItemModel[] newArray(int size) {
            return new CartItemModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(title);
        dest.writeDouble(price);
        dest.writeInt(quantity);
        dest.writeString(imageUrl);
        dest.writeString(farmerId);
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }
}
