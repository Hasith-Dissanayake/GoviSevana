package lk.javainstitute.govisevana.navigations;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.ViewProductAdapter;
import lk.javainstitute.govisevana.model.ProductModel;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;

public class ViewMyProductsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ViewProductAdapter productAdapter;
    private List<ProductModel> productList;
    private FirebaseFirestore db;
    private SharedPreferenceHelper preferenceHelper;
    private ProgressBar progressBar;
    private TextView noProductsText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_my_products, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMyProducts);
        progressBar = view.findViewById(R.id.progressBarMyProducts);
        noProductsText = view.findViewById(R.id.noProductsText);

        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(requireContext());
        productList = new ArrayList<>();
        productAdapter = new ViewProductAdapter(requireContext(), productList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(productAdapter);

        loadMyProducts();

        return view;
    }

    private void loadMyProducts() {
        progressBar.setVisibility(View.VISIBLE);
        String farmerPhone = preferenceHelper.getUserPhone();

        db.collection("products")
                .whereEqualTo("farmerPhone", farmerPhone)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            productList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ProductModel product = document.toObject(ProductModel.class);
                                productList.add(product);
                            }
                            if (productList.isEmpty()) {
                                noProductsText.setVisibility(View.VISIBLE);
                            } else {
                                noProductsText.setVisibility(View.GONE);
                            }
                            productAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Failed to load products!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
