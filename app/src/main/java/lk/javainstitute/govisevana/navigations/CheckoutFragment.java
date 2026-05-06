package lk.javainstitute.govisevana.navigations;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.CartAdapter;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;
import lk.javainstitute.govisevana.model.CartItemModel;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutFragment extends Fragment {

    private EditText fullName, mobileNumber, city, address;
    private TextView totalAmountText;
    private Button payNowButton;
    private FirebaseFirestore db;
    private SharedPreferenceHelper preferenceHelper;
    private double totalAmount = 0.0;

    private static final int PAYHERE_REQUEST = 11001;

    private RecyclerView checkoutRecyclerView;
    private CartAdapter checkoutAdapter;
    private List<CartItemModel> checkoutItems = new ArrayList<>();

    private String orderId;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        fullName = view.findViewById(R.id.fullName);
        mobileNumber = view.findViewById(R.id.mobileNumber);
        city = view.findViewById(R.id.city);
        address = view.findViewById(R.id.address);
        totalAmountText = view.findViewById(R.id.totalAmountText);
        payNowButton = view.findViewById(R.id.payNowButton);
        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(requireContext());

        if (getArguments() != null) {
            totalAmount = getArguments().getDouble("totalAmount", 0.0);
        }

        totalAmountText.setText("Total: Rs " + String.format("%.2f", totalAmount) + "   + Shipping Cost ");
        loadAddressDetails();


        orderId = "ORDER-" + System.currentTimeMillis();


        payNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });

        if (getArguments() != null) {
            totalAmount = getArguments().getDouble("totalAmount", 0.0);
            checkoutItems = getArguments().getParcelableArrayList("cartItems");
        }

        checkoutRecyclerView = view.findViewById(R.id.checkoutRecyclerView);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        checkoutAdapter = new CartAdapter(getContext(), checkoutItems, true);
        checkoutRecyclerView.setAdapter(checkoutAdapter);



        return view;
    }

    private void loadAddressDetails() {
        String userPhone = preferenceHelper.getUserPhone();
        db.collection("addresses").document(userPhone)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            fullName.setText(documentSnapshot.getString("fullName"));
                            mobileNumber.setText(documentSnapshot.getString("mobileNumber"));
                            city.setText(documentSnapshot.getString("city"));
                            address.setText(documentSnapshot.getString("address"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load address!", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void processPayment() {
        String fullNameText = fullName.getText().toString().trim();
        String mobileText = mobileNumber.getText().toString().trim();
        String cityText = city.getText().toString().trim();
        String addressText = address.getText().toString().trim();

        if (fullNameText.isEmpty() || mobileText.isEmpty() || cityText.isEmpty() || addressText.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        saveAddressToFirestore(fullNameText, mobileText, cityText, addressText);
        Toast.makeText(getContext(), "Proceeding to payment...", Toast.LENGTH_SHORT).show();

        initiatePayHerePayment(fullNameText, mobileText, cityText, addressText);
    }

    private void initiatePayHerePayment(String name, String phone, String city, String address) {



        InitRequest req = new InitRequest();
        req.setMerchantId("1226348");       // Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(totalAmount);             // Final Amount to be charged
        req.setOrderId(orderId);        // Unique Reference ID
        req.setItemsDescription("Door bell wireless");  // Item description title
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName(name);
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail("");
        req.getCustomer().setPhone(phone);
        req.getCustomer().getAddress().setAddress(address);
        req.getCustomer().getAddress().setCity(city);
        req.getCustomer().getAddress().setCountry("Sri Lanka");



        Intent intent = new Intent(getContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST); //unique request ID e.g. "11001"
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK) {
                if (response != null && response.isSuccess()) {
                    // Payment was successful
                    Toast.makeText(getContext(), "Payment Successful! Saving order...", Toast.LENGTH_SHORT).show();
                    saveOrderToFirestore();
                } else {
                    Toast.makeText(getContext(), "Payment Failed!", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveOrderToFirestore() {

        String userPhone = preferenceHelper.getUserPhone();
        String fullNameText = fullName.getText().toString().trim();
        String cityText = city.getText().toString().trim();
        String addressText = address.getText().toString().trim();

        if (checkoutItems.isEmpty()) {
            Toast.makeText(getContext(), "No items in cart to save!", Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, List<CartItemModel>> ordersByFarmer = new HashMap<>();
        for (CartItemModel item : checkoutItems) {
            String farmerId = item.getFarmerId();
            if (!ordersByFarmer.containsKey(farmerId)) {
                ordersByFarmer.put(farmerId, new ArrayList<>());
            }
            ordersByFarmer.get(farmerId).add(item);
        }


        for (Map.Entry<String, List<CartItemModel>> entry : ordersByFarmer.entrySet()) {
            String farmerId = entry.getKey();
            List<CartItemModel> itemsForFarmer = entry.getValue();

            double totalAmountForFarmer = 0;
            for (CartItemModel item : itemsForFarmer) {
                totalAmountForFarmer += item.getPrice() * item.getQuantity();
            }

            String orderId = "ORDER-" + System.currentTimeMillis() + "-" + farmerId;

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("userPhone", userPhone);
            orderData.put("fullName", fullNameText);
            orderData.put("city", cityText);
            orderData.put("address", addressText);
            orderData.put("farmerId", farmerId);
            orderData.put("totalAmount", totalAmountForFarmer);
            orderData.put("status", "Pending");
            orderData.put("timestamp", System.currentTimeMillis());
            orderData.put("trackingNumber", null);

            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (CartItemModel item : itemsForFarmer) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("productId", item.getProductId());
                itemData.put("title", item.getTitle());
                itemData.put("price", item.getPrice());
                itemData.put("quantity", item.getQuantity());
                itemData.put("imageUrl", item.getImageUrl());
                itemData.put("farmerId", item.getFarmerId());
                itemsList.add(itemData);

                updateProductStock(item.getProductId(), item.getQuantity());
            }
            orderData.put("items", itemsList);

            db.collection("orders").document(orderId)
                    .set(orderData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Order saved for Farmer: " + farmerId, Toast.LENGTH_SHORT).show();
                        clearCartAfterOrder(userPhone);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save order!", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateProductStock(String productId, int purchasedQuantity) {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentStock = documentSnapshot.getLong("quantity");
                        if (currentStock != null && currentStock >= purchasedQuantity) {
                            long newStock = currentStock - purchasedQuantity;


                            db.collection("products").document(productId)
                                    .update("quantity", newStock)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Stock updated for product: " + productId))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update stock", e));
                        } else {
                            Log.e(TAG, "Not enough stock for product: " + productId);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching product stock", e));
    }


    private void clearCartAfterOrder(String userPhone) {
        db.collection("cart").document(userPhone).collection("items")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            snapshot.getReference().delete();
                        }
                        Toast.makeText(getContext(), "Cart cleared!", Toast.LENGTH_SHORT).show();

                        navigateToCartFragment();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to clear cart: ", e);
                    }
                });
    }

    private void navigateToCartFragment() {
        Fragment cartFragment = new CartFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, cartFragment)
                .commit();
    }



    private void saveAddressToFirestore(String fullName, String mobileNumber, String city, String address) {
        String userPhone = preferenceHelper.getUserPhone();
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("fullName", fullName);
        addressData.put("mobileNumber", mobileNumber);
        addressData.put("city", city);
        addressData.put("address", address);

        db.collection("addresses").document(userPhone)
                .set(addressData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Address saved successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to save address!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
