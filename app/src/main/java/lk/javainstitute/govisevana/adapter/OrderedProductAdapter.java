package lk.javainstitute.govisevana.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.CartItemModel;

public class OrderedProductAdapter extends RecyclerView.Adapter<OrderedProductAdapter.ViewHolder> {

    private Context context;
    private List<CartItemModel> orderedProducts;

    public OrderedProductAdapter(Context context, List<CartItemModel> orderedProducts) {
        this.context = context;
        this.orderedProducts = orderedProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemModel product = orderedProducts.get(position);

        holder.productName.setText(product.getTitle());
        holder.productPrice.setText("Rs " + product.getPrice());
        holder.productQuantity.setText("Qty: " + product.getQuantity());

        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return orderedProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
        }
    }
}
