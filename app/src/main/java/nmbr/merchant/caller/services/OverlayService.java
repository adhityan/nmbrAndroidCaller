package nmbr.merchant.caller.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import nmbr.merchant.caller.R;
import nmbr.merchant.caller.contracts.Segment;
import nmbr.merchant.caller.contracts.SegmentMap;
import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.libs.api.APICall;
import nmbr.merchant.caller.libs.api.apiInterface;

public class OverlayService extends Service implements apiInterface {
    private Boolean overlayVisible;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private LinearLayout overlay;

    public OverlayService() {
        this.overlayVisible = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Boolean terminate = intent.getBooleanExtra("terminate", false);
        if(terminate) hide();
        else {
            String number = intent.getStringExtra("number");
            processNumber(number);
        }
    }

    public void processNumber(String number) {
        List<Pair<String, String>> get = new ArrayList<>(1);
        get.add(new Pair<>("phone", number));
        new APICall(this, APICall.HOST + "userdetails.json", "userdetail", get);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.y = 150;
        params.x = 0;

        setup();
        overlay.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlay, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void show() {
        if (overlay != null && !this.overlayVisible) {
            this.overlayVisible = true;
            windowManager.addView(overlay, params);
        }
    }

    private void hide() {
        if (overlay != null && this.overlayVisible) {
            this.overlayVisible = false;
            windowManager.removeView(overlay);
            overlay = null;
        }
    }

    private void setup() {
        if(overlay != null) return;
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        overlay = (LinearLayout)li.inflate(R.layout.dialog_overlay, null);

        ImageView closeButton = (ImageView)overlay.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverlayService.this.stopSelf();
            }
        });
    }

    private void addSingleSegment(FlowLayout segmentsHolder, String name, String color) {
        TextView text = new TextView(this);
        text.setTextSize(12);
        text.setPadding(10, 5, 10, 5);
        text.setTextColor(Color.WHITE);
        text.setTypeface(null, Typeface.BOLD);

        GradientDrawable shape = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.small_circle);
        shape.mutate();
        shape.setColor(Color.parseColor("#" + color));
        text.setBackground(shape);

        text.setText(name);
        segmentsHolder.addView(text);

        FlowLayout.LayoutParams params = (FlowLayout.LayoutParams)text.getLayoutParams();
        params.setMargins(0,5,10,5);
        text.setLayoutParams(params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.hide();
    }

    @Override
    public void apiResponse(String response, String code) {
        try {
            final JSONObject j = new JSONObject(response);
            final JSONObject basic = j.getJSONObject("basic");

            final TextView nameView = (TextView)overlay.findViewById(R.id.user_name);
            final TextView phoneView = (TextView)overlay.findViewById(R.id.user_phone);
            final SimpleDraweeView profilePic = (SimpleDraweeView)overlay.findViewById(R.id.user_image);
            final FlowLayout segmentsHolder = (FlowLayout)overlay.findViewById(R.id.segments_holder);
            final TextView visitsView = (TextView)overlay.findViewById(R.id.visits_summary);
            final TextView historyView = (TextView)overlay.findViewById(R.id.history_summary);

            boolean isNameAvailable = basic.getBoolean("isNameAvailable");
            if(!isNameAvailable) nameView.setText("[Unknown]");
            else nameView.setText(basic.getString("name"));
            phoneView.setText(String.format("+91%s", basic.getString("phone")));

            ArrayList<SegmentMap> userSegments = new ArrayList<SegmentMap>();
            JSONArray seqways = j.getJSONArray("segments");
            for(int i = 0; i < seqways.length(); i++) {
                userSegments.add(new SegmentMap(seqways.getJSONObject(i)));
            }

            String url = basic.getString("photo_url");
            if (url != null && url.length() != 0) {
                ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                        if (imageInfo == null) return;
                        profilePic.setBackground(null);
                    }
                };
                DraweeController controller = Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener).setUri(Uri.parse(url)).build();
                profilePic.setController(controller);
            }

            final JSONObject stats = j.getJSONObject("stats");
            int totalVisits = stats.getInt("totalvisits");
            String vistsText = totalVisits + " " + (totalVisits == 1?"visit":"visits");
            String firstVisited = stats.getString("firstvisited");
            visitsView.setText(MessageFormat.format("{0} since {1}", vistsText, firstVisited));

            final JSONObject historyInfo = j.getJSONObject("history");
            final JSONArray histories = historyInfo.getJSONArray("lessons");
            if(histories.length() > 0) {
                JSONObject history = histories.getJSONObject(0);
                String summary = history.getString("summary");
                String friendlyTime = history.getString("friendlyTimestamp");
                historyView.setText(MessageFormat.format("{0} on {1}", summary, friendlyTime));
            }

            segmentsHolder.removeAllViews();
            if(userSegments.size() > 0) {
                Segment firstSegment = userSegments.get(0).segment;
                addSingleSegment(segmentsHolder, firstSegment.name, firstSegment.color);

                if(userSegments.size() > 1) addSingleSegment(segmentsHolder, String.format("+%d More", userSegments.size() - 1), "000000");
            }

            this.show();
        } catch (Exception e) { Utilities.logError("Error", e); }
    }

    @Override
    public void apiError(String message, String code) {
        Utilities.logWarning("Get phone. API Error.");
    }

    @Override
    public void connectionError(int Status, String code) {
        Utilities.logWarning("Get phone. Connection failed.");
    }
}
