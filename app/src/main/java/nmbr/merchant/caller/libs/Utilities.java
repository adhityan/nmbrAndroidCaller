package nmbr.merchant.caller.libs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import nmbr.merchant.caller.R;
import nmbr.merchant.caller.services.OverlayService;
import nmbr.merchant.caller.structs.NumberSource;
import nmbr.merchant.caller.superclasses.NApplication;

public class Utilities {
    public static final Hashtable<String, Typeface> typefaces = new Hashtable<>();
    public static String Regular = "fonts/ProximaNova-Reg.otf";
    public static String Bold = "fonts/ProximaNova-Semibold.otf";
    public static String Icons = "fonts/iconfont.otf";
    public static String logTag = "ENMBR";

    public static void logInfo(String message) {
        logInfo(logTag, message);
    }

    public static void logInfo(String tag, String message) {
        Log.i(tag, message);
        //Crashlytics.log(Log.INFO, tag, message);
    }

    public static void logDebug(String message) {
        logDebug(logTag, message);
    }

    public static void logDebug(String tag, String message) {
        Log.d(tag, message);
        //Crashlytics.log(Log.DEBUG, tag, message);
    }

    public static void logWarning(String message) {
        logWarning(logTag, message);
    }

    public static void logWarning(String tag, String message) {
        Log.w(tag, message);
        //Crashlytics.log(Log.WARN, tag, message);
    }

    public static void logError(String message) {
        logError(logTag, message);
    }

    public static void logError(String tag, String message) {
        Log.e(tag, message);
        //Crashlytics.log(Log.ERROR, tag, message);
    }

    public static void logError(String message, Exception e) {
        logError(logTag, message, e);
    }

    public static void logError(String tag, String message, Exception e) {
        Log.e(tag, message, e);
        //Crashlytics.log(Log.ERROR, tag, message);
        //Crashlytics.logException(e);
    }

    public static Typeface getTypeface(Context c, String name) {
        synchronized (typefaces) {
            if (!typefaces.containsKey(name)) {
                try {
                    InputStream inputStream = c.getAssets().open(name);
                    File file = createFileFromInputStream(inputStream, name);
                    if (file == null) {
                        return Typeface.DEFAULT;
                    }
                    Typeface t = Typeface.createFromFile(file);
                    typefaces.put(name, t);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Typeface.DEFAULT;
                }
            }
            return typefaces.get(name);
        }
    }

    private static File createFileFromInputStream(InputStream inputStream, String name) {
        try {
            File f = File.createTempFile("font", null);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];

            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return f;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap createCenterCroppedImage(Bitmap bmp, int reqHeight, int reqWidth) {
        // if(bmp.getHeight() > bmp.getWidth()) {
        int xdiff = bmp.getWidth() - reqWidth;
        int ydiff = bmp.getHeight() - reqHeight;

        if (xdiff >= 0 && ydiff >= 0)
            bmp = Bitmap.createBitmap(bmp, xdiff / 2, ydiff / 2, reqWidth, reqHeight);
        else if (xdiff >= 0 && ydiff < 0) {
            xdiff = bmp.getWidth() - bmp.getHeight();
            bmp = Bitmap.createBitmap(bmp, xdiff / 2, 0, bmp.getHeight(), bmp.getHeight());
        } else if (xdiff < 0 && ydiff >= 0) {
            ydiff = bmp.getHeight() - bmp.getWidth();
            bmp = Bitmap.createBitmap(bmp, 0, ydiff / 2, bmp.getWidth(), bmp.getWidth());
        } else {
            if (bmp.getHeight() > bmp.getWidth()) {
                ydiff = bmp.getHeight() - bmp.getWidth();
                bmp = Bitmap.createBitmap(bmp, 0, ydiff / 2, bmp.getWidth(), bmp.getWidth());
            } else {
                xdiff = bmp.getWidth() - bmp.getHeight();
                bmp = Bitmap.createBitmap(bmp, xdiff / 2, 0, bmp.getHeight(), bmp.getHeight());
            }
        }
        // } else {
        // int diff = bmp.getWidth() - bmp.getHeight();
        // bmp = Bitmap.createBitmap(bmp, diff/2, 0, reqHeight, reqHeight);
        // }

        return bmp;
    }

    // new in v60
    public static String convert_time(long time) {
        long second = 1;
        long minute = 60 * second;
        long hour = 60 * minute;
        long day = 24 * hour;
        long month = 30 * day;

        long delta = (System.currentTimeMillis() / 1000) - time;

        if (delta < (60 * minute))
            return NApplication.context.getString(R.string.NOW);
        else if (delta < (24 * hour))
            return NApplication.context.getString(R.string.FEED_TIME_HOUR, (int) (Math.floor(delta / hour)));
        else if (delta < 31 * day)
            return NApplication.context.getString(R.string.FEED_TIME_DAY, (int) (Math.floor(delta / day)));
        else if (delta < month * 12) {
            int months = (int) (Math.floor(delta / day / 30));
            return NApplication.context.getString(R.string.FEED_TIME_MONTH, months);
        } else {
            int years = (int) (Math.floor(delta / day / 30 / 12));
            return NApplication.context.getString(R.string.FEED_TIME_YEAR, years);
        }
    }

    public static boolean isAndroidL() {
        return android.os.Build.VERSION.SDK_INT >= 21;
    }

    public static ArrayList<String> getListFromJSON(JSONArray inputArray) {
        ArrayList<String> list = new ArrayList<>();

        if (inputArray != null) {
            int len = inputArray.length();

            for (int i = 0; i < len; i++) {
                try {
                    list.add(inputArray.get(i).toString());
                } catch (Exception ignored) {
                }
            }
        }

        return list;
    }

    public static String implodeArray(List<String> inputArray, String glueString) {
        /** Output variable */
        String output = "";

        try {
            if (inputArray.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(inputArray.get(0));

                for (int i = 1; i < inputArray.size(); i++) {
                    sb.append(glueString);
                    sb.append(inputArray.get(i));
                }

                output = sb.toString();
            }
        } catch (Exception ignored) {
        }

        return output;
    }

    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 40, targetHeight = 40;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);

