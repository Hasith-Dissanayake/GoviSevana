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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.adapter.MessagesAdapter;
import lk.javainstitute.govisevana.model.MessagePreviewModel;
import lk.javainstitute.govisevana.helper.SharedPreferenceHelper;

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessagesAdapter messagesAdapter;
    private List<MessagePreviewModel> conversationList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserPhone;

    public MessagesFragment() {
        // Default constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        SharedPreferenceHelper preferenceHelper = new SharedPreferenceHelper(requireContext());
        currentUserPhone = preferenceHelper.getUserPhone();

        messagesAdapter = new MessagesAdapter(getContext(), conversationList, currentUserPhone);
        recyclerView.setAdapter(messagesAdapter);

        if (currentUserPhone != null && !currentUserPhone.isEmpty()) {
            loadConversations();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadConversations() {
        db.collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load conversations", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        HashMap<String, MessagePreviewModel> conversationMap = new HashMap<>();

                        for (QueryDocumentSnapshot document : value) {
                            String sender = document.getString("sender");
                            String receiver = document.getString("receiver");
                            String text = document.getString("text");


                            long timestamp = 0;
                            if (document.get("timestamp") instanceof com.google.firebase.Timestamp) {
                                timestamp = document.getTimestamp("timestamp").toDate().getTime();
                            } else if (document.get("timestamp") instanceof Long) {
                                timestamp = document.getLong("timestamp");
                            }


                            if (!sender.equals(currentUserPhone) && !receiver.equals(currentUserPhone)) {
                                continue;
                            }


                            String otherUser = sender.equals(currentUserPhone) ? receiver : sender;


                            if (!conversationMap.containsKey(otherUser) || timestamp > conversationMap.get(otherUser).getTimestamp()) {
                                conversationMap.put(otherUser, new MessagePreviewModel(otherUser, text, timestamp));
                            }
                        }


                        conversationList.clear();
                        conversationList.addAll(conversationMap.values());


                        Collections.sort(conversationList, (m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));

                        messagesAdapter.notifyDataSetChanged();
                    }
                });
    }

}
