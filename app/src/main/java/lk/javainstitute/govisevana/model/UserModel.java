package lk.javainstitute.govisevana.model;

import com.google.firebase.Timestamp;

public class UserModel {

    private String phone;
    private String fullname;
    private String password;
    private String usertype;
    private boolean isActive;
    private Timestamp createdTimestamp;

    public UserModel() {
    }

    public UserModel(String phone, String fullname, String password, String usertype, boolean isActive, Timestamp createdTimestamp) {
        this.phone = phone;
        this.fullname = fullname;
        this.password = password;
        this.usertype = usertype;
        this.isActive = isActive;
        this.createdTimestamp = createdTimestamp;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
