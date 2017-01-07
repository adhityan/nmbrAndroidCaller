package nmbr.merchant.caller.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nmbr.merchant.caller.R;
import nmbr.merchant.caller.libs.Utilities;
import nmbr.merchant.caller.libs.api.APICall;
import nmbr.merchant.caller.libs.api.apiInterface;
import nmbr.merchant.caller.superclasses.NActivity;
import nmbr.merchant.caller.superclasses.NApplication;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends NActivity implements apiInterface {
    /**
     * Id to identity permission requests.
     */
    private static final int REQUEST_READ_PHONE_STATE = 0;
    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 1;
    private static final int REQUEST_READ_SMS = 2;

    // UI references.
    private TextInputEditText mPhoneView;
    private TextInputEditText mPasswordView;
    private Button mEmailSignInButton;
    private View mProgressView;
    private View mLoginFormView;

    // Local variables
    private SharedPreferences prefs;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SYSTEM_ALERT_WINDOW) {
            proceedAfterSystemWindowPermissions();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPhoneView = (TextInputEditText) findViewById(R.id.phone);
        mPasswordView = (TextInputEditText) findViewById(R.id.password);
        mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        prefs = getSharedPreferences(NApplication.SHARED_PREFERENCES_NAME, 0);
        proceedAfterPhoneStatePermissions();
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void unblockLogin() {
        mPhoneView.setEnabled(true);
        mPasswordView.setEnabled(true);
        mEmailSignInButton.setEnabled(true);
    }

    private void complete(String name, String picture) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USER_NAME", name);
        editor.putString("USER_PHONE", mPhoneView.getText().toString());
        editor.putString("USER_IMAGE", picture);
        editor.putBoolean("USER_LOGGED_IN", true);
        editor.commit();

        gotoHome();
    }

    private void gotoHome() {
        Intent startIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(startIntent);
        finish();
    }

    private void proceedAfterPhoneStatePermissions() {
        if (!mayRequestPhoneStateReaderStatus()) {
            return;
        }

        Utilities.logDebug("Phone state permissions available");
        proceedAfterSystemWindowPermissions();
    }

    private void proceedAfterSystemWindowPermissions() {
        if (!mayRequestSystemWindowAddStatus()) {
            return;
        }

        Utilities.logDebug("System window permissions available");
        proceedAfterReadSmsPermissions();
    }

    private void proceedAfterReadSmsPermissions() {
        if (!mayRequestSmsReadStatus()) {
            return;
        }

        Utilities.logDebug("System window permissions available");
        unblockLogin();
    }

    private boolean mayRequestPhoneStateReaderStatus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
            Snackbar.make(mPhoneView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
        return false;
    }

    private boolean mayRequestSystemWindowAddStatus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if(!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
            return false;
        }
        else return true;
    }

    private boolean mayRequestSmsReadStatus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_SMS)) {
            Snackbar.make(mPhoneView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_SMS}, REQUEST_READ_SMS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_SMS}, REQUEST_READ_SMS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_READ_PHONE_STATE) proceedAfterPhoneStatePermissions();
            else if (requestCode == REQUEST_READ_SMS) proceedAfterReadSmsPermissions();
        }
    }

    private void attemptLogin() {
        String phone = mPhoneView.getText().toString();
        if(phone.length() != 10) {
            Toast.makeText(this, "Phone number must be 10 digits.", Toast.LENGTH_LONG).show();
            return;
        }

        String password = mPasswordView.getText().toString();
        if(password.length() < 4) {
            Toast.makeText(this, "Password is minimum 4 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        showProgress(true);
        List<Pair<String, String>> post = new ArrayList<>(2);
        post.add(new Pair<>("phone", phone));
        post.add(new Pair<>("password", password));
        new APICall(this, APICall.HOST + "getloginoptions.json", "getloginoptions", null, post);
    }

    private boolean isPhoneValid(String phone) {
        //TODO: Replace this with your own logic
        return phone.length() == 10;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void apiResponse(String response, String code) {
        if(code.equalsIgnoreCase("getloginoptions")) {
            try {
                JSONObject j = new JSONObject(response);

                JSONArray businesses = j.getJSONArray("businessAllowed");
                JSONObject business = businesses.getJSONObject(0);
                int bid = business.getInt("bid");

                List<Pair<String, String>> post = new ArrayList<>(6);
                post.add(new Pair<>("phone", mPhoneView.getText().toString()));
                post.add(new Pair<>("password", mPasswordView.getText().toString()));
                post.add(new Pair<>("business_id", String.valueOf(bid)));
                post.add(new Pair<>("device_type", "android"));
                post.add(new Pair<>("model", Utilities.getModelName()));
                post.add(new Pair<>("imei", Utilities.getIMEI()));

                new APICall(this, APICall.HOST + "merchantlogin.json", "merchantlogin", null, post);
            }
            catch (Exception e) { Log.d("enmbr", "Login", e); }
        }
        else if(code.equalsIgnoreCase("merchantlogin")) {
            try {
                JSONObject j = new JSONObject(response);
                JSONObject admin = j.getJSONObject("admin");
                complete(admin.getString("name"), admin.getString("photo_url"));
            }
            catch (Exception e) { Log.d("enmbr", "Login", e); }
        }
    }

    @Override
    public void apiError(String message, String code) {
        try {
            JSONObject j = new JSONObject(message);
            if (j.has("message")) {
                String m = j.getString("message");
                Toast.makeText(this, m, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) { Toast.makeText(this, "Something is wrong! Please call your relationship manager to fix this.", Toast.LENGTH_LONG).show(); }
        showProgress(false);
    }

    @Override
    public void connectionError(int Status, String code) {
        Utilities.showAlertDialog(this, "Sorry", "Could not connect to the internet!", "Try again",
        new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.cancel();
            }
        }, "Connect to wifi",
        new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        showProgress(false);
    }
}

