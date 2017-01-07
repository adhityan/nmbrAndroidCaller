package nmbr.merchant.caller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.superclasses.NApplication;

/**
 * Created by Adhityan on 01/02/2016.
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(NApplication.SHARED_PREFERENCES_NAME, 0);
        boolean userLoggedIn = prefs.getBoolean("USER_LOGGED_IN", false);
        if(!userLoggedIn) return;

        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                if(pdusObj == null) return;

                for (Object aPdusObj : pdusObj) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String sender = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Utilities.logDebug("Sender: " + sender + " SMS: " + message);
                    if(sender.length() == 10 && Utilities.isNumeric(sender)) Utilities.startOverlayService(context, sender);
                    if (!sender.toLowerCase().contains("NMBR".toLowerCase())) return;

                    String code = getVerificationCode(message);
                    if(code == null) return;

                    Utilities.logDebug("OTP: " + code);
                    EventBus.getDefault().post(new VerifyCodeReceivedEvent(code));
                }
            }
        }
        catch (Exception e) { Utilities.logError("This just happened while trying to read SMS", e); }
    }

    /**
     * Getting the OTP from sms message body
     *
     * @param message The SMS message text
     * @return OTP
     */
    private String getVerificationCode(String message) {
        Pattern pattern = Pattern.compile("(?<!\\d)\\d{6}(?!\\d)");
        Matcher matcher = pattern.matcher(message);
        if(matcher.find()) return matcher.group(0);
        else return null;
    }

    public class VerifyCodeReceivedEvent {
        public final String code;
        public VerifyCodeReceivedEvent(String code) {
            this.code = code;
        }
    }
}