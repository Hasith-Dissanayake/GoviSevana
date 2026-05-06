package lk.javainstitute.govisevana.model;

public class BannerModel {
    private String imageurl;


    public BannerModel() {
    }


    public BannerModel(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
