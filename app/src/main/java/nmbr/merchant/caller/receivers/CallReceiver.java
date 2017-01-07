package nmbr.merchant.caller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.Toast;

import nmbr.merchant.caller.activities.OverlayDialog;
import nmbr.merchant.caller.libs.Utilities;

public class CallReceiver extends BroadcastReceiver {
    OverlayDialog dialog;
    TelephonyManager telephonyManager;
    PhoneStateListener listener;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // If, the received action is not a type of "Phone_State", ignore it
        if (!intent.getAction().equals("android.intent.action.PHONE_STATE"))
            return;

        if(dialog == null) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            listener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    String stateString = "N/A";

                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            stateString = "Idle";
                            telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
                            dialog = null;
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            stateString = "Off Hook";
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                            stateString = "Ringing";
                            dialog = new OverlayDialog(context, incomingNumber);
                            break;
                    }

                    Utilities.logDebug("onCallStateChanged: " + stateString + " | " + incomingNumber);
                }
            };

            // Register the listener with the telephony manager
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
