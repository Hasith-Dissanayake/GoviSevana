package lk.javainstitute.govisevana.navigations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.CartAdapter;
import lk.javainstitute.govisevana.model.CartItemModel;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;

public class CartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItemModel> cartItemList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView cartTotalPrice;
    private String userPhone;
    private Button checkoutButton;

    public CartFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cartTotalPrice = view.findViewById(R.id.cartTotalPrice);
        checkoutButton = view.findViewById(R.id.checkoutButton);

        db = FirebaseFirestore.getInstance();
        SharedPreferenceHelper preferenceHelper = new SharedPreferenceHelper(requireContext());

        userPhone = preferenceHelper.getUserPhone();

        if (userPhone == null || userPhone.equals("Unknown Number")) {
            Toast.makeText(requireContext(), "Please log in to view cart", Toast.LENGTH_SHORT).show();
        } else {
            loadCartItems();
        }


        cartAdapter = new CartAdapter(getContext(), cartItemList, this);
        cartRecyclerView.setAdapter(cartAdapter);


        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToCheckout();
            }
        });

        return view;
    }

    private void loadCartItems() {
        db.collection("cart").document(userPhone).collection("items")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        cartItemList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            CartItemModel cartItem = document.toObject(CartItemModel.class);
                            cartItemList.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                        updateTotalPrice();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();

                    }
                });
    }


    public void updateTotalPrice() {
        double total = 0.0;
        for (CartItemModel item : cartItemList) {
            total += item.getPrice() * item.getQuantity();
        }
        cartTotalPrice.setText("Total: Rs " + total);
    }


    private void proceedToCheckout() {
        if (cartItemList.isEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        CheckoutFragment checkoutFragment = new CheckoutFragment();

        Bundle args = new Bundle();
        args.putDouble("totalAmount", calculateTotal());
        args.putParcelableArrayList("cartItems", new ArrayList<>(cartItemList)); // Pass cart items
        checkoutFragment.setArguments(args);

        transaction.replace(R.id.fragment_container, checkoutFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    private double calculateTotal() {
        double total = 0.0;
        for (CartItemModel item : cartItemList) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
}
