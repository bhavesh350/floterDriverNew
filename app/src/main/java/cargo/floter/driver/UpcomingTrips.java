package cargo.floter.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import cargo.floter.driver.CustomActivity.ResponseCallback;
import cargo.floter.driver.adapter.UpcomingTripsAdapter;
import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.model.Trip;
import cargo.floter.driver.model.TripStatus;
import cargo.floter.driver.utils.AppConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpcomingTrips extends CustomActivity implements ResponseCallback {
    private UpcomingTripsAdapter adapter;
    private RecyclerView rv_history;
    private Toolbar toolbar;
    private TextView toolbar_title;

    class C05381 extends TypeToken<List<Trip>> {
        C05381() {
        }
    }

    class C07542 extends JsonHttpResponseHandler {
        C07542() {
        }

        public void onSuccess(int statusCode, Header[] headers, String response) {
            Log.d("Response:", response.toString());
            MyApp.setStatus(AppConstants.IS_ON_TRIP, true);
            UpcomingTrips.this.startActivity(new Intent(UpcomingTrips.this.getContext(), OnTripActivity.class));
            UpcomingTrips.this.finishAffinity();
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            if (statusCode == 0) {
                MyApp.popMessage("Error!", "Timeout error, wait for other ride.", UpcomingTrips.this.getContext());
                return;
            }
            MyApp.setStatus(AppConstants.IS_ON_TRIP, true);
            UpcomingTrips.this.startActivity(new Intent(UpcomingTrips.this.getContext(), OnTripActivity.class));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResponseListener(this);
        setContentView(R.layout.activity_history);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar_title = (TextView) this.toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(this.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle((CharSequence) "");
        this.rv_history = (RecyclerView) findViewById(R.id.rc_history);
        this.toolbar_title.setText("Upcoming Requests");
        loadHistory();
    }

    private void loadHistory() {
        RequestParams p = new RequestParams();
        p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
        p.put("trip_status", TripStatus.Upcoming.name());
        postCall(getContext(), AppConstants.BASE_URL_TRIP + "gettrips", p, "Getting upcoming trips...", 1);
    }

    public void onJsonObjectResponseReceived(JSONObject o, int callNumber) {
        if (callNumber == 1 && o.optString("status").equals("OK")) {
            try {
                this.adapter = new UpcomingTripsAdapter(this, (List) new Gson().fromJson(o.getJSONArray("response").toString(), new C05381().getType()));
                this.rv_history.setLayoutManager(new LinearLayoutManager(this, 1, false));
                this.rv_history.setAdapter(this.adapter);
            } catch (JSONException e) {
                MyApp.popMessageAndFinish("Message", "No upcoming request available.", this);
                e.printStackTrace();
            } catch (Exception e2) {
                MyApp.popMessageAndFinish("Message", "No upcoming request available.", this);
                e2.printStackTrace();
            }
        } else if (o.optString("status").equals("OK") && callNumber == 5) {
            Trip t = null;
            try {
                t = (Trip) new Gson().fromJson(o.getJSONObject("response").toString(), Trip.class);
            } catch (JSONException e3) {
                e3.printStackTrace();
            }
            if (t == null) {
                MyApp.popMessage("Error!", "Trip not found...", getContext());
            } else if (t.getTrip_status().equals(TripStatus.Declined.name()) || t.getTrip_status().equals(TripStatus.Cancelled.name())) {
                finish();
            } else {
                MyApp.getApplication().writeTrip(t);
                MyApp.spinnerStart(getContext(), "Please wait...");
                RequestParams pp = new RequestParams();
                pp.put(MyApp.EXTRA_MESSAGE, "Trip Accepted");
                pp.put("trip_id", t.getTrip_id());
                pp.put("trip_status", TripStatus.Accepted.name());
                pp.put("android", t.getUser().getU_device_token());
                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(30000);
                client.post("http://floter.in/floterapi/push/RiderPushNotification?", pp, new C07542());
            }
        }
    }

    public void onJsonArrayResponseReceived(JSONArray a, int callNumber) {
    }

    public void onErrorReceived(String error) {
        MyApp.popMessage("Error!", error, getContext());
    }

    private Context getContext() {
        return this;
    }

    public void startBooking(Trip t) {
        MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, t.getTrip_id());
        startDeclineTrip(true, t);
    }

    public void cancelBooking(Trip t) {
        MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
        startDeclineTrip(false, t);
    }

    private void startDeclineTrip(boolean b, Trip t) {
        RequestParams p = new RequestParams();
        p.put("trip_id", t.getTrip_id());
        p.put("trip_date", MyApp.millsToDate2(System.currentTimeMillis()));
        if (b) {
            p.put("trip_status", TripStatus.Accepted.name());
            postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "Please wait...", 5);
            return;
        }
        p.put("trip_status", TripStatus.Declined.name());
        postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "Please wait...", 5);
        MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
    }
}
