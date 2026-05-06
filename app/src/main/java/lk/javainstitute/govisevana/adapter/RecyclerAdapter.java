package lk.javainstitute.govisevana.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import lk.javainstitute.govisevana.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<Uri> imageUris;
    public RecyclerAdapter(ArrayList<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_single_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .into(holder.imageView);

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    imageUris.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, imageUris.size());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView,remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recyclerImage);
            remove=itemView.findViewById(R.id.imageremove);
        }
    }
}
