package lk.javainstitute.govisevana.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.ArrayList;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.ProductModel;
import lk.javainstitute.govisevana.navigations.AddProductFragment;

public class ViewProductAdapter extends RecyclerView.Adapter<ViewProductAdapter.ViewHolder> {

    private Context context;
    private List<ProductModel> productList;
    private FirebaseFirestore db;

    public ViewProductAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_product, parent, false);
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
        holder.productQuantity.setText("Stock: " + product.getQuantity());


        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProductFragment(product);
            }
        });


        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice, productQuantity;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }


    private void openEditProductFragment(ProductModel product) {
        AddProductFragment addProductFragment = new AddProductFragment();
        Bundle bundle = new Bundle();
        bundle.putString("productId", product.getProductId());
        bundle.putString("title", product.getTitle());
        bundle.putString("description", product.getDescription());
        bundle.putDouble("price", product.getPrice());
        bundle.putString("farmerName", product.getFarmerName());
        bundle.putString("farmerPhone", product.getFarmerPhone());
        bundle.putInt("quantity", product.getQuantity());
        bundle.putStringArrayList("imageUrls", product.getImageUrls() != null ? new ArrayList<>(product.getImageUrls()) : new ArrayList<>());
        addProductFragment.setArguments(bundle);

        ((FragmentActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, addProductFragment)
                .addToBackStack(null)
                .commit();
    }


    private void showDeleteConfirmationDialog(ProductModel product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Product");
        builder.setMessage("Are you sure you want to delete this product?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteProduct(product));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void deleteProduct(ProductModel product) {
        db.collection("products").document(product.getProductId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                        productList.remove(product);
                        notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
