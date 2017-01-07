package nmbr.merchant.caller.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import nmbr.merchant.caller.R;
import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.superclasses.NActivity;
import nmbr.merchant.caller.superclasses.NApplication;

public class SplashActivity extends NActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setBackgroundDrawable(null);

        SharedPreferences prefs = getSharedPreferences(NApplication.SHARED_PREFERENCES_NAME, 0);
        boolean userLoggedIn = prefs.getBoolean("USER_LOGGED_IN", false);

        SharedPreferences.Editor editor = prefs.edit();
        String mac_id = Utilities.getMAC();
        editor.putString("MAC_ID", mac_id);
        editor.commit();

        if (userLoggedIn) gotoHome();
        else gotoLogin();
    }

    private void gotoLogin() {
        Intent startIntent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    private void gotoHome() {
        Intent startIntent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(startIntent);
        finish();
    }
}
