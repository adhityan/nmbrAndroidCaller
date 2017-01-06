package nmbr.merchant.caller.libs.socketio;

import android.os.Handler;
import android.os.Looper;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;

import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.superclasses.NApplication;

public class PubSub {
    private static PubNub pubnub;
    private static Handler handler;
    private static asyncInterface activity;
    private static String channel;

    static {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(NApplication.PUBNUB_PUBLISH_KEY);
        pnConfiguration.setPublishKey(NApplication.PUBNUB_SUBSCRIBE_KEY);
        pnConfiguration.setSecretKey(NApplication.PUBNUB_SECRET_KEY);
        pnConfiguration.setUuid(String.valueOf(NApplication.RUID));
        pnConfiguration.setSecure(true);

        pubnub = new PubNub(pnConfiguration);
        handler = new Handler(Looper.getMainLooper());
    }

    public static void init(int user_id) {
        channel = "unmc" + user_id;

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    Utilities.logWarning("Pubnub unexpected disconnect");
                    // This event happens when radio / connectivity is lost
                    pubnub.reconnect();
                }
                else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    Utilities.logDebug("Pubnub connectCallback");
                    // Connect event. You can do stuff like publish, and know you'll get it.
                    // Or just use the connected event to confirm you are subscribed for
                    // UI / internal notifications, etc
                }
                else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                    Utilities.logDebug("Pubnub reConnectCallback");
                    // Happens as part of our regular operation. This event happens when
                    // radio / connectivity is lost, then regained.
                }
                else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
                    Utilities.logError("Pubnub decryption error");
                    // Handle messsage decryption error. Probably client configured to
                    // encrypt messages and on live data feed it received plain text.
                }
                else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
                    Utilities.logWarning("Pubnub timeout callback");
                    // do some magic and call reconnect when ready
                    pubnub.reconnect();
                }
            }

            @Override
            public void message(PubNub pubnub, final PNMessageResult message) {
                Utilities.logDebug("Pubnub successCallback");

                if (activity != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            String m = String.valueOf(message.getMessage());
                            Utilities.logDebug("Pubnub message: " + m);
                            activity.messageReceived(m);
                        }
                    });
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) { }
        });

        pubnub.subscribe().channels(Arrays.asList(channel)).execute();
    }

    public static void terminate() {
        pubnub.unsubscribe()
                .channels(Arrays.asList(channel))
                .execute();
    }

    public static void subscribe(final asyncInterface activity) {
        PubSub.activity = activity;
    }

    public static void unsubscribe() {
        activity = null;
    }

    public static void resubscribe() {
        pubnub.reconnect();
    }
}