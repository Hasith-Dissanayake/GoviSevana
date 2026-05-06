package lk.javainstitute.govisevana.navigations;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import lk.javainstitute.govisevana.helper.CartHelper;
import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.ImageSliderAdapter;
import lk.javainstitute.govisevana.model.CartItemModel;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;


public class SingleProductFragment extends Fragment {

    private ViewPager2 productImageViewPager;
    private TextView productTitle, productDescription, productPrice, farmerName, farmerPhone, availableQuantity;
    private ArrayList<String> imageUrls;
    private EditText searchInput, selectedQuantity;
    private BottomNavigationView bottomNavigationView;
    private ImageView farmerImage, backIcon , messageicon;
    private Button decreaseQuantity, increaseQuantity, addToCartButton;
    private LinearLayout farmernotview, farmernotview2;
    private SharedPreferenceHelper preferenceHelper;
    private int maxQuantity = 1;
    private int currentQuantity = 1;
    private static final int CALL_PERMISSION_REQUEST_CODE = 1;
    private CartHelper cartHelper;
    private String productId, imageUrl;
    private double price;
    private FirebaseFirestore db;

    public SingleProductFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_product, container, false);


        bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }

        db = FirebaseFirestore.getInstance();

        productImageViewPager = view.findViewById(R.id.productImageViewPager);
        productTitle = view.findViewById(R.id.productTitle);
        productDescription = view.findViewById(R.id.productDescription);
        productPrice = view.findViewById(R.id.productPrice);
        farmerName = view.findViewById(R.id.farmerName);
        farmerPhone = view.findViewById(R.id.farmerPhone);
        availableQuantity = view.findViewById(R.id.availableQuantity);
        searchInput = view.findViewById(R.id.searchInput);
        selectedQuantity = view.findViewById(R.id.selectedQuantity);
        farmerImage = view.findViewById(R.id.farmerImage);
        backIcon = view.findViewById(R.id.backicon);
        decreaseQuantity = view.findViewById(R.id.decreaseQuantity);
        increaseQuantity = view.findViewById(R.id.increaseQuantity);

        addToCartButton = view.findViewById(R.id.addToCartButton);
        messageicon = view.findViewById(R.id.messageicon);


        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            }
        });


        if (getArguments() != null) {
            productId = getArguments().getString("productId", null);
            String title = getArguments().getString("title", "No Title");
            String description = getArguments().getString("description", "No Description");
            double price = getArguments().getDouble("price", 0.0);
            String farmer = getArguments().getString("farmerName", "Unknown");
            String phone = getArguments().getString("farmerPhone", "No Contact");
            int quantity = getArguments().getInt("quantity", 1);
            imageUrls = getArguments().getStringArrayList("imageUrls");

            Log.d("SingleProduct", "Product ID: " + productId);


            productTitle.setText(title);
            productDescription.setText(description);
            productPrice.setText("Rs " + price);
            farmerName.setText("Farmer: " + farmer);
            farmerPhone.setText("Phone: " + phone);
            availableQuantity.setText("Available: " + quantity);
            maxQuantity = quantity;
            selectedQuantity.setText(String.valueOf(currentQuantity));


            if (imageUrls != null && !imageUrls.isEmpty()) {
                ImageSliderAdapter adapter = new ImageSliderAdapter(getContext(), imageUrls);
                productImageViewPager.setAdapter(adapter);
            }



            loadFarmerImage();

        }


        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateQuantity(-1);
            }
        });
        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateQuantity(1);
            }
        });


        searchInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.clearFocus();
                navigateToSearchFragment();
            }
        });

        farmernotview = view.findViewById(R.id.farmernotview1);
        farmernotview2 = view.findViewById(R.id.farmernotview2);


        preferenceHelper = new SharedPreferenceHelper(requireContext());


        if (!preferenceHelper.isLoggedIn()) {

            farmernotview.setVisibility(View.GONE);
            farmernotview2.setVisibility(View.GONE);
            messageicon.setVisibility(View.GONE);
        } else if (preferenceHelper.getUserType().equals("Farmer")) {

            farmernotview2.setVisibility(View.GONE);
            farmernotview.setVisibility(View.GONE);
            messageicon.setVisibility(View.GONE);
        } else {

            farmernotview.setVisibility(View.VISIBLE);
            farmernotview2.setVisibility(View.VISIBLE);
        }

        farmerPhone = view.findViewById(R.id.farmerPhone);

        if (getArguments() != null) {
            String phone = getArguments().getString("farmerPhone", "No Contact");

            farmerPhone.setText("Phone: " + phone);


            farmerPhone.setOnClickListener(v -> {
                if (!phone.equals("No Contact")) {
                    makePhoneCall(phone);
                } else {
                    Toast.makeText(getContext(), "No phone number available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        cartHelper = new CartHelper(requireContext());
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });

        messageicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatFragment();
            }
        });


        return view;
    }

    private void openChatFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("farmerPhone", farmerPhone.getText().toString().replace("Phone: ", "").trim());
        bundle.putString("farmerName", farmerName.getText().toString().replace("Farmer: ", "").trim());

        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }


    private void loadFarmerImage() {
        if (getArguments() != null) {
            String farmerPhoneNumber = getArguments().getString("farmerPhone", "");

            if (farmerPhoneNumber == null || farmerPhoneNumber.trim().isEmpty()) {
                Log.e("FarmerImage", "Farmer phone number is empty!");
                return;
            }

            db.collection("users").document(farmerPhoneNumber)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String imageUrl = documentSnapshot.getString("profileImageUrl");

                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Glide.with(requireContext())
                                            .load(imageUrl)
                                            .placeholder(R.drawable.farmer)
                                            .error(R.drawable.farmer)
                                            .into(farmerImage);
                                    Log.d("FarmerImage", "Profile image loaded: " + imageUrl);
                                } else {
                                    Log.e("FarmerImage", "No profile image found, using default.");
                                }
                            } else {
                                Log.e("FarmerImage", "Farmer document does not exist.");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FarmerImage", "Failed to load image", e);
                        }
                    });
        }
    }




    private void addToCart() {
        int quantity = Integer.parseInt(selectedQuantity.getText().toString());
        if (quantity <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (productId == null || productTitle.getText().toString().isEmpty() || imageUrls == null || imageUrls.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Missing product details", Toast.LENGTH_SHORT).show();
            Log.e("AddToCart", "Error: Missing product details");
            return;
        }

        double productPriceValue;
        try {
            productPriceValue = Double.parseDouble(productPrice.getText().toString().replace("Rs ", "").trim());
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Error: Invalid price", Toast.LENGTH_SHORT).show();
            Log.e("AddToCart", "Error: Invalid price format", e);
            return;
        }

        String imageUrlValue = imageUrls.get(0);
        String farmerId = getArguments().getString("farmerPhone", "Unknown Farmer");


        CartItemModel cartItem = new CartItemModel(
                productId,
                productTitle.getText().toString(),
                productPriceValue,
                quantity,
                imageUrlValue,
                farmerId
        );

        cartHelper.addToCart(cartItem, requireContext());
    }




    private void updateQuantity(int change) {
        int newQuantity = currentQuantity + change;
        if (newQuantity >= 1 && newQuantity <= maxQuantity) {
            currentQuantity = newQuantity;
            selectedQuantity.setText(String.valueOf(currentQuantity));
        }
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {

            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (getArguments() != null) {
                    String phone = getArguments().getString("farmerPhone", "No Contact");
                    makePhoneCall(phone);
                }
            } else {

                Toast.makeText(getContext(), "Call permission is required to make a phone call.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToSearchFragment() {
        bottomNavigationView.setVisibility(View.GONE);
        Fragment searchFragment = new SearchFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();
    }


}
