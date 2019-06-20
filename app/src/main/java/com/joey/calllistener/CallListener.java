package com.joey.calllistener;

import android.content.Context;
import android.util.Log;

import java.util.Date;

public class CallListener extends PhonecallReceiver {
    private static final String TAG = CallListener.class.getSimpleName();

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        //Log.d(TAG, "onIncomingCallReceived: " + "Number: " + number);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //Log.d(TAG, "onIncomingCallAnswered: " + "Number: " + number);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //Log.d(TAG, "onIncomingCallEnded: " + "Number: " + number);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //Log.d(TAG, "onOutgoingCallStarted: " + "Number: " + number);
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //Log.d(TAG, "onOutgoingCallEnded: " + "Number: " + number);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        //Log.d(TAG, "onMissedCall: " + "Number: " + number);
    }
}
