package nmbr.merchant.caller.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import nmbr.merchant.caller.R;

public class OverlayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = 20;
        params.height = 100;
        params.width = 550;
        params.y = 10;

        this.getWindow().setAttributes(params);
    }
}
