package lk.javainstitute.govisevana.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.model.MessageModel;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<MessageModel> messageList;
    private String currentUserPhone;

    public ChatAdapter(List<MessageModel> messageList, String currentUserPhone) {
        this.messageList = messageList;
        this.currentUserPhone = currentUserPhone;
    }

    @Override
    public int getItemViewType(int position) {

        if (messageList.get(position).getSender().equals(currentUserPhone)) {
            return 1;
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_sender, parent, false);
        } else {

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_receiver, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.messageTextView.setText(message.getText());
        holder.timestampTextView.setText(message.getFormattedTimestamp());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageText);
            timestampTextView = itemView.findViewById(R.id.messageTimestamp);
        }
    }
}
