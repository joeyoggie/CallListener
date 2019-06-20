package com.joey.calllistener;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static MainActivity mInstance;

    private static final int RC_PERMISSION_READ_PHONE_STATE = 1001;
    private static final int RC_PERMISSION_PROCESS_OUTGOING_CALLS = 1002;
    private static final int RC_PERMISSION_SYSTEM_ALERT_WINDOW = 1003;
    private static final int RC_ACTIVITY_PERMISSION_TURN_ON = 1004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton, stopButton;
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkReadPhoneStatePermissions();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrackingService();
            }
        });

        mInstance = this;
    }

    public static MainActivity getInstance(){
        return mInstance;
    }

    public static void startListeningService(){
        if(MainActivity.getInstance() != null){
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                MainActivity.getInstance().startForegroundService(new Intent(MainActivity.getInstance(), CallListenerService.class));
            }else{
                MainActivity.getInstance().startService(new Intent(MainActivity.getInstance(), CallListenerService.class));
            }
        }
    }

    public static void stopTrackingService(){
        if(MainActivity.getInstance() != null){
            MainActivity.getInstance().stopService(new Intent(MainActivity.getInstance(), CallListenerService.class));
        }
    }

    private void checkReadPhoneStatePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(mInstance, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                checkOutgoingCallsPermissions();
            }else{
                requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, RC_PERMISSION_READ_PHONE_STATE);
            }
        }else{
            //no need to show runtime permission stuff
            checkOutgoingCallsPermissions();
        }
    }

    private void checkOutgoingCallsPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(mInstance, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                checkSystemAlertWindowPermissions();
            }else{
                requestPermissions(new String[]{"android.permission.PROCESS_OUTGOING_CALLS"}, RC_PERMISSION_PROCESS_OUTGOING_CALLS);
            }
        }else{
            //no need to show runtime permission stuff
            checkSystemAlertWindowPermissions();
        }
    }

    private void checkSystemAlertWindowPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.canDrawOverlays(mInstance)){
                if (ContextCompat.checkSelfPermission(mInstance, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    startListeningService();
                }else{
                    requestPermissions(new String[]{"android.permission.SYSTEM_ALERT_WINDOW"}, RC_PERMISSION_SYSTEM_ALERT_WINDOW);
                }
            }else{
                startListeningService();
            }
        }else{
            //no need to show runtime permission stuff
            startListeningService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode){
            case RC_PERMISSION_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    checkOutgoingCallsPermissions();
                }else{
                    //denied
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.READ_PHONE_STATE")) {
                        new AlertDialog.Builder(mInstance)
                                .setTitle(mInstance.getResources().getString(R.string.phone_state_permission_required_title))
                                .setMessage(mInstance.getResources().getString(R.string.phone_state_permission_required_message))
                                .setPositiveButton(mInstance.getResources().getString(R.string.permission_allow), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, RC_PERMISSION_READ_PHONE_STATE);
                                    }
                                })
                                .show();
                    }else{
                        //Toast.makeText(getActivity(), "asked before and user denied", Toast.LENGTH_SHORT).show();
                        //Go to settings to enable permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(mInstance);
                        builder.setTitle(mInstance.getString(R.string.phone_state_permission_required_title));
                        builder.setMessage(mInstance.getString(R.string.phone_state_permission_required_message));
                        builder.setPositiveButton(mInstance.getString(R.string.go_to_app_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + Constants.PACKAGE_NAME));
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(intent, RC_ACTIVITY_PERMISSION_TURN_ON);
                            }
                        });
                        builder.setNegativeButton(mInstance.getString(R.string.exit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mInstance.finish();
                            }
                        });
                        builder.show();
                    }
                }
            }
            case RC_PERMISSION_PROCESS_OUTGOING_CALLS: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    checkSystemAlertWindowPermissions();
                }else{
                    //denied
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.PROCESS_OUTGOING_CALLS")) {
                        new AlertDialog.Builder(mInstance)
                                .setTitle(mInstance.getResources().getString(R.string.process_outgoing_calls_permission_required_title))
                                .setMessage(mInstance.getResources().getString(R.string.process_outgoing_calls_permission_required_message))
                                .setPositiveButton(mInstance.getResources().getString(R.string.permission_allow), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.PROCESS_OUTGOING_CALLS"}, RC_PERMISSION_PROCESS_OUTGOING_CALLS);
                                    }
                                })
                                .show();
                    }else{
                        //Toast.makeText(getActivity(), "asked before and user denied", Toast.LENGTH_SHORT).show();
                        //Go to settings to enable permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(mInstance);
                        builder.setTitle(mInstance.getString(R.string.process_outgoing_calls_permission_required_title));
                        builder.setMessage(mInstance.getString(R.string.process_outgoing_calls_permission_required_message));
                        builder.setPositiveButton(mInstance.getString(R.string.go_to_app_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + Constants.PACKAGE_NAME));
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(intent, RC_ACTIVITY_PERMISSION_TURN_ON);
                            }
                        });
                        builder.setNegativeButton(mInstance.getString(R.string.exit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mInstance.finish();
                            }
                        });
                        builder.show();
                    }
                }
            }
            case RC_PERMISSION_SYSTEM_ALERT_WINDOW: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    startListeningService();
                }else{
                    //denied
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.SYSTEM_ALERT_WINDOW")) {
                        new AlertDialog.Builder(mInstance)
                                .setTitle(mInstance.getResources().getString(R.string.system_alert_window_permission_required_title))
                                .setMessage(mInstance.getResources().getString(R.string.system_alert_window_permission_required_message))
                                .setPositiveButton(mInstance.getResources().getString(R.string.permission_allow), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.SYSTEM_ALERT_WINDOW"}, RC_PERMISSION_SYSTEM_ALERT_WINDOW);
                                    }
                                })
                                .show();
                    }else{
                        //Toast.makeText(getActivity(), "asked before and user denied", Toast.LENGTH_SHORT).show();
                        //Go to settings to enable permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(mInstance);
                        builder.setTitle(mInstance.getString(R.string.system_alert_window_permission_required_title));
                        builder.setMessage(mInstance.getString(R.string.system_alert_window_permission_required_message));
                        builder.setPositiveButton(mInstance.getString(R.string.go_to_app_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + Constants.PACKAGE_NAME));
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(intent, RC_ACTIVITY_PERMISSION_TURN_ON);
                            }
                        });
                        builder.setNegativeButton(mInstance.getString(R.string.exit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mInstance.finish();
                            }
                        });
                        builder.show();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_ACTIVITY_PERMISSION_TURN_ON){
            checkReadPhoneStatePermissions();
        }
    }
}
