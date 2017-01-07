package nmbr.merchant.caller.superclasses;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.nfc.NfcAdapter;
import android.util.LruCache;

import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import io.fabric.sdk.android.Fabric;
import nmbr.merchant.caller.libs.Analytics;
import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.libs.api.APICall;
import okhttp3.OkHttpClient;

public final class NApplication extends Application {
    public static final boolean RECORD_NETWORK_TRAFFIC = false;
    public static final String SHARED_PREFERENCES_NAME = "parapa";
    public static final LruCache<String, Bitmap> imageCache = new LruCache<>(30);

    public static final String API_HOST = "https://api.nmbr.club/";
    //public static final String API_HOST = "http://192.168.1.2/zencard/api/";

    public static Context context;
    public static boolean isNfcEnabled;
    public static boolean firstRun;
    public static boolean hasInit;

    public static int UID;
    public static String SEGMENT_API_KEY;
    public static String PUBNUB_PUBLISH_KEY;
    public static String PUBNUB_SUBSCRIBE_KEY;
    public static String PUBNUB_SECRET_KEY;
    public static String MAC_ID;

    static {
        hasInit = false;
        firstRun = true;
    }

    public static void init(String MAC_ID, String segment_api_key, String pubnub_publish_key, String pubnub_subscribe_key, String pubnub_secret_key) {
        if (hasInit) return;
        hasInit = true;

        NApplication.MAC_ID = MAC_ID;
        Crashlytics.setString("MAC_ID", MAC_ID);

        NApplication.SEGMENT_API_KEY = segment_api_key;
        NApplication.PUBNUB_PUBLISH_KEY = pubnub_publish_key;
        NApplication.PUBNUB_SUBSCRIBE_KEY = pubnub_subscribe_key;
        NApplication.PUBNUB_SECRET_KEY = pubnub_secret_key;

        Analytics.init(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        context = getApplicationContext();
        //Realm.deleteRealmFile(this);

        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        //TODO: Add last version setting clear? checks
        final int lastVersion = settings.getInt("version", 0);
        int currentVersion = -1;

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("version", currentVersion);
        editor.apply();

        String sText = Utilities.getScreenSize();
        Point sValue = Utilities.getScreenResolution();
        Utilities.logInfo("Screen detected: " + sText + " (" + sValue.x + "," + sValue.y + ")");

        Utilities.logDebug("Immersive mode: " + (Utilities.isImmersiveModeAvailable() ? "Available" : "Not available"));
        isNfcEnabled = NfcAdapter.getDefaultAdapter(this) != null;
        Utilities.logDebug("NFC status: " + (isNfcEnabled ? "Enabled" : "Disabled"));

        if (RECORD_NETWORK_TRAFFIC) Stetho.initializeWithDefaults(this);
        APICall.init(this);
        setUpFresco();

        //FacebookSdk.sdkInitialize(this);
        //AppEventsLogger.activateApp(this);
    }

    private void setUpFresco() {
        OkHttpClient client = new OkHttpClient();
        if (NApplication.RECORD_NETWORK_TRAFFIC) client.networkInterceptors().add(new StethoInterceptor());
        Fresco.initialize(context, OkHttpImagePipelineConfigFactory.newBuilder(context, client).setDownsampleEnabled(true).build());
    }
}