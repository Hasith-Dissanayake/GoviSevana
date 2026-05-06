package lk.javainstitute.govisevana.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.MessagePreviewModel;
import lk.javainstitute.govisevana.navigations.ChatFragment;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private Context context;
    private List<MessagePreviewModel> conversationList;
    private String currentUserPhone;

    public MessagesAdapter(Context context, List<MessagePreviewModel> conversationList, String currentUserPhone) {
        this.context = context;
        this.conversationList = conversationList;
        this.currentUserPhone = currentUserPhone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messages, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessagePreviewModel conversation = conversationList.get(position);
        String phoneNumber = conversation.getPhoneNumber();


        FirebaseFirestore.getInstance().collection("users").document(phoneNumber)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("fullname");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            holder.messageSenderName.setText(name != null ? name : phoneNumber);

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(context).load(profileImageUrl)
                                        .placeholder(R.drawable.ic_person_24)
                                        .into(holder.messageProfileImage);
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ChatFragment chatFragment = new ChatFragment();
                                    Bundle args = new Bundle();
                                    args.putString("farmerPhone", phoneNumber);
                                    args.putString("farmerName", name);
                                    chatFragment.setArguments(args);

                                    ((FragmentActivity) context).getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragment_container, chatFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            });
                        }
                    }
                });

        holder.messageText.setText(conversation.getLastMessage());
        holder.messageTimestamp.setText(formatTimestamp(conversation.getTimestamp()));
    }


    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView messageProfileImage;
        TextView messageSenderName, messageText, messageTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageProfileImage = itemView.findViewById(R.id.messageProfileImage);
            messageSenderName = itemView.findViewById(R.id.messageSenderName);
            messageText = itemView.findViewById(R.id.messageText);
            messageTimestamp = itemView.findViewById(R.id.messageTimestamp);
        }
    }
}
