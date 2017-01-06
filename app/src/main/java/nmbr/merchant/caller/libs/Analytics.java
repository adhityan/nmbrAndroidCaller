package nmbr.merchant.caller.libs;

import android.content.Context;

import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import java.util.HashMap;
import java.util.Map;

import nmbr.merchant.caller.superclasses.NApplication;


public class Analytics {
    private static boolean hasInit;
    private static Analytics instance;

    static {
        hasInit = false;
    }

    private com.segment.analytics.Analytics segment;
    //private AppEventsLogger facebook;

    public static Analytics get() {
        if (instance == null) {
            instance = new Analytics();
        }

        return instance;
    }

    public static void init(Context context) {
        try {
            Analytics.get().segment = new com.segment.analytics.Analytics.Builder(context, NApplication.SEGMENT_API_KEY).logLevel(com.segment.analytics.Analytics.LogLevel.NONE).build();
        } catch (Exception e) {
            Analytics.get().segment = null;
        }

        /*try {
            Analytics.get().facebook = AppEventsLogger.newLogger(context);
        } catch (Exception e) {
            Analytics.get().facebook = null;
        }*/

        hasInit = true;
        instance.track("App Launched");
    }

    public void identify(String user_id) {
        identify(user_id, null);
    }

    public void identify(String user_id, HashMap<String, Object> traits) {
        if (!hasInit) return;

        Traits t = new Traits();
        if (traits != null) {
            for (Map.Entry<String, Object> entry : traits.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                t.put(key, value);
            }
        }

        try {
            if (segment != null) segment.identify(user_id, t, null);
        } catch (Exception ignored) {
        }
    }

    public void company(String program_id) {
        company(program_id, null);
    }

    public void company(String program_id, HashMap<String, Object> traits) {
        if (!hasInit) return;

        Traits t = new Traits();
        if (traits != null) {
            for (Map.Entry<String, Object> entry : traits.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                t.putValue(key, value);
            }
        }

        try {
            if (segment != null) segment.group(program_id, t, null);
        } catch (Exception ignored) {
        }
    }

    public void track(String event) {
        track(event, null);
    }

    public void track(String event, HashMap<String, Object> properties) {
        if (!hasInit) return;

        Properties p = new Properties();
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                p.put(key, value);
            }
        }

        try {
            if (segment != null) segment.track(event, p);
        } catch (Exception ignored) {
        }
    }

    public void screen(String screen) {
        screen(screen, null);
    }

    public void screen(String screen, HashMap<String, Object> properties) {
        if (!hasInit) return;

        Properties p = new Properties();
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                p.put(key, value);
            }
        }

        try {
            if (segment != null) segment.screen(null, screen, p);
        } catch (Exception ignored) {
        }
    }
}