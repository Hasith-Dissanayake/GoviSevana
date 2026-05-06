package lk.javainstitute.govisevana.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.OrderModel;

public class FarmerOrdersAdapter extends RecyclerView.Adapter<FarmerOrdersAdapter.ViewHolder> {

    private Context context;
    private List<OrderModel> orderList;
    private FirebaseFirestore db;

    public FarmerOrdersAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farmer_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        if (holder.orderId != null)
            holder.orderId.setText("Order ID: " + order.getOrderId());

        if (holder.customerName != null)
            holder.customerName.setText("Customer: " + (order.getFullName() != null ? order.getFullName() : "N/A"));

        if (holder.customerCity != null)
            holder.customerCity.setText("City: " + (order.getCity() != null ? order.getCity() : "N/A"));

        if (holder.customerAddress != null)
            holder.customerAddress.setText("Address: " + (order.getAddress() != null ? order.getAddress() : "N/A"));

        if (holder.totalAmount != null)
            holder.totalAmount.setText("Total: Rs " + order.getTotalAmount());

        if (holder.status != null)
            holder.status.setText("Status: " + order.getStatus());

        if (holder.trackingNumber != null) {
            String trackingNum = order.getTrackingNumber();
            if (trackingNum != null && !trackingNum.isEmpty()) {
                holder.trackingNumber.setVisibility(View.VISIBLE);
                holder.trackingNumber.setText("Tracking Number: " + trackingNum);


                holder.trackingNumber.setOnClickListener(view -> {
                    String trackingUrl = "https://parcelsapp.com/en/tracking/" + trackingNum;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trackingUrl));
                    context.startActivity(browserIntent);
                });
            } else {
                holder.trackingNumber.setVisibility(View.GONE);
            }
        }

        if (holder.timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            holder.timestamp.setText("Date: " + sdf.format(order.getTimestamp()));
        }

        OrderedProductAdapter productAdapter = new OrderedProductAdapter(context, order.getItems());
        holder.orderedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.orderedProductsRecyclerView.setAdapter(productAdapter);

        holder.addTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTrackingNumberDialog(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void showTrackingNumberDialog(OrderModel order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Tracking Number");

        final EditText input = new EditText(context);
        input.setHint("Tracking Number");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String trackingNumber = input.getText().toString().trim();
            if (!TextUtils.isEmpty(trackingNumber)) {
                updateTrackingNumber(order.getOrderId(), trackingNumber);
            } else {
                Toast.makeText(context, "Tracking Number cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateTrackingNumber(String orderId, String trackingNumber) {
        db.collection("orders").document(orderId)
                .update("trackingNumber", trackingNumber, "status", "Shipping")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Tracking Number Updated!", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to update tracking number", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, customerName, customerCity, customerAddress, totalAmount, status, trackingNumber, timestamp;
        RecyclerView orderedProductsRecyclerView;
        Button addTrackingButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderId = itemView.findViewById(R.id.orderId);
            customerName = itemView.findViewById(R.id.customerName);
            customerCity = itemView.findViewById(R.id.customerCity);
            customerAddress = itemView.findViewById(R.id.customerAddress);
            totalAmount = itemView.findViewById(R.id.totalAmount);
            status = itemView.findViewById(R.id.orderStatus);
            trackingNumber = itemView.findViewById(R.id.trackingNumber);
            timestamp = itemView.findViewById(R.id.orderTimestamp);
            orderedProductsRecyclerView = itemView.findViewById(R.id.orderedProductsRecyclerView);
            addTrackingButton = itemView.findViewById(R.id.updateStatusButton);
        }
    }
}
