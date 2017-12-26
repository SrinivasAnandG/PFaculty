package com.srinivasanand.pfaculty;

/**
 * Created by srinivasanand on 28/08/17.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class FcmMessagingService extends FirebaseMessagingService {
    String type = "";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            type = "json";
            sendNotification(remoteMessage.getData().toString());
        }
        if (remoteMessage.getNotification() !=null) {
            type = "message";
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }
    private void sendNotification(String messageBody){
        String id="",message="",title="";

        if(type.equals("json")) {
            try {
                JSONObject jsonObject = new JSONObject(messageBody);
                id = jsonObject.getString("id");
                message = jsonObject.getString("message");
                title = jsonObject.getString("title");

            } catch (JSONException e) {

            }
        }
        else if(type.equals("message"))
        {
            message = messageBody;
        }

        Intent intent=new Intent(this,NotificationsByAdmin.class);
        //Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
        intent.putExtra("title",message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle());
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSound(soundUri);
        notificationBuilder.setSmallIcon(R.drawable.snist);
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.drawable.snist));
        notificationBuilder.setAutoCancel(true);
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuilder.build());
    }
}

