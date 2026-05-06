package lk.javainstitute.govisevana.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHelper {
    private static final String PREF_NAME = "GoviSevanaPreferences";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private static final String KEY_USER_TYPE = "userType";

    private static final String KEY_FARMER_NAME = "farmerName";
    private static final String KEY_FARMER_PHONE = "farmerPhone";

    private static final String KEY_PROFILE_IMAGE = "profileImageUrl";

    private SharedPreferences preferences;

    public SharedPreferenceHelper(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean isFirstLaunch) {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch).apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        preferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public void setUserType(String userType) {
        preferences.edit().putString(KEY_USER_TYPE, userType).apply();
    }

    public String getUserType() {
        return preferences.getString(KEY_USER_TYPE, "Buyer");
    }


    public void setUserName(String farmerName) {
        preferences.edit().putString(KEY_FARMER_NAME, farmerName).apply();
    }


    public String getUserName() {
        return preferences.getString(KEY_FARMER_NAME, "Unknown Farmer");
    }


    public void setUserPhone(String farmerPhone) {
        preferences.edit().putString(KEY_FARMER_PHONE, farmerPhone).apply();
    }


    public String getUserPhone() {
        return preferences.getString(KEY_FARMER_PHONE, "Unknown Number");
    }

    public void setUserProfileImage(String imageUrl) {
        preferences.edit().putString(KEY_PROFILE_IMAGE, imageUrl).apply();
    }

    public String getUserProfileImage() {
        return preferences.getString(KEY_PROFILE_IMAGE, "");
    }
}
