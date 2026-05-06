package lk.javainstitute.govisevana.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.CartItemModel;
import lk.javainstitute.govisevana.navigations.CartFragment;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context context;
    private List<CartItemModel> cartItems;
    private FirebaseFirestore db;
    private String userPhone;
    private CartFragment cartFragment;
    private boolean isCheckout;


    public CartAdapter(Context context, List<CartItemModel> cartItems, CartFragment cartFragment) {
        this.context = context;
        this.cartItems = cartItems;
        this.cartFragment = cartFragment;
        this.isCheckout = false;
        this.db = FirebaseFirestore.getInstance();
        SharedPreferenceHelper preferenceHelper = new SharedPreferenceHelper(context);
        this.userPhone = preferenceHelper.getUserPhone();
    }


    public CartAdapter(Context context, List<CartItemModel> cartItems, boolean isCheckout) {
        this.context = context;
        this.cartItems = cartItems;
        this.isCheckout = isCheckout;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemModel item = cartItems.get(position);

        holder.title.setText(item.getTitle());
        holder.price.setText("Rs " + item.getPrice());
        holder.quantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context).load(item.getImageUrl()).into(holder.image);


        if (isCheckout) {
            holder.increaseQuantity.setVisibility(View.GONE);
            holder.decreaseQuantity.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.GONE);
        } else {
            holder.increaseQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateQuantity(item, holder, 1);
                }
            });

            holder.decreaseQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.getQuantity() > 1) {
                        updateQuantity(item, holder, -1);
                    } else {
                        Toast.makeText(context, "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeFromCart(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, quantity;
        Button increaseQuantity, decreaseQuantity, removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cartImage);
            title = itemView.findViewById(R.id.cartTitle);
            price = itemView.findViewById(R.id.cartPrice);
            quantity = itemView.findViewById(R.id.cartQuantity);
            increaseQuantity = itemView.findViewById(R.id.increaseQuantity);
            decreaseQuantity = itemView.findViewById(R.id.decreaseQuantity);
            removeButton = itemView.findViewById(R.id.removeFromCartButton);
        }
    }

    private void updateQuantity(CartItemModel item, ViewHolder holder, int change) {
        int newQuantity = item.getQuantity() + change;
        db.collection("cart").document(userPhone).collection("items")
                .document(item.getProductId())
                .update("quantity", newQuantity)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        item.setQuantity(newQuantity);
                        holder.quantity.setText(String.valueOf(newQuantity));
                        cartFragment.updateTotalPrice();
                        Toast.makeText(context, "Quantity updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeFromCart(CartItemModel item) {
        db.collection("cart").document(userPhone).collection("items")
                .document(item.getProductId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cartItems.remove(item);
                        notifyDataSetChanged();
                        cartFragment.updateTotalPrice();
                        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
