package cargo.floter.driver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.utils.AppConstants;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends CustomActivity implements CustomActivity.ResponseCallback {
    private EditText edt_phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (MyApp.getStatus(AppConstants.IS_LOGIN)) {
            startActivity(new Intent(getContext(), MainActivity.class));
            finish();
        }
//        getHashKey();
        setupUiElements();
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS}, 1010);
        }
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, 1011);
        }

        setResponseListener(this);

    }

//    private void getHashKey() {
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.truck.floter",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }
//    }

    private void setupUiElements() {
        setTouchNClick(R.id.txt_login);
        edt_phone = (EditText) findViewById(R.id.edt_phone);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.txt_login) {
            if (TextUtils.isEmpty(edt_phone.getText().toString())) {
                edt_phone.setError("Enter Phone number");
                return;
            }
            if (edt_phone.getText().toString().length() < 10) {
                edt_phone.setError("Enter a valid phone number");
                return;
            }
//             http://floter.in/floterapi/index.php/driverapi/sendotp?d_phone=7737507050
//             http://floter.in/floterapi/index.php/driverapi/verifyotp?
// d_phone=7737507050&otp=170047&detail=75e63069-20e1-11e7-929b-00163ef91450
            RequestParams p = new RequestParams();
            p.put("d_phone", edt_phone.getText().toString());
            postCall(getContext(), AppConstants.BASE_URL + AppConstants.SEND_OTP, p, "Please wait...", 1);
        }
    }

    private Context getContext() {
        return LoginActivity.this;
    }

    @Override
    public void onJsonObjectResponseReceived(JSONObject o, int callNumber) {
        if (o.optString("status").equals("OK") && o.optInt("code") == 200) {
//            MyApp.showMassage(getContext(), "Verify otp");
            try {
                String details = o.getJSONObject("response").optString("Details");
                startActivity(new Intent(getContext(), VerifyPhoneNumberActivity.class)
                        .putExtra(AppConstants.EXTRA_1, edt_phone.getText().toString())
                        .putExtra(AppConstants.EXTRA_2, details));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            MyApp.showMassage(getContext(), "Error_" + o.optInt("code"));
        }
    }

    @Override
    public void onJsonArrayResponseReceived(JSONArray a, int callNumber) {

    }

    @Override
    public void onErrorReceived(String error) {

    }


}
