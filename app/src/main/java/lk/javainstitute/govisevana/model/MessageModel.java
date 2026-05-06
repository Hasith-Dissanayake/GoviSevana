package lk.javainstitute.govisevana.model;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageModel {
    private String messageId;
    private String sender;
    private String receiver;
    private String text;

    @ServerTimestamp
    private Timestamp timestamp;

    public MessageModel() {

    }

    public MessageModel(String messageId, String sender, String receiver, String text, Timestamp timestamp) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    public String getFormattedTimestamp() {
        if (timestamp == null) {
            return "Just now";
        }

        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}
