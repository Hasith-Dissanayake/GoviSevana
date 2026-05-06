package lk.javainstitute.govisevana.navigations;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.ChatAdapter;
import lk.javainstitute.govisevana.broadcast.MessageReceiver;
import lk.javainstitute.govisevana.model.MessageModel;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private EditText messageInput;
    private ImageView sendMessageButton, backIcon, chatProfileImage;
    private FirebaseFirestore db;
    private String currentUserPhone, farmerPhone;
    private SharedPreferenceHelper preferenceHelper;
    private TextView chatFarmerName;
    private LocalBroadcastManager localBroadcastManager;

    public ChatFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();
        preferenceHelper = new SharedPreferenceHelper(requireContext());
        currentUserPhone = preferenceHelper.getUserPhone();

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        backIcon = view.findViewById(R.id.backicon);
        chatProfileImage = view.findViewById(R.id.chatProfileImage);
        chatFarmerName = view.findViewById(R.id.chatFarmerName);
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(messageList, currentUserPhone);
        chatRecyclerView.setAdapter(chatAdapter);

        if (getArguments() != null) {
            farmerPhone = getArguments().getString("farmerPhone");
            String farmerName = getArguments().getString("farmerName");

            if (farmerName != null) {
                chatFarmerName.setText(farmerName);
            }


            fetchFarmerProfileImage(farmerPhone);
        }

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        loadMessages();
        return view;
    }

    private void fetchFarmerProfileImage(String phone) {
        if (phone == null || phone.isEmpty()) {
            Log.e("ChatFragment", "Farmer phone is null or empty");
            return;
        }

        db.collection("users").document(phone)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_person_24)
                                        .error(R.drawable.ic_person_24)
                                        .circleCrop()
                                        .into(chatProfileImage);
                            } else {
                                Log.e("ChatFragment", "Profile image URL is empty.");
                            }
                        } else {
                            Log.e("ChatFragment", "Farmer document does not exist.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ChatFragment", "Failed to load profile image", e);
                    }
                });
    }

    private void loadMessages() {
        if (farmerPhone == null || currentUserPhone == null) return;

        CollectionReference messagesRef = db.collection("messages");

        messagesRef.whereIn("sender", Arrays.asList(currentUserPhone, farmerPhone))
                .whereIn("receiver", Arrays.asList(currentUserPhone, farmerPhone))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("ChatFragment", "Error loading messages: ", e);
                        return;
                    }

                    messageList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            MessageModel message = document.toObject(MessageModel.class);
                            messageList.add(message);
                        } catch (Exception ex) {
                            Log.e("ChatFragment", "Failed to parse message: " + document.getId(), ex);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        DocumentReference messageRef = db.collection("messages").document();

        MessageModel message = new MessageModel(
                messageRef.getId(),
                currentUserPhone,
                farmerPhone,
                text,
                Timestamp.now()
        );

        messageRef.set(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        messageInput.setText("");
                        loadMessages();
                        sendNotificationBroadcast(text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void sendNotificationBroadcast(String messageText) {
        Intent broadcastIntent = new Intent(requireContext(), MessageReceiver.class);
        broadcastIntent.putExtra("senderPhone", currentUserPhone);
        broadcastIntent.putExtra("messageText", messageText);
        requireContext().sendBroadcast(broadcastIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("lk.javainstitute.govisevana.MESSAGE_NOTIFICATION");
        localBroadcastManager.registerReceiver(messageReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        localBroadcastManager.unregisterReceiver(messageReceiver);
    }

    private final MessageReceiver messageReceiver = new MessageReceiver();
}
