package lk.javainstitute.govisevana.broadcast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import lk.javainstitute.govisevana.R;
import lk.javainstitute.govisevana.MainActivity;

public class MessageReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "govisevana_messages";
    private static final String CHANNEL_NAME = "Chat Messages";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String senderPhone = intent.getStringExtra("senderPhone");
        String messageText = intent.getStringExtra("messageText");

        showNotification(context, senderPhone, messageText);
    }

    private void showNotification(Context context, String senderPhone, String messageText) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }


        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_messages_24)
                .setContentTitle("New Message from " + senderPhone)
                .setContentText(messageText)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
