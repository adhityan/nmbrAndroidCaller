package nmbr.merchant.caller.superclasses;

import android.content.Intent;
import android.support.v4.app.Fragment;

import nmbr.merchant.caller.libs.api.apiInterface;


public class NFragment extends Fragment implements apiInterface {
    public boolean onBackKeyPressed() {
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) { }

    @Override
    public void apiResponse(String response, String code) {

    }

    @Override
    public void apiError(String message, String code) {

    }

    @Override
    public void connectionError(int Status, String code) {

    }
}
