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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.OrdersAdapter;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;
import lk.javainstitute.govisevana.model.OrderModel;

public class OrdersFragment extends Fragment {

    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private List<OrderModel> orderList = new ArrayList<>();
    private FirebaseFirestore db;
    private SharedPreferenceHelper preferenceHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(requireContext());

        loadOrders();

        ordersAdapter = new OrdersAdapter(getContext(), orderList);
        ordersRecyclerView.setAdapter(ordersAdapter);

        return view;
    }

    private void loadOrders() {
        String userPhone = preferenceHelper.getUserPhone();

        db.collection("orders").whereEqualTo("userPhone", userPhone)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            OrderModel order = document.toObject(OrderModel.class);
                            orderList.add(order);
                        }
                        ordersAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load orders!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
