package nmbr.merchant.caller.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nmbr.merchant.caller.R;
import nmbr.merchant.caller.contracts.Segment;
import nmbr.merchant.caller.contracts.SegmentMap;
import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.libs.api.APICall;
import nmbr.merchant.caller.libs.api.apiInterface;

public class OverlayDialog extends Dialog implements apiInterface {
    private int uid;
    private String number;
    private ImageView closeButton;
    private TextView phoneView, nameView;
    private List<SegmentMap> userSegments;
    private FlowLayout segmentsHolder;
    private Context context;
    private SimpleDraweeView profilePic;
    private LinearLayout rootLayout;

    public OverlayDialog(Context context, String number) {
        super(context, R.style.OverlayWindow);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;
        this.number = number;
        work();
    }

    public void dismissDialog() {
        this.hide();
        this.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_overlay);
        this.hide();

        closeButton = (ImageView)findViewById(R.id.close_button);
        phoneView = (TextView)findViewById(R.id.user_phone);
        nameView = (TextView)findViewById(R.id.user_name);
        segmentsHolder = (FlowLayout)findViewById(R.id.segments_holder);
        profilePic = (SimpleDraweeView)findViewById(R.id.user_image);
        rootLayout = (LinearLayout)findViewById(R.id.dialog_overlay);

//        rootLayout.setOnTouchListener(new View.OnTouchListener() {
//            private float mDx;
//            private float mDy;
//
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                int action = motionEvent.getAction();
//                if (action == MotionEvent.ACTION_DOWN) {
//                    mDx = mCurrentX - event.getRawX();
//                    mDy = mCurrentY - event.getRawY();
//                } else
//                if (action == MotionEvent.ACTION_MOVE) {
//                    mCurrentX = (int) (event.getRawX() + mDx);
//                    mCurrentY = (int) (event.getRawY() + mDy);
//                    OverlayDialog.this.update(mCurrentX, mCurrentY, -1, -1);
//                }
//                return true;
//            }
//        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverlayDialog.this.dismissDialog();
            }
        });

        this.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    private void work() {
        uid = 0;
        List<Pair<String, String>> get = new ArrayList<>(1);
        get.add(new Pair<>("phone", number));
        new APICall(this, APICall.HOST + "userdetails.json", "userdetail", get);
    }

    private void showMe() {
        this.show();
    }

    @Override
    public void apiResponse(String response, String code) {
        try {
            final JSONObject j = new JSONObject(response);
            this.showMe();

            final JSONObject basic = j.getJSONObject("basic");
            this.uid = basic.getInt("id");

            boolean isNameAvailable = basic.getBoolean("isNameAvailable");
            if(!isNameAvailable) nameView.setText("[Unknown]");
            else nameView.setText(basic.getString("name"));
            phoneView.setText(String.format("+91%s", basic.getString("phone")));

            this.userSegments = new ArrayList<SegmentMap>();
            JSONArray seqways = j.getJSONArray("segments");
            for(int i = 0; i < seqways.length(); i++) {
                this.userSegments.add(new SegmentMap(seqways.getJSONObject(i)));
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

            processSegments();
        } catch (Exception e) { Utilities.logError("Error", e); Toast.makeText(this.context, "Something is amiss! Try again.", Toast.LENGTH_LONG).show(); }
    }

    private void processSegments() {
        segmentsHolder.removeAllViews();

        if(userSegments.size() > 0) {
            Segment firstSegment = userSegments.get(0).segment;
            addSingleSegment(firstSegment.name, firstSegment.color);

            if(userSegments.size() > 1) addSingleSegment(String.format("+%d More", userSegments.size() - 1), "000000");
        }
    }

    private void addSingleSegment(String name, String color) {
        TextView text = new TextView(this.context);
        text.setTextSize(12);
        text.setPadding(10, 5, 10, 5);
        text.setTextColor(Color.WHITE);
        text.setTypeface(null, Typeface.BOLD);

        GradientDrawable shape = (GradientDrawable) ContextCompat.getDrawable(this.context, R.drawable.small_circle);
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
    public void apiError(String message, String code) {

    }

    @Override
    public void connectionError(int Status, String code) {
        Utilities.logWarning("Get Phone. Connection failed.");
    }
}
