package lk.javainstitute.govisevana.navigations;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.govisevana.MainActivity;
import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.BannerAdapter;
import lk.javainstitute.govisevana.adapter.ProductAdapter;
import lk.javainstitute.govisevana.helper.DatabaseHelper;
import lk.javainstitute.govisevana.helper.ShakeSensorHelper;
import lk.javainstitute.govisevana.model.BannerModel;
import lk.javainstitute.govisevana.model.ProductModel;

public class HomeFragment extends Fragment {

    private EditText searchInput;
    private BottomNavigationView bottomNavigationView;
    private ViewPager2 bannerViewPager;
    private RecyclerView recommendedRecyclerView;
    private FirebaseFirestore db;
    private BannerAdapter bannerAdapter;
    private ProductAdapter productAdapter;
    private List<String> bannerImages = new ArrayList<>();
    private List<ProductModel> productList = new ArrayList<>();
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentBannerIndex = 0;
    private DatabaseHelper databaseHelper;
    private ShakeSensorHelper shakeSensorHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        searchInput = view.findViewById(R.id.searchInput);
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        recommendedRecyclerView = view.findViewById(R.id.recommendedRecyclerView);


        if (getActivity().findViewById(R.id.bottom_navigation) != null) {
            bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        }


        db = FirebaseFirestore.getInstance();
        databaseHelper = new DatabaseHelper(getContext());


        bannerAdapter = new BannerAdapter(getContext(), bannerImages);
        bannerViewPager.setAdapter(bannerAdapter);


        recommendedRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), productList);
        recommendedRecyclerView.setAdapter(productAdapter);


        loadBannersFromFirestore();
        loadRecommendedProducts();


        searchInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.clearFocus();
                navigateToSearchFragment();
            }
        });

        shakeSensorHelper = new ShakeSensorHelper(requireContext(), new ShakeSensorHelper.ShakeListener() {
            @Override
            public void onShake() {
                Toast.makeText(requireContext(), "Shake detected! Refreshing products...", Toast.LENGTH_SHORT).show();
                loadRecommendedProducts();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        shakeSensorHelper.registerSensor();

        if (getActivity() != null && getActivity() instanceof MainActivity) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        shakeSensorHelper.unregisterSensor();
    }

    private void loadRecommendedProducts() {
        CollectionReference productsCollection = db.collection("products");

        productsCollection.whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ProductModel product = document.toObject(ProductModel.class);
                            product.setProductId(document.getId());


                            String userId = product.getFarmerPhone();

                            db.collection("users").document(userId).get()
                                    .addOnSuccessListener(userDocument -> {
                                        if (userDocument.exists() && userDocument.getBoolean("active") != null &&
                                                userDocument.getBoolean("active")) {

                                            productList.add(product);


                                            databaseHelper.insertProduct(product);


                                            productAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to verify user", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
                );
    }


    private void loadBannersFromFirestore() {
        CollectionReference bannersCollection = db.collection("banners");
        bannersCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        bannerImages.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            BannerModel banner = document.toObject(BannerModel.class);
                            if (banner != null && banner.getImageurl() != null) {
                                bannerImages.add(banner.getImageurl());
                            }
                        }
                        if (!bannerImages.isEmpty()) {
                            bannerAdapter.notifyDataSetChanged();
                            startAutoScroll();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load banners", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void startAutoScroll() {
        if (bannerHandler == null) {
            bannerHandler = new Handler(Looper.getMainLooper());
        }
        bannerRunnable = () -> {
            if (!bannerImages.isEmpty()) {
                if (currentBannerIndex >= bannerImages.size()) {
                    currentBannerIndex = 0;
                }
                bannerViewPager.setCurrentItem(currentBannerIndex++, true);
                bannerHandler.postDelayed(bannerRunnable, 5000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }


    private void navigateToSearchFragment() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }

        Fragment searchFragment = new SearchFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerHandler != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}
