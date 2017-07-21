package cargo.floter.driver;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

//import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


/**
 * This is a common activity that all other activities of the app can extend to
 * inherit the common behaviors like setting a Theme to activity.
 */
public class CustomActivity extends AppCompatActivity implements
        OnClickListener {

    private static final String TAG = "In-App";

    /**
     * Apply this Constant as touch listener for views to provide alpha touch
     * effect. The view must have a Non-Transparent background.
     */
    public static final cargo.floter.driver.utils.TouchEffect TOUCH = new cargo.floter.driver.utils.TouchEffect();

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        //Fabric.with(this, new Crashlytics());
        setupActionBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow()
                    .addFlags(
                            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // getWindow().setStatusBarColor(getResources().getColor(R.color.main_color_dk));
        }
    }

    void complain(String message) {
        Log.e("In-App", "**** error message: " + message);
        alert(message, "Error!");
    }

    void alert(String message, String title) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message).setTitle(title);
        bld.setNeutralButton("OK", null);
        Log.d("In-App", "Showing alert dialog: " + message);
        bld.create().show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
    }

    /*
     * (non-Jav-adoc)
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    /*
     * (non-Javadoc)
	 *
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method will setup the top title bar (Action bar) content and display
     * values. It will also setup the custom background theme for ActionBar. You
     * can override this method to change the behavior of ActionBar for
     * particular Activity
     */
    protected void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar == null)
            return;
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(null);

    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {

    }

    /**
     * Sets the touch and click listeners for a view..
     *
     * @param id the id of View
     * @return the view
     */
    public View setTouchNClick(int id) {

        View v = setClick(id);
        v.setOnTouchListener(TOUCH);
        return v;
    }

    /**
     * Sets the click listener for a view.
     *
     * @param id the id of View
     * @return the view
     */
    public View setClick(int id) {

        View v = findViewById(id);
        v.setOnClickListener(this);
        return v;
    }

    public void setResponseListener(CustomActivity.ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }


    public void postCallJsonObject(Context c, String url, RequestParams params, String loadingMsg, final int callNumber) {
        if (!TextUtils.isEmpty(loadingMsg))
            cargo.floter.driver.application.MyApp.spinnerStart(c, loadingMsg);
        Log.d("URL:", url);
        Log.d("Request:", params.toString());
//        StringEntity entity = null;
//        try {
//            entity = new StringEntity(params.toString(), "UTF-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30000);
            client.post(url, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                    cargo.floter.driver.application.MyApp.spinnerStop();
                    Log.d("Response:", response.toString());
                    if (response.optString("status").equals("true")) {
                        responseCallback.onJsonObjectResponseReceived(response, callNumber);
                    } else {
                        responseCallback.onErrorReceived(response.optString("msg"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    cargo.floter.driver.application.MyApp.spinnerStop();

                    Log.d("error message:", throwable.getMessage());
                    if (statusCode == 0)
                        responseCallback.onErrorReceived(getString(R.string.timeout));
                    else
                        responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    cargo.floter.driver.application.MyApp.spinnerStop();
                    Log.d("error message:", throwable.getMessage());
                    responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }
            });
    }

    private CustomActivity.ResponseCallback responseCallback;

    public void postCall(Context c, String url, RequestParams p, String loadingMsg, final int callNumber) {
        if (!TextUtils.isEmpty(loadingMsg))
            cargo.floter.driver.application.MyApp.spinnerStart(c, loadingMsg);
        Log.d("URl:", url);
        Log.d("Request:", p.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);

        client.post(url, p, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                cargo.floter.driver.application.MyApp.spinnerStop();
                Log.d("Response:", response.toString());
                try {
                    responseCallback.onJsonObjectResponseReceived(response, callNumber);
                } catch (Exception e) {
                    responseCallback.onErrorReceived("No data available");
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                cargo.floter.driver.application.MyApp.spinnerStop();
//                Log.d("error message:", throwable.getMessage());
                if (statusCode == 0) {
                    responseCallback.onErrorReceived(getString(R.string.timeout));
                } else {
                    responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                cargo.floter.driver.application.MyApp.spinnerStop();
//                Log.d("error message:", throwable.getMessage());
                if (statusCode == 0) {
                    responseCallback.onErrorReceived(getString(R.string.timeout));
                } else {
                    responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }
            }
        });
    }

    public void normalPostCall(Context c, String url, String loadingMsg, final int callNumber) {
        if (!TextUtils.isEmpty(loadingMsg))
            cargo.floter.driver.application.MyApp.spinnerStart(c, loadingMsg);
        Log.d("URl:", url);
//        Log.d("Request:", p.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        client.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                cargo.floter.driver.application.MyApp.spinnerStop();
                Log.d("Response:", response.toString());
//                if (response.optString("status").equals("true")) {
                responseCallback.onJsonObjectResponseReceived(response, callNumber);
//                } else {
//                    responseCallback.onErrorReceived(response.optString("msg"));
//                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                cargo.floter.driver.application.MyApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                if (statusCode == 0) {
                    responseCallback.onErrorReceived(getString(R.string.timeout));
                } else {
                    responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                cargo.floter.driver.application.MyApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                if (statusCode == 0) {
                    responseCallback.onErrorReceived(getString(R.string.timeout));
                } else {
                    responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }
            }
        });
    }

    public void postForArray(Context c, String url, String loadingMsg, final int callNumber) {
        if (!TextUtils.isEmpty(loadingMsg))
            cargo.floter.driver.application.MyApp.spinnerStart(c, loadingMsg);
        Log.d("URl:", url);
//        Log.d("Request:", p.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        client.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONArray response) {
                cargo.floter.driver.application.MyApp.spinnerStop();
                Log.d("Response:", response.toString());
//                if (response.optString("status").equals("true")) {
                responseCallback.onJsonArrayResponseReceived(response, callNumber);
//                } else {
//                    responseCallback.onErrorReceived(response.optString("msg"));
//                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                cargo.floter.driver.application.MyApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                if (statusCode == 0) {
                    responseCallback.onErrorReceived(getString(R.string.timeout));
                } else {
                    responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                cargo.floter.driver .application.MyApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                if (statusCode == 0) {
                    responseCallback.onErrorReceived(getString(R.string.timeout));
                } else {
                    responseCallback.onErrorReceived(getString(R.string.something_wrong) + "_" + statusCode);
                }
            }
        });
    }


    public interface ResponseCallback {
        void onJsonObjectResponseReceived(JSONObject o, int callNumber);

        void onJsonArrayResponseReceived(JSONArray a, int callNumber);

        void onErrorReceived(String error);

    }
}
