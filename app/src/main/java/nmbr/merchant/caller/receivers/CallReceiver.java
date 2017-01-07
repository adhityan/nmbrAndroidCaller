package nmbr.merchant.caller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.services.OverlayService;
import nmbr.merchant.caller.superclasses.NApplication;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        // If, the received action is not a type of "Phone_State", ignore it
        if (!intent.getAction().equals("android.intent.action.PHONE_STATE"))
            return;

        SharedPreferences prefs = context.getSharedPreferences(NApplication.SHARED_PREFERENCES_NAME, 0);
        boolean userLoggedIn = prefs.getBoolean("USER_LOGGED_IN", false);
        if(!userLoggedIn) return;

        String stateString = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Utilities.logDebug("onCallStateChanged: " + stateString);

        if (stateString.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Utilities.logDebug("incomingNumber: ", incomingNumber);
            Utilities.startOverlayService(context, incomingNumber);
        }
    }
}
