package nmbr.merchant.caller.libs.api;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.superclasses.NApplication;
import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APICall extends AsyncTask<Void, Void, String> {
    private static boolean hasInit = false;
    private static final String apiKey = "6d1cf80a6a3c0cdcadfe714d991baf5c";

    private static String device_id;
    public static String HOST = NApplication.API_HOST;
    private static double lat;
    private static double lng;

    private apiInterface a;
    private String url, code;
    private int status;
    private List<Pair<String, String>> get, post;

    public APICall(apiInterface a, String url, String code) {
        this(a, url, code, null);
    }

    public APICall(apiInterface a, String url, String code, List<Pair<String, String>> get) {
        this(a, url, code, get, null);
    }

    public APICall(apiInterface a, String url, String code, List<Pair<String, String>> get, List<Pair<String, String>> post) {
        this.status = -1;

        this.a = a;
        this.url = url;
        this.code = code;
        this.get = get;
        this.post = post;

        this.execute();
    }

    public static void init(Context c) {
        if(hasInit) return; hasInit = true;
        device_id = Utilities.getMAC();

        SharedPreferences prefs = c.getSharedPreferences(NApplication.SHARED_PREFERENCES_NAME, 0);
        lat = prefs.getFloat("lat", 0);
        lng = prefs.getFloat("lng", 0);

        PackageManager pm = c.getPackageManager();
        int hasPerm = pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, c.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);

            if (bestProvider != null) {
                Location currentLocation = locationManager.getLastKnownLocation(bestProvider);

                if (currentLocation != null) {
                    lat = currentLocation.getLatitude();
                    lng = currentLocation.getLongitude();
                } else lat = lng = -1;
            }
        }

        Utilities.logDebug("MAC: " + device_id);
        Utilities.logDebug("Current lat: " + lat);
        Utilities.logDebug("Current lng: " + lng);
    }

    public static void updateLatLng(LatLng l) {
        lat = l.latitude;
        lng = l.longitude;
    }

    public static APIResponse call(String url) {
        return call(url, null, null);
    }

    public static APIResponse call(String url, List<Pair<String, String>> get) {
        return call(url, get, null);
    }

    public static APIResponse call(String url, List<Pair<String, String>> get, List<Pair<String, String>> post) {
        if (get != null) {
            Uri.Builder ub = Uri.parse(url).buildUpon();

            for (Pair<String, String> entry : get) {
                ub.appendQueryParameter(entry.first, entry.second);
            }

            url = ub.build().toString();
        }

        Boolean isPost = false;
        if (post == null) post = new ArrayList<>();
        else isPost = true;

        Boolean isLatLngPresent = false;
        FormBody.Builder postBuilder = new FormBody.Builder();
        for (Pair<String, String> entry : post) {
            postBuilder.add(entry.first, entry.second);
            if (entry.first.equalsIgnoreCase("lat") || entry.first.equalsIgnoreCase("lng")) isLatLngPresent = true;
        }

        if (!isLatLngPresent) {
            postBuilder.add("lat", String.valueOf(lat));
            postBuilder.add("lng", String.valueOf(lng));
        }
        RequestBody postBuild = postBuilder.build();

        Request request = new Request.Builder()
                .addHeader("X-DEVICE-ID", device_id)
                .addHeader("X-HTTP-Y-API-KEY", apiKey)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(url)
                .post(postBuild)
                .build();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);
        if (NApplication.RECORD_NETWORK_TRAFFIC) clientBuilder.addNetworkInterceptor(new StethoInterceptor());
        OkHttpClient client = clientBuilder.build();

        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();

            String type = "GET";
            if (isPost) type = "POST";
            Utilities.logDebug(type + ": " + url + " Response code: " + response.code());

            int status = 1;
            if (response.header("Error", null) != null) status = 0;
            else if (result.equals("")) status = 0;
            return new APIResponse(result, status);
        }
        catch (IOException e) {
            return new APIResponse("", 3);
        }
    }

    @Override
    protected String doInBackground(Void... arg) {
        APIResponse r = call(url, get, post);
        status = r.status;
        return r.result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (status == 1) a.apiResponse(result, code);
        else if (status == 0) a.apiError(result, code);
        else {
            Utilities.logError("CALL FAILED WITH CODE " + status + ": " + url);
            a.connectionError(status, code);
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}