        if (scaleBitmapImage != null) {
            canvas.drawBitmap(scaleBitmapImage,
                    new Rect(0, 0, scaleBitmapImage.getWidth(), scaleBitmapImage.getHeight()),
                    new Rect(0, 0, targetWidth, targetHeight), null);
        }
        return targetBitmap;
    }

    public static String getHash(String input) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            byte[] result = mDigest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte aResult : result) {
                sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception e) {
            Log.e("exeno", "Hash failed", e);
            return "";
        }
    }

    public static boolean isImmersiveModeAvailable() {
        try {
            int id = NApplication.context.getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
            if (id == 0) return false; // not on KitKat
            else
                return NApplication.context.getResources().getBoolean(id); // enabled = are translucent bars supported on this device
        } catch (Exception e) {
            return false;
        }
    }

    public static void enterImmersiveMode(final View view) {
        if (!isImmersiveModeAvailable()) return;

        Runnable restoreImmersiveModeRunnable = new Runnable() {
            public void run() {
                enterImmersiveMode(view);
            }
        };

        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        view.postDelayed(restoreImmersiveModeRunnable, 500);
    }

    public static String getScreenSize() {
        int screenSize = NApplication.context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        String screenSizeText = "Screen size is neither large, normal or small";
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                screenSizeText = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                screenSizeText = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                screenSizeText = "Small screen";
                break;
        }

        return screenSizeText;
    }

    public static Point getScreenResolution() {
        WindowManager wm = (WindowManager) NApplication.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return new Point(width, height);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        double ratioH = (double) options.outHeight / reqHeight;
        double ratioW = (double) options.outWidth / reqWidth;

        int h = (int) Math.round(ratioH);
        int w = (int) Math.round(ratioW);

        if (h > 1 || w > 1) {
            if (h > w) {
                inSampleSize = h >= 2 ? h : 2;

            } else {
                inSampleSize = w >= 2 ? w : 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap fastBlur(Bitmap bitmap, int radius) {
        try {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            Utilities.logDebug("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            Bitmap blurBitmap = bitmap.copy(bitmap.getConfig(), true);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;

                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }

                yw += w;
            }

            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }

                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }

            Utilities.logDebug("pix", w + " " + h + " " + pix.length);
            blurBitmap.setPixels(pix, 0, w, 0, 0, w, h);
            return blurBitmap;
        } catch (OutOfMemoryError e) {
            return bitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }

    public static String getUUID() {
        return Settings.Secure.getString(NApplication.context.getContentResolver(), Settings.Secure.ANDROID_ID);

        /*
        final TelephonyManager tm = (TelephonyManager) NApplication.context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(NApplication.context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();*/
    }

    public static String constructFileName(String url) {
        return url.replaceAll("/", "_");
    }

    public static Bitmap getBitmapFromDisk(String url, Context ctx) {

        Bitmap defautBitmap = null;

        try {
            String filename = constructFileName(url);
            File filePath = new File(ctx.getCacheDir(), filename);

            if (filePath.exists() && filePath.isFile() && !filePath.isDirectory()) {
                FileInputStream fi;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                fi = new FileInputStream(filePath);
                defautBitmap = BitmapFactory.decodeStream(fi, null, opts);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception ignored) {
        }

        return defautBitmap;
    }

    public static void writeBitmapToDisk(String url, Bitmap bmp, Context ctx, Bitmap.CompressFormat format) {
        FileOutputStream fos;
        String fileName = constructFileName(url);

        try {
            if (bmp != null) {
                fos = new FileOutputStream(new File(ctx.getCacheDir(), fileName));
                bmp.compress(format, 75, fos);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getRoundedCornerBitmap(final Bitmap bitmap, final float roundPx) {
        if (bitmap != null) {
            try {
                final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(output);

                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                final RectF rectF = new RectF(rect);

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);

                return output;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    public static String convertArrayToCSVString(List<String> list) {
        return list.toString().replace("[", "").replace("]", "").replace(", ", ",");
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean checkLgManufacturer() {
        return android.os.Build.VERSION.SDK_INT == 17 && android.os.Build.MANUFACTURER.toLowerCase().startsWith("lg");
    }

    /*public static boolean checkPlayServices(Context c) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        return googleAPI.isGooglePlayServicesAvailable(c) == ConnectionResult.SUCCESS;
    }

    public static String doPlayRegisteration(Context c) throws IOException {
        try {
            InstanceID instanceID = InstanceID.getInstance(c);
            return instanceID.getToken(c.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        }
        catch (IOException e) { Utilities.logError("At ID Listener Service", e); throw e; }
    }*/

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context c) {
        try {
            PackageInfo packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e) {
            Utilities.logError("Package name could not be found. FATAL", e);
            throw new RuntimeException("Package name not found");
        }
    }

    public static void showAlertDialog(final Activity activity, String title, String message, String b1Text, MaterialDialog.SingleButtonCallback b1Click) {
        showAlertDialog(activity, title, message, b1Text, b1Click, null, null);
    }

    public static void showAlertDialog(final Activity activity, String title, String message, String b1Text, MaterialDialog.SingleButtonCallback b1Click, String b2Text, MaterialDialog.SingleButtonCallback b2Click) {
        if(!activity.hasWindowFocus()) return;

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        if (title != null) builder.title(title);
        if (message != null) builder.content(message);

        if(b1Text != null) {
            builder.positiveText(b1Text);
            builder.onPositive(b1Click);
        }
        if(b2Text != null) {
            builder.negativeText(b2Text);
            builder.onNegative(b2Click);
        }
        builder.show();
    }

    public static String getMAC() {
        return Settings.Secure.getString(NApplication.context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getModelName() {
        if(Build.MANUFACTURER.equalsIgnoreCase("unknown")) return
                Build.MODEL + " " + Build.VERSION.RELEASE
                        + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
        else
            return Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    public static String getIMEI() {
        if(Build.MANUFACTURER.equalsIgnoreCase("unknown")) return "";
        else {
            TelephonyManager telephonyManager = (TelephonyManager) NApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getDeviceId();
        }
    }

    public static void clearSharedPreferences(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static void startOverlayService(Context context, int uid, String number, String unformattedNumber, NumberSource source) {
        Intent i = new Intent(context, OverlayService.class);
        i.putExtra("uid", uid);
        i.putExtra("number", number);
        i.putExtra("unformattedNumber", unformattedNumber);
        i.putExtra("source", source.toString());
        context.startService(i);
    }

    public static void stopOverlayService(Context context) {
        Intent i = new Intent(context, OverlayService.class);
        i.putExtra("terminate", true);
        context.startService(i);
    }
}