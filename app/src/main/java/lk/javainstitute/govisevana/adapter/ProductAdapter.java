package lk.javainstitute.govisevana.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;
import java.util.ArrayList;
import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.ProductModel;
import lk.javainstitute.govisevana.navigations.SingleProductFragment;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<ProductModel> productList;

    public ProductAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductModel product = productList.get(position);


        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context).load(product.getImageUrls().get(0)).into(holder.productImage);
        } else {
            Glide.with(context).load(R.drawable.placeholder_image).into(holder.productImage);
        }


        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("Rs " + product.getPrice());
        holder.productRating.setText("4.9");
        holder.productSold.setText("| 4.5k Sold");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSingleProductFragment(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice, productRating, productSold;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            productRating = itemView.findViewById(R.id.productRating);
            productSold = itemView.findViewById(R.id.productSold);
        }
    }


    private void openSingleProductFragment(ProductModel product) {
        SingleProductFragment singleProductFragment = new SingleProductFragment();
        Bundle bundle = new Bundle();
        bundle.putString("productId", product.getProductId());
        bundle.putString("title", product.getTitle());
        bundle.putString("description", product.getDescription());
        bundle.putDouble("price", product.getPrice());
        bundle.putString("farmerName", product.getFarmerName());
        bundle.putString("farmerPhone", product.getFarmerPhone());
        bundle.putInt("quantity", product.getQuantity());
        bundle.putStringArrayList("imageUrls", product.getImageUrls() != null ? new ArrayList<>(product.getImageUrls()) : new ArrayList<>());
        singleProductFragment.setArguments(bundle);

        ((FragmentActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, singleProductFragment)
                .addToBackStack(null)
                .commit();
    }
}
