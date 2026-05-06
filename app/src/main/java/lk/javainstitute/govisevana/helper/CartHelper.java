package lk.javainstitute.govisevana.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.javainstitute.govisevana.model.CartItemModel;

public class CartHelper {
    private static final String TAG = "CartHelper";
    private final FirebaseFirestore db;
    private final String userPhone;

    public CartHelper(Context context) {
        db = FirebaseFirestore.getInstance();
        SharedPreferenceHelper preferenceHelper = new SharedPreferenceHelper(context);


        userPhone = preferenceHelper.getUserPhone();

        Log.d(TAG, "User phone from SharedPreferences: " + userPhone);

        if (userPhone == null || userPhone.equals("Unknown Number")) {
            Toast.makeText(context, "Please log in to manage the cart", Toast.LENGTH_SHORT).show();
        }
    }


    public void addToCart(CartItemModel cartItem, Context context) {
        if (userPhone == null || userPhone.equals("Unknown Number")) {
            Toast.makeText(context, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cartItem.getProductId() == null) {
            Toast.makeText(context, "Error: Product ID is missing", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: Product ID is NULL!");
            return;
        }

        CollectionReference cartRef = db.collection("cart").document(userPhone).collection("items");

        cartRef.document(cartItem.getProductId()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            int newQuantity = documentSnapshot.getLong("quantity").intValue() + cartItem.getQuantity();
                            cartRef.document(cartItem.getProductId()).update("quantity", newQuantity)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Failed to update cart", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Failed to update cart: ", e);
                                        }
                                    });
                        } else {

                            cartRef.document(cartItem.getProductId()).set(cartItem)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Failed to add to cart: ", e);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error checking cart", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error checking cart: ", e);
                    }
                });
    }



}
