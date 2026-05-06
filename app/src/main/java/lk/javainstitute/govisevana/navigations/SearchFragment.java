package lk.javainstitute.govisevana.navigations;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.ProductAdapter;
import lk.javainstitute.govisevana.model.ProductModel;

public class SearchFragment extends Fragment {

    private EditText searchInput;
    private ImageView backIcon, searchIcon;
    private RecyclerView searchRecyclerView;
    private ProductAdapter productAdapter;
    private List<ProductModel> productList = new ArrayList<>();
    private FirebaseFirestore db;

    private TextView sortByPrice;
    private boolean isAscending = true;

    public SearchFragment() {
        // Default constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        backIcon = view.findViewById(R.id.backicon);
        searchInput = view.findViewById(R.id.searchInput);
        searchIcon = view.findViewById(R.id.searchIcon);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        db = FirebaseFirestore.getInstance();


        searchRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), productList);
        searchRecyclerView.setAdapter(productAdapter);


        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            }
        });


        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchInput.getText().toString().trim();
                searchProducts(query);
            }
        });


        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                searchProducts(searchInput.getText().toString().trim());
                return true;
            }
            return false;
        });

        sortByPrice = view.findViewById(R.id.sortByPrice);
        sortByPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSortOrder();
            }
        });

        return view;
    }

    private void toggleSortOrder() {
        isAscending = !isAscending;


        int icon = isAscending ? R.drawable.ic_arrow_drop_up_24 : R.drawable.ic_arrow_drop_down_24;
        sortByPrice.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);


        Collections.sort(productList, (p1, p2) -> isAscending
                ? Double.compare(p1.getPrice(), p2.getPrice())
                : Double.compare(p2.getPrice(), p1.getPrice()));

        productAdapter.notifyDataSetChanged();
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            productList.clear();
            productAdapter.notifyDataSetChanged();
            return;
        }

        String searchQuery = query.toLowerCase();

        db.collection("products")
                .whereEqualTo("approved", true)
                .orderBy("price", isAscending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ProductModel product = document.toObject(ProductModel.class);
                            if (product.getTitle().toLowerCase().contains(searchQuery)) {
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Search failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Ensure bottom navigation is visible when exiting SearchFragment
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }
    }

}
