package com.joey.calllistener;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

public class CallListenerService extends Service {
    private static final String TAG = CallListenerService.class.getSimpleName();

    private static boolean started;

    public CallListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!started){
            started = true;
            buildNotification();
            startCallListening();
        }else{
            buildNotification();
        }
    }

    private void buildNotification() {
        registerReceiver(stopReceiver, new IntentFilter(Constants.STOP_LISTENING_SERVICE));
        /*PendingIntent broadcastIntent = PendingIntent.getBroadcast(this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);*/
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("tracking", "tracking");
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent broadcastIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent broadcastIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.STOP_TRACKING_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.CHANNEL_ID_MUTE, getString(R.string.channel_name_mute), NotificationManager.IMPORTANCE_LOW);

            //Configure the notification channel, NO SOUND
            notificationChannel.setDescription("mute");
            notificationChannel.setSound(null,null);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        String subtitle = "";

        //Create the persistent notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID_MUTE)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.listening_enabled_notification))
                //Make this notification ongoing so it can’t be dismissed by the user//
                .setOngoing(true)
                .setContentIntent(broadcastIntent);
        //mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        mBuilder.setDefaults(0);

        startForeground(Constants.NOTIFICATION_ID_LISTENING, mBuilder.build());
    }

    private void dismissNotification(){
        //cancel notification
        //Get an instance of NotificationManager//
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one.
        mNotificationManager.cancel((Constants.NOTIFICATION_ID_LISTENING/*(int) orderID*/) /* ID of notification */);
    }

    private void startCallListening(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        MainActivity.getInstance().registerReceiver(new CallListener(){
            @Override
            protected void onIncomingCallReceived(Context ctx, final String number, Date start) {
                Log.d(TAG, "onIncomingCallReceived: " + "Number: " + number);
                //send number to backend
                sendNumberToBackend(number);
            }

            @Override
            protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
                Log.d(TAG, "onIncomingCallAnswered: " + "Number: " + number);
            }

            @Override
            protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
                Log.d(TAG, "onIncomingCallEnded: " + "Number: " + number);
            }

            @Override
            protected void onOutgoingCallStarted(Context ctx, final String number, Date start) {
                Log.d(TAG, "onOutgoingCallStarted: " + "Number: " + number);
                //send number to backend
                sendNumberToBackend(number);
            }

            @Override
            protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
                Log.d(TAG, "onOutgoingCallEnded: " + "Number: " + number);
            }

            @Override
            protected void onMissedCall(Context ctx, String number, Date start) {
                Log.d(TAG, "onMissedCall: " + "Number: " + number);
            }
        }, intentFilter);
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Unregister the BroadcastReceiver when the notification is tapped//
            unregisterReceiver(stopReceiver);

            //dismiss the notification
            dismissNotification();

            started = false;

            //TODO stop call listener

            //Stop the Service//
            stopSelf();
        }
    };

    private void sendNumberToBackend(final String number){
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                showNumberInfo(number, "extra_info_from_backend");
            }
        }, 2000);
    }

    private void showNumberInfo(String number, String extraInfo){
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.myDialog);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View dialogView = inflater.inflate(R.layout.caller_dialog, null);

        TextView numberTextView = dialogView.findViewById(R.id.caller_number_textview);
        TextView infoTextView = dialogView.findViewById(R.id.caller_info_textview);
        ImageView dismissButton = dialogView.findViewById(R.id.dismiss_button);
        numberTextView.setText("Call from/to: " + number);
        infoTextView.setText(extraInfo);

        builder.setView(dialogView);
        final AlertDialog alert = builder.create();
        alert.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else{
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = alert.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.CENTER);
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the from from the window
                alert.dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        //Unregister the BroadcastReceiver when the notification is tapped//
        unregisterReceiver(stopReceiver);

        //dismiss the notification
        dismissNotification();

        //TODO stop call listener

        started = false;
    }
}
