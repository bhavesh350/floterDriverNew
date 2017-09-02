package cargo.floter.driver;

/**
 * Created by SONI on 4/19/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.application.SingleInstance;
import cargo.floter.driver.model.TripStatus;
import cargo.floter.driver.utils.AppConstants;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (MyApp.getStatus(AppConstants.IS_LOGIN)) {

            Log.d(TAG, "From: " + remoteMessage.getFrom());
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
                Map<String, String> dataMap = remoteMessage.getData();
                if (dataMap.containsKey("trip_status")) {
                    String tripStatus = dataMap.get("trip_status");
                    Intent i;
                    if (tripStatus.equals(TripStatus.Pending.name())) {
                        MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID,
                                remoteMessage.getData().get("trip_id"));
                        if (MyApp.getStatus(AppConstants.IS_LOGIN)) {
                            MyApp.setStatus("ALLOW_TRIP", true);
                            if (MyApp.getStatus(AppConstants.IS_OPEN)) {
                                i = new Intent("cargo.floter.driver.RIDE");
                                i.putExtra("TYPE", "NEW_TRIP");
//                            timestamp
                                long millis = 0;
                                try {
                                    JSONObject o = new JSONObject(remoteMessage.getData().get("object"));
                                    millis = o.optLong("timestamp");

                                    SingleInstance.getInstance().setJsonTripPayload(o);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if ((System.currentTimeMillis() - millis) > 30000) {
                                    return;
                                }
                                sendBroadcast(i);
                                return;
                            }
                            long millis = 0;
                            try {
                                JSONObject o = new JSONObject(remoteMessage.getData().get("object"));
                                millis = o.optLong("timestamp");
                                SingleInstance.getInstance().setJsonTripPayload(o);
                            } catch (JSONException e2) {
                                e2.printStackTrace();
                            }
                            if ((System.currentTimeMillis() - millis) > 30000) {
                                return;
                            }
                            sendNewRideNotification("New booking arrived\nclick to open.", remoteMessage.getData().get("trip_id"));
                        }
                    } else if (tripStatus.equals(TripStatus.Cancelled.name())) {
                        MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
                        MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
                        if (MyApp.getStatus(AppConstants.IS_OPEN)) {
                            i = new Intent("cargo.floter.driver.RIDE");
                            i.putExtra("TYPE", "CANCELLED");
                            sendBroadcast(i);
                            return;
                        }
                        sendCancelBookingNotification(dataMap.get("trip_id"), dataMap.get(MyApp.EXTRA_MESSAGE));
                    } else if (tripStatus.equals(TripStatus.Accepted.name())) {
                        MyApp.setStatus(AppConstants.IS_ON_TRIP, true);
                        MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, remoteMessage.getData().get("trip_id"));
                        sendNewRideNotification("New Trip Assigned by Floter\nclick to open.", remoteMessage.getData().get("trip_id"));
                    } else if (tripStatus.equals(TripStatus.Finished.name())) {
                        sendNotification(dataMap.get(MyApp.EXTRA_MESSAGE));
                    } else if (tripStatus.equals("upcoming") || tripStatus.equals(TripStatus.Upcoming.name())) {
//                    MyApp.setStatus(AppConstants.IS_ON_TRIP, true);
//                    MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, remoteMessage.getData().get("trip_id"));
                        sendUpcomingNotification("New Upcoming Trip has been Assigned by Floter\nclick to open.", remoteMessage.getData().get("trip_id"));
                    }
                }
            }
        }
    }


    private void sendNewRideNotification(String messageBody, String tripId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("TYPE", "NEW_TRIP");
        intent.putExtra("trip_id", tripId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String messageText = messageBody;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification mNotification = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("New Booking Request").setContentText(messageText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText)).setAutoCancel(false).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)).build();
        mNotification.flags = 21;
        try {
            notificationManager.notify(Integer.parseInt(MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID)), mNotification);
        } catch (Exception e) {
            notificationManager.notify(0, mNotification);
        }
    }

    private void sendUpcomingNotification(String messageBody, String tripId) {
        Intent intent = new Intent(this, UpcomingTrips.class);
        intent.putExtra("TYPE", "NEW_TRIP");
        intent.putExtra("trip_id", tripId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String messageText = messageBody;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification mNotification = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("New Booking Request").setContentText(messageText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText)).setAutoCancel(false).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)).build();
        mNotification.flags = 21;
        try {
            notificationManager.notify(Integer.parseInt(MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID)), mNotification);
        } catch (Exception e) {
            notificationManager.notify(0, mNotification);
        }
    }

    private void sendCancelBookingNotification(String tripId, String messageText) {
        Intent intent = new Intent(this, OnTripActivity.class);
        intent.putExtra("TYPE", "CANCELLED");
        intent.putExtra("trip_id", tripId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("Trip Cancelled").setContentText(messageText).setStyle(new NotificationCompat.BigTextStyle().bigText(messageText)).setAutoCancel(true).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            notificationManager.notify(Integer.parseInt(MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID)), notificationBuilder.build());
        } catch (Exception e) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("Floter").setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody)).setContentText(messageBody).setAutoCancel(true).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)).build());
    }
}