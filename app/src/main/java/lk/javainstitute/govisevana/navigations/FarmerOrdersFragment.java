package lk.javainstitute.govisevana.navigations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.FarmerOrdersAdapter;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;
import lk.javainstitute.govisevana.model.OrderModel;

public class FarmerOrdersFragment extends Fragment {

    private RecyclerView farmerOrdersRecyclerView;
    private FarmerOrdersAdapter farmerOrdersAdapter;
    private List<OrderModel> orderList = new ArrayList<>();
    private FirebaseFirestore db;
    private SharedPreferenceHelper preferenceHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farmer_orders, container, false);

        farmerOrdersRecyclerView = view.findViewById(R.id.farmerOrdersRecyclerView);
        farmerOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(requireContext());

        loadFarmerOrders();

        farmerOrdersAdapter = new FarmerOrdersAdapter(getContext(), orderList);
        farmerOrdersRecyclerView.setAdapter(farmerOrdersAdapter);

        listenForFarmerOrderUpdates();

        return view;
    }

    private void listenForFarmerOrderUpdates() {
        String farmerId = preferenceHelper.getUserPhone();

        db.collection("orders").whereEqualTo("farmerId", farmerId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(getContext(), "Failed to load farmer orders!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        orderList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            OrderModel order = document.toObject(OrderModel.class);
                            orderList.add(order);
                        }
                        farmerOrdersAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadFarmerOrders() {
        String farmerId = preferenceHelper.getUserPhone();

        db.collection("orders").whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            OrderModel order = document.toObject(OrderModel.class);
                            orderList.add(order);
                        }
                        farmerOrdersAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load farmer orders!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
