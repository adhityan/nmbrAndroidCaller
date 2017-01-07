package nmbr.merchant.caller.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nmbr.merchant.caller.R;
import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.services.OverlayService;
import nmbr.merchant.caller.superclasses.NActivity;
import nmbr.merchant.caller.superclasses.NApplication;

public class HomeActivity extends NActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView userNameView = (TextView)findViewById(R.id.user_name);
        Button logoutButton = (Button)findViewById(R.id.logout_button);
        final SharedPreferences prefs = this.getSharedPreferences(NApplication.SHARED_PREFERENCES_NAME, 0);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.clearSharedPreferences(prefs);
                gotoLogin();
            }
        });

        userNameView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent(HomeActivity.this, OverlayService.class);
                i.putExtra("number", "9910314001");
                startService(i);

                //new OverlayDialog(HomeActivity.this, "9910314001");
                return false;
            }
        });

        String name = prefs.getString("USER_NAME", null);
        if(name != null) userNameView.setText(String.format("Welcome %s!", name));
        else logoutButton.performClick();
    }

    private void gotoLogin() {
        Intent startIntent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }
}
