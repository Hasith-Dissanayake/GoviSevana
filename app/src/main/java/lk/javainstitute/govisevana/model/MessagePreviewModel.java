package lk.javainstitute.govisevana.model;

public class MessagePreviewModel {
    private String phoneNumber;
    private String lastMessage;
    private long timestamp;

    public MessagePreviewModel() {

    }

    public MessagePreviewModel(String phoneNumber, String lastMessage, long timestamp) {
        this.phoneNumber = phoneNumber;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
