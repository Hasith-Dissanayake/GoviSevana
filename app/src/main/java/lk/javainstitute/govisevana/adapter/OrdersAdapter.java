package lk.javainstitute.govisevana.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.OrderModel;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private Context context;
    private List<OrderModel> orderList;

    public OrdersAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.orderId.setText("Order ID: " + order.getOrderId());
        holder.totalAmount.setText("Total: Rs " + order.getTotalAmount());
        holder.status.setText("Status: " + order.getStatus());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        holder.timestamp.setText("Date: " + sdf.format(order.getTimestamp()));

        if (order.getTrackingNumber() != null && !order.getTrackingNumber().isEmpty()) {
            holder.trackingNumber.setVisibility(View.VISIBLE);
            holder.trackingNumber.setText("Tracking Number: " + order.getTrackingNumber());


            holder.trackingNumber.setOnClickListener(view -> {
                String trackingUrl = "https://parcelsapp.com/en/tracking/" + order.getTrackingNumber();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trackingUrl));
                context.startActivity(browserIntent);
            });
        } else {
            holder.trackingNumber.setVisibility(View.GONE);
        }

        OrderedProductAdapter productAdapter = new OrderedProductAdapter(context, order.getItems());
        holder.orderedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.orderedProductsRecyclerView.setAdapter(productAdapter);
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, totalAmount, status, timestamp, trackingNumber;
        RecyclerView orderedProductsRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            totalAmount = itemView.findViewById(R.id.totalAmount);
            status = itemView.findViewById(R.id.orderStatus);
            timestamp = itemView.findViewById(R.id.orderTimestamp);
            trackingNumber = itemView.findViewById(R.id.trackingNumber);
            orderedProductsRecyclerView = itemView.findViewById(R.id.orderedProductsRecyclerView);
        }
    }
}
