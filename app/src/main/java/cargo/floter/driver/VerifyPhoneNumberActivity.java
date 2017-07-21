package cargo.floter.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.model.Driver;
import cargo.floter.driver.utils.AppConstants;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VerifyPhoneNumberActivity extends CustomActivity implements CustomActivity.ResponseCallback {


    private ProgressBar progress;
    private EditText edt_verify_number;
    private TextView munual_code, txt_submit, txt_resend, txt_mobile;
    private String detailString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone);
        munual_code = (TextView) findViewById(R.id.manually_code);
        txt_mobile = (TextView) findViewById(R.id.txt_mobile);
        txt_resend = (TextView) findViewById(R.id.txt_resend);
        txt_submit = (TextView) findViewById(R.id.txt_submit);

        detailString = getIntent().getStringExtra(AppConstants.EXTRA_2);

        setResponseListener(this);
        setTouchNClick(R.id.txt_submit);
        setTouchNClick(R.id.txt_resend);
        setTouchNClick(R.id.manually_code);
        txt_mobile.setText(getIntent().getStringExtra(AppConstants.EXTRA_1));
        munual_code.setText(Html.fromHtml("<u>Enter Manually</u>"));
        setupUiElements();

        IncomingSms.setListener(new IncomingSms.SmsListener() {
            @Override
            public void onNewOTP(final String otp) {

                progress.setVisibility(View.GONE);
                edt_verify_number.setText(otp);
                verifyOtp(otp);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IncomingSms.setListener(null);
    }

    private void verifyOtp(String otp) {
//        http://floter.in//floterapi/index.php/userapi/verifyotp?
// u_mobile=9015660024&otp=458954&detail=442aa50d-2041-11e7-929b-00163ef91450
        RequestParams p = new RequestParams();
        p.put("d_phone", getIntent().getStringExtra(AppConstants.EXTRA_1));
        p.put("otp", otp);
        p.put("detail", detailString);
        postCall(getContext(), AppConstants.BASE_URL + AppConstants.VERIFY_OTP, p, "Please wait...", 2);
    }

    private void setupUiElements() {
        progress = (ProgressBar) findViewById(R.id.progress);
        edt_verify_number = (EditText) findViewById(R.id.edt_verify_number);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.txt_submit) {
            if (edt_verify_number.getText().toString().length() < 6) {
                MyApp.showMassage(getContext(), "Please enter otp");
                edt_verify_number.setError("Enter otp");
                return;
            }
            RequestParams p = new RequestParams();
            p.put("d_phone", getIntent().getStringExtra(AppConstants.EXTRA_1));
            p.put("otp", edt_verify_number.getText().toString());
            p.put("detail", detailString);
            postCall(getContext(), AppConstants.BASE_URL + AppConstants.VERIFY_OTP, p, "Verifying otp...", 2);
        } else if (v == txt_resend) {
            RequestParams p = new RequestParams();
            p.put("d_phone", getIntent().getStringExtra(AppConstants.EXTRA_1));
            postCall(getContext(), AppConstants.BASE_URL + AppConstants.SEND_OTP, p, "Requesting otp...", 1);
        } else if (v == munual_code) {
            progress.setVisibility(View.GONE);
            edt_verify_number.setEnabled(true);
            edt_verify_number.requestFocus();
        }
    }

    private Context getContext() {
        return VerifyPhoneNumberActivity.this;
    }

    @Override
    public void onJsonObjectResponseReceived(JSONObject o, int callNumber) {
        if (callNumber == 1) {
            if (o.optString("status").equals("OK") && o.optInt("code") == 200) {
//                MyApp.showMassage(getContext(), "Verify otp");
                try {
                    detailString = o.getJSONObject("response").optString("Details");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                MyApp.showMassage(getContext(), "Error_" + o.optInt("code"));
            }
        } else {
//            {"status":"Error","code":200,"message":"Mobile Number Missing","response":null}
            if (o.optString("status").equals("OK") && o.optInt("code") == 200) {
                try {
                    JSONObject arr = o.getJSONObject("response");

                    try {
                        Driver u = new Gson().fromJson(o.getJSONObject("response").toString(), Driver.class);
                        MyApp.getApplication().writeDriver(u);
                        startActivity(new Intent(getContext(), MainActivity.class));
                        MyApp.setStatus(AppConstants.IS_LOGIN, true);
                        finishAffinity();
                    } catch (JSONException e) {
                        MyApp.popMessage("Error!", o.optString("message") + "_" + o.optInt("code"), getContext());
                        e.printStackTrace();
                    }


                } catch (JSONException e) {
                    try {
                        JSONArray arr = o.getJSONArray("response");
                        if (arr.length() == 0) {
                            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                            b.setCancelable(false);
                            b.setMessage("You are not a valid driver, please contact to Floter team");
                            b.setTitle("Error!");
                            b.setPositiveButton("Contact Now", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int i) {
                                    Intent intent = new Intent(Intent.ACTION_CALL);

                                    intent.setData(Uri.parse("tel:" + "8763121573"));
                                    if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(VerifyPhoneNumberActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 102);
                                        return;
                                    }
                                    getContext().startActivity(intent);
                                    d.dismiss();
                                }
                            });
                            b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int i) {
                                    d.dismiss();
                                }
                            });
                            b.create().show();
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        MyApp.showMassage(getContext(), o.optString("message"));
                    }

                }
            } else {

                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setCancelable(false);
                b.setMessage("You are not a valid driver, please contact to Floter team");
                b.setTitle("Error!");
                b.setPositiveButton("Contact Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int i) {
                        Intent intent = new Intent(Intent.ACTION_CALL);

                        intent.setData(Uri.parse("tel:" + "8763121573"));
                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(VerifyPhoneNumberActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 102);
                            return;
                        }
                        getContext().startActivity(intent);
                        d.dismiss();
                    }
                });
                b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int i) {
                        d.dismiss();
                    }
                });
                b.create().show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i("Permission", "Call permission has now been granted. Showing preview.");
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + "8763121573"));
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    getContext().startActivity(intent);
                }

            } else {
                Log.i("Permission", "Call permission was NOT granted.");
                MyApp.showMassage(getContext(), getString(R.string.call_permissions_not_granted));

            }
        }
    }

    @Override
    public void onJsonArrayResponseReceived(JSONArray a, int callNumber) {

    }

    @Override
    public void onErrorReceived(String error) {
        MyApp.showMassage(getContext(), error);
    }


}
