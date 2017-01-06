package nmbr.merchant.caller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import nmbr.merchant.caller.activities.OverlayActivity;
import nmbr.merchant.caller.libs.Utilities;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Utilities.logDebug("HERE");
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Utilities.logDebug("IncomingBroadcastReceiver: onReceive: " + state);

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) || state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            Utilities.logDebug("Phone is ringing");

            Intent i = new Intent(context, OverlayActivity.class);
            i.putExtras(intent);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //Wait.oneSec();
            //context.startActivity(i);
        }
    }
}
