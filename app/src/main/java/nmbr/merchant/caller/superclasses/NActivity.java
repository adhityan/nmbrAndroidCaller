package nmbr.merchant.caller.superclasses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import nmbr.merchant.caller.libs.Analytics;
import nmbr.merchant.caller.libs.socketio.PubSub;
import nmbr.merchant.caller.libs.socketio.asyncInterface;

public class NActivity extends AppCompatActivity {
    public NActivity() {
        super();

        Analytics.get().screen(this.getClass().getSimpleName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (this instanceof asyncInterface) {
            PubSub.subscribe((asyncInterface) this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this instanceof asyncInterface) {
            PubSub.unsubscribe();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
    }
}