package nmbr.merchant.caller.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import nmbr.merchant.caller.R;
import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.superclasses.NActivity;
import nmbr.merchant.caller.superclasses.NApplication;

public class HomeActivity extends NActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView userNameView = (TextView)findViewById(R.id.user_name);
        ImageView logoutButton = (ImageView) findViewById(R.id.logout_button);
        final SimpleDraweeView userImage = (SimpleDraweeView)findViewById(R.id.user_image);
        final SharedPreferences prefs = this.getSharedPreferences(NApplication.SHARED_PREFERENCES_NAME, 0);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.clearSharedPreferences(prefs);
                gotoLogin();
            }
        });

        /*userNameView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent(HomeActivity.this, OverlayService.class);
                i.putExtra("number", "9910314001");
                startService(i);

                //new OverlayDialog(HomeActivity.this, "9910314001");
                return false;
            }
        });*/

        String name = prefs.getString("USER_NAME", null);
        if(name != null) userNameView.setText(String.format("Welcome %s!", name));
        else logoutButton.performClick();

        String url = prefs.getString("USER_IMAGE", null);
        if (url != null && url.length() != 0) {
            ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                    if (imageInfo == null) return;
                    userImage.setBackground(null);
                }
            };
            DraweeController controller = Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener).setUri(Uri.parse(url)).build();
            userImage.setController(controller);
        }
    }

    private void gotoLogin() {
        Intent startIntent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }
}
