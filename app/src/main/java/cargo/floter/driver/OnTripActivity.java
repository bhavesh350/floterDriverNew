package cargo.floter.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mancj.slideup.SlideUp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.fragments.FragmentDrawer;
import cargo.floter.driver.model.Payment;
import cargo.floter.driver.model.RateCard;
import cargo.floter.driver.model.Trip;
import cargo.floter.driver.model.TripStatus;
import cargo.floter.driver.utils.AppConstants;
import cargo.floter.driver.utils.LocationProvider;
import cz.msebera.android.httpclient.Header;

public class OnTripActivity extends CustomActivity implements CustomActivity.ResponseCallback,
        FragmentDrawer.FragmentDrawerListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, LocationProvider.LocationCallback, LocationProvider.PermissionCallback {


    protected static final String TAG = "MainActivity";
    private Button btn_arrived;
    private Button btn_call_user;
    private Button btn_loading_done;
    private Button btn_reached;
    private Button btn_start;
    private Button btn_stop;
    private Button btn_submit;
    private Button btn_unloading_done;
    private Location currentLocation = null;
    private Marker currentLocationMarker = null;
    private Trip currentTrip;
    private Marker destMarker = null;
    private FragmentDrawer drawerFragment;
    IntentFilter filter = new IntentFilter("cargo.floter.driver.RIDE");
    private GoogleApiClient googleApiClient;
    private boolean isCalculation = false;
    private boolean isOnceMarkerSet = false;
    private LinearLayout ll_feedback;
    private LocationProvider locationProvider;
    private LatLng mCenterLatLong;
    private GoogleMap mMap;
    private Toolbar mToolbar;
    private SupportMapFragment mapFragment;
    Payment payment = null;
    private RadioGroup radio_group;
    private RatingBar rating_bar;
    BroadcastReceiver receiver = new C02303();
    private SlideUp slideUp;
    private Marker sourceMarker = null;
    private TextView txt_direction;
    private TextView txt_pay_mode;
    private TextView txt_cancel;
    private TextView txt_rating_status;
    private TextView txt_trip_payment;
    private TextView txt_user_name;
    private Handler locationHandler = new Handler();
    private boolean isInvoiceGenerated = false;

    class C02292 implements RatingBar.OnRatingBarChangeListener {
        C02292() {
        }

        public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
            if (v <= 1.0f) {
                txt_rating_status.setText("Bad");
            } else if (v > 1.0f && ((double) v) < 2.5d) {
                txt_rating_status.setText("Below Average");
            } else if (((double) v) >= 2.5d && ((double) v) < 3.5d) {
                txt_rating_status.setText("Average");
            } else if (((double) v) < 3.5d || ((double) v) >= 4.5d) {
                txt_rating_status.setText("Excellent");
            } else {
                txt_rating_status.setText("Good");
            }
        }
    }

    class C02303 extends BroadcastReceiver {
        C02303() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("TYPE").equals("CANCELLED")) {
                currentTrip.setTrip_status(TripStatus.Cancelled.name());
                MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
                MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
                startActivity(new Intent(getContext(), MainActivity.class));
                MyApp.setSharedPrefString("button", "");
            }
        }
    }

    public class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) new URL(mapsApiDirectionsUrl).openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String str = "";
                while (true) {
                    str = br.readLine();
                    if (str == null) {
                        break;
                    }
                    sb.append(str);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.d("Exception url", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private ParserTask() {
        }

        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                routes = new PathJSONParser().parse(new JSONObject(jsonData[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            PolylineOptions polyLineOptions = null;
            String distance = "";
            String duration = "";
            for (int i = 0; i < routes.size(); i++) {
                ArrayList<LatLng> points = new ArrayList();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = (List) routes.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = (HashMap) path.get(j);
                    if (j == 0) {
                        distance = point.get("distance");
                    } else if (j == 1) {
                        duration = point.get("duration");
                    } else {
                        points.add(new LatLng(Double.parseDouble(point.get("lat")), Double.parseDouble(point.get("lng"))));
                    }
                }
                polyLineOptions.addAll(points);
                polyLineOptions.width(10.0f);
                polyLineOptions.color(Color.parseColor("#156CB3"));
            }
            float f = 0.0f;
            try {
                f = Float.parseFloat(distance) / 1000.0f;
                f = ((float) Math.round(100.0f * f)) / 100.0f;
            } catch (Exception e) {
            }
            if (f < 3.0f) {
                f = 2.0f;
            }
            RateCard.RateCardResponse r = null;
            try {
                String carId = MyApp.getApplication().readDriver().getCar_id();
                for (RateCard.RateCardResponse rr : MyApp.getApplication().readRateCard().getResponse()) {
                    if (rr.getCar_id().equals(carId)) {
                        r = rr;
                    }
                }
            } catch (Exception e2) {
            }
            MyApp.spinnerStop();
            RequestParams p;
            if (r == null) {
//                MyApp.showMassage(getContext(),"Rate card is null and fare calculated as old");
                if (isCalculation) {
                    p = new RequestParams();
                    p.put("trip_id", currentTrip.getTrip_id());
                    p.put("driver_id", currentTrip.getDriver_id());
                    p.put("user_id", currentTrip.getUser_id());
                    p.put("u_fname", currentTrip.getUser().getU_fname() + " " + currentTrip.getUser().getU_lname());
                    p.put("d_fname", currentTrip.getDriver().getD_name());
                    p.put("pay_date", currentTrip.getTrip_modified().split(" ")[0]);
                    p.put("pay_mode", currentTrip.getTrip_pay_mode());
                    int payAmount = 0;
                    try {
                        payAmount = Integer.parseInt(currentTrip.getTrip_pay_amount());
                    } catch (Exception e) {
                    }
                    p.put("pay_amount", payAmount);
//                p.put("trip_fare", payAmount);
                    p.put("pay_status", "PENDING");
                    p.put("promo_id", "");
                    p.put("order_id", "ORDER_" + currentTrip.getTrip_id());
                    p.put("transaction_id", "00000" + currentTrip.getTrip_id());
                    p.put("pay_promo_code", currentTrip.getTrip_promo_code());
                    if (currentTrip.getTrip_promo_code().equals("FIRST50")) {
                        p.put("pay_promo_amt", "50");
                        payAmount = payAmount - 50;
                        p.put("pay_amount", payAmount);
                    } else if (currentTrip.getTrip_promo_code().equals("FLOTER05")) {
                        p.put("pay_promo_amt", (payAmount - (int) (payAmount * 0.05)) + "");
                        payAmount = payAmount - (int) (payAmount * 0.05);
                        p.put("pay_amount", payAmount);
                    }

                    postCall(getContext(), AppConstants.BASE_PAYMENT + "save?", p, "Creating invoice...", 7);
                }
            } else if (isCalculation) {
                int charge = Integer.parseInt(r.getBase_fare());
                if (f == 0) {
                    f = (float) MyApp.distance(sourceMarker.getPosition().latitude, sourceMarker.getPosition().longitude
                            , currentLocationMarker.getPosition().latitude, currentLocationMarker.getPosition().longitude);
//                    if (f < 4) {
//                        f = 1;
//                    }
                }
                if (f > 2.0f) {
                    try {
                        charge = (int) (((float) charge) + ((f - 2.0f) * ((float) Integer.parseInt(r.getPrice_per_km()))));
                    } catch (Exception e3) {
                    }
                }
                int freeTime = 0;
                int loadUnloadTime = loadingUnloadingTime();
                try {
                    freeTime = Integer.parseInt(r.getFree_load_unload_time());
                } catch (Exception e4) {
                }
                if (loadUnloadTime > freeTime) {
                    try {
                        charge += Integer.parseInt(r.getCharge_after_free_time()) * (loadUnloadTime - freeTime);
                    } catch (Exception e5) {
                    }
                }
                int tripFare = charge;
                charge += (int) (((float) charge) * 0.05f);
//                MyApp.showMassage(getContext(),"distance is: "+distance+" and fare is: "+charge);
                p = new RequestParams();
                p.put("trip_id", currentTrip.getTrip_id());
                p.put("trip_fare", tripFare);
                p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
                p.put("trip_pay_amount", charge);
                p.put("trip_actual_drop_lat", Double.valueOf(currentLocation.getLatitude()));
                p.put("trip_actual_drop_lng", Double.valueOf(currentLocation.getLongitude()));
                postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "Please wait...", 3);
            }
            try {
                mMap.addPolyline(polyLineOptions);
            } catch (Exception e) {
            }

        }
    }

    public class PathJSONParser {
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList();
            try {
                JSONArray jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList();
                    for (int j = 0; j < jLegs.length(); j++) {
                        JSONObject jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                        HashMap<String, String> hmDistance = new HashMap();
                        hmDistance.put("distance", jDistance.getString(FirebaseAnalytics.Param.VALUE));
                        JSONObject jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                        HashMap<String, String> hmDuration = new HashMap();
                        hmDuration.put("duration", jDuration.getString(FirebaseAnalytics.Param.VALUE));
                        path.add(hmDistance);
                        path.add(hmDuration);
                        JSONArray jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            List<LatLng> list = decodePoly((String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points"));
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap();
                                hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList();
            int index = 0;
            int len = encoded.length();
            int lat = 0;
            int lng = 0;
            while (index < len) {
                int index2;
                int shift = 0;
                int result = 0;
                int b = 0;
                while (true) {
                    index2 = index + 1;
                    b = encoded.charAt(index) - 63;
                    result |= (b & 31) << shift;
                    shift += 5;
                    if (b < 32) {
                        break;
                    }
                    index = index2;
                }
                lat += (result & 1) != 0 ? (result >> 1) ^ -1 : result >> 1;
                shift = 0;
                result = 0;
                index = index2;
                while (true) {
                    index2 = index + 1;
                    b = encoded.charAt(index) - 63;
                    result |= (b & 31) << shift;
                    shift += 5;
                    if (b < 32) {
                        break;
                    }
                    index = index2;
                }
                lng += (result & 1) != 0 ? (result >> 1) ^ -1 : result >> 1;
                poly.add(new LatLng(((double) lat) / 100000.0d, ((double) lng) / 100000.0d));
                index = index2;
            }
            return poly;
        }
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        private ReadTask() {
        }

        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = new MapHttpConnection().readUr(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(new String[]{result});
        }
    }

    class C05331 implements SlideUp.Listener {
        C05331() {
        }

        public void onSlide(float percent) {
        }

        public void onVisibilityChanged(int visibility) {
            if (visibility != 8) {
            }
        }
    }

    class C05348 implements GoogleApiClient.OnConnectionFailedListener {
        C05348() {
        }

        public void onConnectionFailed(ConnectionResult connectionResult) {
            MyApp.showMassage(getContext(), "Location error " + connectionResult.getErrorCode());
        }
    }

    class C05359 implements ResultCallback<LocationSettingsResult> {
        C05359() {
        }

        public void onResult(LocationSettingsResult result) {
            Status status = result.getStatus();
            switch (status.getStatusCode()) {
                case 6:
                    try {
                        status.startResolutionForResult(OnTripActivity.this, 44);
                        return;
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                        return;
                    }
                default:
                    return;
            }
        }
    }

    class C07504 extends JsonHttpResponseHandler {
        C07504() {
        }

        public void onSuccess(int statusCode, Header[] headers, String response) {
            Log.d("Response:", response.toString());
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        }
    }

    class C07515 extends JsonHttpResponseHandler {
        C07515() {
        }

        public void onSuccess(int statusCode, Header[] headers, String response) {
            Log.d("Response:", response.toString());
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        }
    }

    class C07526 extends JsonHttpResponseHandler {
        C07526() {
        }

        public void onSuccess(int statusCode, Header[] headers, String response) {
            Log.d("Response:", response.toString());
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        }
    }

    class C07537 extends JsonHttpResponseHandler {
        C07537() {
        }

        public void onSuccess(int statusCode, Header[] headers, String response) {
            MyApp.spinnerStop();
            Log.d("Response:", response.toString());
            btn_loading_done.setVisibility(View.VISIBLE);
            btn_arrived.setVisibility(View.GONE);
            MyApp.setSharedPrefString("button", "btn_arrived");
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
            MyApp.spinnerStop();
        }

        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
            MyApp.spinnerStop();
        }

        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            MyApp.spinnerStop();
            MyApp.setSharedPrefString("button", "");
            if (statusCode == 0) {
                btn_loading_done.setVisibility(View.VISIBLE);
                btn_arrived.setVisibility(View.GONE);
                txt_direction.setText("Show Direction to Destination");
                return;
            }
            btn_loading_done.setVisibility(View.VISIBLE);
            btn_arrived.setVisibility(View.GONE);
            txt_direction.setText("Show Direction to Destination");
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main_on_trip);
        this.mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(this.mToolbar);
        this.mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        this.locationProvider = new LocationProvider(this, this, this);
        this.currentTrip = MyApp.getApplication().readTrip();
        setupUiElements();
        this.mapFragment.getMapAsync(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("");
        setResponseListener(this);
        this.drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        this.drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), this.mToolbar);
        this.drawerFragment.setDrawerListener(this);
        onNewIntent(getIntent());
    }

    private void setupUiElements() {
        this.radio_group = (RadioGroup) findViewById(R.id.radio_group);
        this.txt_rating_status = findViewById(R.id.txt_rating_status);
        this.txt_pay_mode = findViewById(R.id.txt_pay_mode);
        this.txt_cancel = findViewById(R.id.txt_cancel);
        setTouchNClick(R.id.txt_cancel);
        this.txt_trip_payment = findViewById(R.id.txt_trip_payment);
        this.txt_user_name = findViewById(R.id.txt_user_name);
        this.txt_direction = findViewById(R.id.txt_direction);
        this.btn_call_user = findViewById(R.id.btn_call_user);
        this.btn_arrived = findViewById(R.id.btn_arrived);
        this.btn_unloading_done = findViewById(R.id.btn_unloading_done);
        this.btn_reached = findViewById(R.id.btn_reached);
        this.btn_loading_done = findViewById(R.id.btn_loading_done);
        this.btn_start = findViewById(R.id.btn_start);
        this.btn_stop = findViewById(R.id.btn_stop);
        this.btn_submit = findViewById(R.id.btn_submit);
        this.rating_bar = findViewById(R.id.rating_bar);
        this.ll_feedback = findViewById(R.id.ll_feedback);
        setTouchNClick(R.id.btn_arrived);
        setTouchNClick(R.id.btn_unloading_done);
        setTouchNClick(R.id.btn_reached);
        setTouchNClick(R.id.btn_loading_done);
        setTouchNClick(R.id.btn_start);
        setTouchNClick(R.id.btn_stop);
        setTouchNClick(R.id.btn_submit);
        setTouchNClick(R.id.txt_direction);
        setTouchNClick(R.id.btn_call_user);
        try {
            this.txt_user_name.setText("You are on ride with\n" + this.currentTrip.getUser().getU_fname() + " " + this.currentTrip.getUser().getU_lname());
        } catch (Exception e) {
            MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
            startActivity(new Intent(getContext(), MainActivity.class));
            MyApp.setSharedPrefString("button", "");
            finishAffinity();
        }
        this.slideUp = new SlideUp.Builder(this.ll_feedback).withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.BOTTOM).build();
        this.slideUp = new SlideUp.Builder(this.ll_feedback).withListeners(new C05331())
                .withStartGravity(Gravity.BOTTOM).withGesturesEnabled(false).withStartState(SlideUp.State.HIDDEN).build();
        this.rating_bar.setOnRatingBarChangeListener(new C02292());
    }

    protected void onStart() {
        super.onStart();
        MyApp.setStatus(AppConstants.IS_OPEN, true);
        registerReceiver(this.receiver, this.filter);
        this.locationProvider.connect();
    }

    protected void onResume() {
        super.onResume();
        MyApp.setStatus(AppConstants.IS_OPEN, true);
        registerReceiver(this.receiver, this.filter);
        if (!MyApp.isLocationEnabled(getContext())) {
            enableGPS();
        }
        if (this.currentTrip.getTrip_status().equals(TripStatus.Accepted.name())) {
            this.btn_arrived.setVisibility(View.VISIBLE);
            this.txt_direction.setText("Show Direction to User");
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.OnGoing.name())) {
            this.txt_direction.setText("Show Direction to Destination");
            this.btn_start.setVisibility(View.GONE);
            MyApp.setSharedPrefString("button", "btn_start");
            this.btn_reached.setVisibility(View.VISIBLE);
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.Finished.name())) {
            if (isInvoiceGenerated) {
                MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
                MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
                startActivity(new Intent(getContext(), MainActivity.class));
                MyApp.setSharedPrefString("button", "");
            } else {
                this.btn_unloading_done.setVisibility(View.GONE);
                this.btn_loading_done.setVisibility(View.GONE);
                this.btn_arrived.setVisibility(View.GONE);
                MyApp.setSharedPrefString("button", "btn_unloading_done");
                this.btn_stop.setVisibility(View.VISIBLE);
            }
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.Cancelled.name())) {
            MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
            MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            startActivity(new Intent(getContext(), MainActivity.class));
            MyApp.setSharedPrefString("button", "");
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.Driver_Cancel.name())) {
            MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
            MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            startActivity(new Intent(getContext(), MainActivity.class));
            MyApp.setSharedPrefString("button", "");
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.Reported.name())) {
            MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
            MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            startActivity(new Intent(getContext(), MainActivity.class));
            MyApp.setSharedPrefString("button", "");
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.Loading.name())) {
            this.btn_start.setVisibility(View.VISIBLE);
            this.btn_arrived.setVisibility(View.GONE);
            MyApp.setSharedPrefString("button", "btn_arrived");
            this.btn_stop.setVisibility(View.GONE);
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.Unloading.name())) {
            this.btn_unloading_done.setVisibility(View.GONE);
            this.btn_loading_done.setVisibility(View.GONE);
            this.btn_arrived.setVisibility(View.GONE);
            MyApp.setSharedPrefString("button", "btn_unloading_done");
            this.btn_stop.setVisibility(View.VISIBLE);
        } else if (this.currentTrip.getTrip_status().equals(TripStatus.Reached.name())) {
            this.btn_unloading_done.setVisibility(View.VISIBLE);
            this.btn_loading_done.setVisibility(View.GONE);
            this.btn_arrived.setVisibility(View.GONE);
            this.btn_stop.setVisibility(View.GONE);
            MyApp.setSharedPrefString("button", "btn_arrived");
        }
    }

    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey("TYPE") && intent.getStringExtra("TYPE").equals("CANCELLED")) {
            this.currentTrip.setTrip_status(TripStatus.Cancelled.name());
            MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
            startActivity(new Intent(getContext(), MainActivity.class));
            MyApp.setSharedPrefString("button", "");
        }
    }

    protected void onPause() {
        super.onPause();
        MyApp.setStatus(AppConstants.IS_OPEN, false);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        MyApp.showMassage(getContext(), "You are on trip, you cannot go back till finish the trip.\nThank you.");
    }

    public void onDestroy() {
        super.onDestroy();
        MyApp.setStatus(AppConstants.IS_OPEN, false);
        locationHandler.removeCallbacks(locationCallback);
    }

    protected void onStop() {
        super.onStop();
        MyApp.setStatus(AppConstants.IS_OPEN, false);
        this.locationProvider.disconnect();
        unregisterReceiver(this.receiver);
    }

    public void onClick(View v) {
        super.onClick(v);
        if (!MyApp.isConnectingToInternet(getContext())) {
            MyApp.popMessage("Alert!", "Please connect to working internet connection.", getContext());
        } else if (v.getId() == R.id.btn_arrived) {
            sendArrivalNotificationToUser();
            MyApp.setSharedPrefLong(AppConstants.TRIP_LOAD_START + this.currentTrip.getTrip_id(), System.currentTimeMillis());
            MyApp.setSharedPrefString("button", "btn_arrived");

        } else if (v.getId() == R.id.btn_start) {
            changeTripStatus(TripStatus.OnGoing.name());
            MyApp.setSharedPrefString("button", "btn_start");

            RequestParams p = new RequestParams();
            p.put("trip_id", MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID));
            p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
            if (currentLocation != null) {
                p.put("scheduled_pick_lat", currentLocation.getLatitude() + "");
                p.put("scheduled_pick_lng", currentLocation.getLongitude() + "");
                postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "", 25);
            }

        } else if (v.getId() == R.id.btn_stop) {
            RequestParams p = new RequestParams();
            p.put("trip_id", this.currentTrip.getTrip_id());
            p.put("driver_id", this.currentTrip.getDriver_id());
            p.put("user_id", this.currentTrip.getUser_id());
//            p.put("trip_status", TripStatus.Finished.name());
            postCall(getContext(), AppConstants.BASE_URL_TRIP + "endtrip", p, "Please wait...", 11);
            MyApp.setSharedPrefString("button", "btn_stop");
        } else if (v.getId() == R.id.btn_call_user) {
            Intent intent = new Intent("android.intent.action.DIAL");
            intent.setData(Uri.parse("tel:" + this.currentTrip.getUser().getU_mobile()));
            startActivity(intent);
        } else if (v == this.txt_direction) {
            Intent mapIntent;
            if (this.txt_direction.getText().toString().contains("User")) {
                mapIntent = new Intent("android.intent.action.VIEW", Uri.parse("google.navigation:q=" + this.currentTrip.getTrip_from_loc()));
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                return;
            }
            mapIntent = new Intent("android.intent.action.VIEW", Uri.parse("google.navigation:q=" + this.currentTrip.getTrip_to_loc()));
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else if (v == this.btn_submit) {
            rateUserAndFinish();
            MyApp.setSharedPrefString("button", "");
        } else if (v == this.btn_unloading_done) {
            MyApp.setSharedPrefLong(AppConstants.TRIP_UNLOAD_END + this.currentTrip.getTrip_id(), System.currentTimeMillis());
            this.btn_stop.setVisibility(View.VISIBLE);
            changeTripStatus(TripStatus.Unloading.name());
            MyApp.setSharedPrefString("button", "btn_unloading_done");
        } else if (v == this.btn_loading_done) {
            MyApp.setSharedPrefLong(AppConstants.TRIP_LOAD_END + this.currentTrip.getTrip_id(), System.currentTimeMillis());
            this.btn_start.setVisibility(View.VISIBLE);
            changeTripStatus(TripStatus.Loading.name());
            MyApp.setSharedPrefString("button", "btn_loading_done");
        } else if (v == this.btn_reached) {
            this.btn_reached.setVisibility(View.GONE);
            this.btn_unloading_done.setVisibility(View.VISIBLE);
            changeTripStatus(TripStatus.Reached.name());
            MyApp.setSharedPrefLong(AppConstants.TRIP_UNLOAD_START + this.currentTrip.getTrip_id(), System.currentTimeMillis());
            MyApp.setSharedPrefString("button", "btn_reached");
        } else if (v == txt_cancel) {
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Cancel Trip").setMessage("Are you sure that you want to cancel trip?\nMake sure it will reflect as " +
                    "a bad impact to your profile. So avoid cancellation by your end.\nThank you")
                    .setPositiveButton("YES, Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            d.cancel();
                            changeTripStatus(TripStatus.Driver_Cancel.name());
                        }
                    }).setNegativeButton("Don't Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    d.cancel();
                }
            }).create().show();
        }
    }

    private void rateUserAndFinish() {
        String selectedText = ((RadioButton) this.radio_group.getChildAt
                (this.radio_group.indexOfChild(this.radio_group
                        .findViewById(this.radio_group.getCheckedRadioButtonId())))).getText().toString();
        RequestParams p = new RequestParams();
        p.put("trip_id", MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID));
        p.put("trip_status", TripStatus.Finished.name());
        p.put("trip_feedback", selectedText);
        p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
        p.put("trip_rating", this.rating_bar.getRating() + "");
        postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "Please wait...", 22);
    }

    private int loadingUnloadingTime() {
        return (int) TimeUnit.MILLISECONDS.toMinutes((MyApp.getSharedPrefLong(AppConstants.TRIP_LOAD_END + this.currentTrip.getTrip_id())
                - MyApp.getSharedPrefLong(AppConstants.TRIP_LOAD_START + this.currentTrip.getTrip_id()))
                + (MyApp.getSharedPrefLong(AppConstants.TRIP_UNLOAD_END + this.currentTrip.getTrip_id())
                - MyApp.getSharedPrefLong(AppConstants.TRIP_UNLOAD_START + this.currentTrip.getTrip_id())));
    }


    private void calculateFinalFareStatusAndShowInvoice() {
        locationProvider = new LocationProvider(this, this, this);
        locationProvider.connect();
        isOnceMarkerSet = false;
        this.isCalculation = true;
//        String url = getMapsApiDirectionsUrl(this.sourceMarker.getPosition(), this.currentLocationMarker.getPosition());
//        new ReadTask().execute(new String[]{url});
        MyApp.spinnerStart(getContext(), "Calculating Fair Details...");
    }

    private void changeTripStatus(String trip_status) {
        RequestParams p = new RequestParams();
        p.put("trip_id", this.currentTrip.getTrip_id());
        p.put("trip_status", trip_status);
        p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
        postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "Please wait...", 5);
        RequestParams pp;
        AsyncHttpClient client;
        if (trip_status.equals(TripStatus.OnGoing.name())) {
            pp = new RequestParams();
            pp.put(MyApp.EXTRA_MESSAGE, "You are on ride with\n" + this.currentTrip.getDriver().getD_name() + "\n" +
                    this.currentTrip.getDriver().getTruck_reg_no());
            pp.put("trip_id", this.currentTrip.getTrip_id());
            pp.put("trip_status", trip_status);
            pp.put("android", this.currentTrip.getUser().getU_device_token());
            client = new AsyncHttpClient();
            client.setTimeout(30000);
            client.post("http://floter.in/floterapi/push/RiderPushNotification.php?", pp, new C07504());
        } else if (trip_status.equals(TripStatus.Loading.name())) {
            pp = new RequestParams();
            pp.put(MyApp.EXTRA_MESSAGE, "Loading goods for your trip is done\nDriver is all set to start the trip now.");
            pp.put("trip_id", this.currentTrip.getTrip_id());
            pp.put("trip_status", trip_status);
            pp.put("android", this.currentTrip.getUser().getU_device_token());
            client = new AsyncHttpClient();
            client.setTimeout(30000);
            client.post("http://floter.in/floterapi/push/RiderPushNotification.php?", pp, new C07515());
        } else if (trip_status.equals(TripStatus.Reached.name())) {
            pp = new RequestParams();
            pp.put(MyApp.EXTRA_MESSAGE, "Driver Reached at your destination.\nPlease proceed for unloading of your goods.");
            pp.put("trip_id", this.currentTrip.getTrip_id());
            pp.put("trip_status", trip_status);
            pp.put("android", this.currentTrip.getUser().getU_device_token());
            client = new AsyncHttpClient();
            client.setTimeout(30000);
            client.post("http://floter.in/floterapi/push/RiderPushNotification.php?", pp, new C07526());
        } else if (trip_status.equals(TripStatus.Driver_Cancel.name())) {
            pp = new RequestParams();
            pp.put(MyApp.EXTRA_MESSAGE, "This trip has been cancelled by driver, please reach to us if it's not requested by you.");
            pp.put("trip_id", this.currentTrip.getTrip_id());
            pp.put("trip_status", trip_status);
            pp.put("android", this.currentTrip.getUser().getU_device_token());
            client = new AsyncHttpClient();
            client.setTimeout(30000);
            client.post("http://floter.in/floterapi/push/RiderPushNotification.php?", pp, new C07526());
        }
    }

    private void sendArrivalNotificationToUser() {
        MyApp.spinnerStart(getContext(), "Please wait...");
        RequestParams pp = new RequestParams();
        pp.put(MyApp.EXTRA_MESSAGE, "Driver arrived to your location.\nPlease process you loading into truck");
        pp.put("android", this.currentTrip.getUser().getU_device_token());
        pp.put("trip_id", this.currentTrip.getTrip_id());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        client.post("http://floter.in/floterapi/push/RiderPushNotification.php?", pp, new C07537());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MyApp.spinnerStop();
            }
        }, 30000);
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        MyApp.cancelNotification(ctx, notifyId);
    }

    public void onDrawerItemSelected(View view, int position) {
        if (position == 6) {
            MyApp.popMessage("Message!", "You are on a trip, so you cannot logout.", getContext());
//            MyApp.setStatus(AppConstants.IS_LOGIN, false);
//            startActivity(new Intent(this, LoginActivity.class));
//            finishAffinity();
        } else if (position == 3) {
            startActivity(new Intent(getContext(), ProfileActivity.class));
        } else if (position == 1) {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        } else if (position == 2) {
            MyApp.popMessage("Message!", "You are on a trip, so you cannot go for upcoming trips.", getContext());
        } else if (position == 5) {
            startActivity(new Intent(getContext(), SupportActivity.class));
        } else if (position == 4) {
            startActivity(new Intent(getContext(), PaymentsActivity.class).putExtra(AppConstants.EXTRA_1, false));
        }
    }

    public void enableGPS() {
        if (this.googleApiClient == null) {
            this.googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addOnConnectionFailedListener(new C05348()).build();
            this.googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(30000);
            locationRequest.setFastestInterval(FusedLocationWithSettingsDialog.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            LocationServices.SettingsApi.checkLocationSettings(this.googleApiClient, builder.build()).setResultCallback(new C05359());
            return;
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(FusedLocationWithSettingsDialog.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        LocationServices.SettingsApi.checkLocationSettings(this.googleApiClient, builder.build()).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            public void onResult(LocationSettingsResult result) {
                Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case 6:
                        try {
                            status.startResolutionForResult(OnTripActivity.this, 44);
                            return;
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            return;
                        }
                    default:
                        return;
                }
            }
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        this.mMap = googleMap;
        View mapView = this.mapFragment.getView();
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        if (!(mapView == null || mapView.findViewById(Integer.parseInt("1")) == null)) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(10, 0);
            layoutParams.addRule(12, -1);
            layoutParams.setMargins(0, 0, 30, 150);
        }
        this.mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("Camera postion change", cameraPosition + "");
                mCenterLatLong = cameraPosition.target;
                try {
                    Location mLocation = new Location("");
                    mLocation.setLatitude(mCenterLatLong.latitude);
                    mLocation.setLongitude(mCenterLatLong.longitude);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        double sourceLat = Double.parseDouble(this.currentTrip.getTrip_scheduled_pick_lat());
        double sourceLng = Double.parseDouble(this.currentTrip.getTrip_scheduled_pick_lng());
        double destLat = Double.parseDouble(this.currentTrip.getTrip_scheduled_drop_lat());
        double destLng = Double.parseDouble(this.currentTrip.getTrip_scheduled_drop_lng());
        if (this.sourceMarker != null) {
            this.sourceMarker.remove();
        }
        if (this.destMarker != null) {
            this.destMarker.remove();
        }
        this.sourceMarker = this.mMap.addMarker(new MarkerOptions().position(new LatLng(sourceLat, sourceLng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)));
        this.destMarker = this.mMap.addMarker(new MarkerOptions().position(new LatLng(destLat, destLng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_red)));
        this.destMarker.setSnippet(this.currentTrip.getTrip_from_loc());
        this.sourceMarker.setSnippet(this.currentTrip.getTrip_to_loc());

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void handleNewLocation(Location location) {
        if (location != null) {
            try {
                this.currentLocation = location;
                if (this.currentLocationMarker != null) {
                    this.currentLocationMarker.remove();
                    this.currentLocationMarker = null;
                }
                this.currentLocationMarker = this.mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_driver)));
                changeMap(location);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(this.sourceMarker.getPosition());
                builder.include(this.destMarker.getPosition());
                this.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(adjustBoundsForMaxZoomLevel(builder.build()), 100));
                if (!this.isOnceMarkerSet) {
                    if (isCalculation) {
                        this.isOnceMarkerSet = true;
                        String url = getMapsApiDirectionsUrl(this.sourceMarker.getPosition(), this.currentLocationMarker.getPosition());
                        OnTripActivity onTripActivity = this;
                        new ReadTask().execute(new String[]{url});
                    } else {
                        this.isOnceMarkerSet = true;
                        String url = getMapsApiDirectionsUrl(this.sourceMarker.getPosition(), this.destMarker.getPosition());
                        OnTripActivity onTripActivity = this;
                        new ReadTask().execute(new String[]{url});
                    }

                }
                RequestParams pp = new RequestParams();
                pp.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
                pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
                pp.put("d_lat", this.currentLocation.getLatitude() + "");
                pp.put("d_is_available", 0);
                pp.put("d_lng", this.currentLocation.getLongitude() + "");
                pp.put("d_degree", Float.valueOf(location.getBearing()));
                postCall(getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            locationHandler.postDelayed(locationCallback, 30000);
        }
    }

    private Context getContext() {
        return this;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1010 && ContextCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_FINE_LOCATION") == -1) {
            MyApp.showMassage(getContext(), "Location Denied, you cannot use the app");
        }
    }

    private void changeMap(Location location) {
        if (this.mMap != null) {
            this.mMap.getUiSettings().setZoomControlsEnabled(false);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15.5f).tilt(0.0f).build();
            if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
                this.mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1010);
            }
            this.mMap.getUiSettings().setMyLocationButtonEnabled(true);
            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            return;
        }
        Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
    }

    public void handleManualPermission() {
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1010);
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

//    private String tripFare = "";

    public void onJsonObjectResponseReceived(JSONObject o, int callNumber) {
        Trip t;
        RequestParams p;
        if (o.optString("status").equals("OK") && callNumber == 3) {
            t = null;
            try {
                t = new Gson().fromJson(o.getJSONObject("response").toString(), Trip.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (t == null) {
                p = new RequestParams();
                p.put("trip_id", this.currentTrip.getTrip_id());
                p.put("driver_id", this.currentTrip.getDriver_id());
                p.put("user_id", this.currentTrip.getUser_id());
                p.put("u_fname", this.currentTrip.getUser().getU_fname() + " " + this.currentTrip.getUser().getU_lname());
                p.put("d_fname", this.currentTrip.getDriver().getD_name());
                p.put("pay_date", this.currentTrip.getTrip_modified().split(" ")[0]);
                p.put("pay_mode", this.currentTrip.getTrip_pay_mode());
                int payAmount = 0;
                try {
                    payAmount = Integer.parseInt(currentTrip.getTrip_pay_amount());
                } catch (Exception e) {
                }
                p.put("pay_amount", payAmount);

                p.put("pay_status", "PENDING");
                p.put("promo_id", "");
                p.put("order_id", "ORDER_" + this.currentTrip.getTrip_id());
                p.put("transaction_id", "00000" + this.currentTrip.getTrip_id());
                p.put("pay_promo_code", this.currentTrip.getTrip_promo_code());
                p.put("pay_promo_amt", "");
                if (currentTrip.getTrip_promo_code().equals("FIRST50")) {
                    p.put("pay_promo_amt", "50");
                    payAmount = payAmount - 50;
                    p.put("pay_amount", payAmount);
                } else if (currentTrip.getTrip_promo_code().equals("FLOTER05")) {
                    p.put("pay_promo_amt", (payAmount - (int) (payAmount * 0.05)) + "");
                    payAmount = payAmount - (int) (payAmount * 0.05);
                    p.put("pay_amount", payAmount);
                }
                postCall(getContext(), AppConstants.BASE_PAYMENT + "save?", p, "Creating invoice...", 7);
                return;
            }
            this.currentTrip = t;
            MyApp.getApplication().writeTrip(t);
            p = new RequestParams();
            p.put("trip_id", this.currentTrip.getTrip_id());
            p.put("driver_id", this.currentTrip.getDriver_id());
            p.put("user_id", this.currentTrip.getUser_id());
            p.put("u_fname", this.currentTrip.getUser().getU_fname() + " " + this.currentTrip.getUser().getU_lname());
            p.put("d_fname", this.currentTrip.getDriver().getD_name());
            p.put("pay_date", this.currentTrip.getTrip_modified().split(" ")[0]);
            p.put("pay_mode", this.currentTrip.getTrip_pay_mode());
            int payAmount = 0;
            try {
                payAmount = Integer.parseInt(currentTrip.getTrip_pay_amount());

            } catch (Exception e) {
            }
//            tripFare = payAmount + "";
            p.put("pay_amount", payAmount);
            p.put("pay_status", "PENDING");
            p.put("promo_id", "");
            p.put("order_id", "ORDER_" + this.currentTrip.getTrip_id());
            p.put("transaction_id", "00000" + this.currentTrip.getTrip_id());
            p.put("pay_promo_code", this.currentTrip.getTrip_promo_code());
            p.put("pay_promo_amt", "");
            if (currentTrip.getTrip_promo_code().equals("FIRST50")) {
                p.put("pay_promo_amt", "50");
                payAmount = payAmount - 50;
                p.put("pay_amount", payAmount);
            } else if (currentTrip.getTrip_promo_code().equals("FLOTER05")) {
                p.put("pay_promo_amt", (int) (payAmount * 0.05) + "");
                payAmount = payAmount - (int) (payAmount * 0.05);
                p.put("pay_amount", payAmount);
            }
            postCall(getContext(), AppConstants.BASE_PAYMENT + "save?", p, "Creating invoice...", 7);
        } else if (o.optString("status").equals("OK") && callNumber == 5) {
            t = null;
            try {
                t = new Gson().fromJson(o.getJSONObject("response").toString(), Trip.class);
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            if (t == null) {
                MyApp.popMessage("Error!", "Trip not found...", getContext());
            } else if (t.getTrip_status().equals(TripStatus.Cancelled.name())) {
                MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
                startActivity(new Intent(getContext(), MainActivity.class));
                MyApp.setSharedPrefString("button", "");
                finishAffinity();
            } else if (t.getTrip_status().equals(TripStatus.Driver_Cancel.name())) {
                MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
                startActivity(new Intent(getContext(), MainActivity.class));
                MyApp.setSharedPrefString("button", "");
                finishAffinity();
            } else {
                this.currentTrip = t;
                MyApp.getApplication().writeTrip(t);
                if (this.currentTrip.getTrip_status().equals(TripStatus.OnGoing.name())) {
                    this.btn_start.setVisibility(View.GONE);
                    MyApp.setSharedPrefString("button", "btn_start");
                    this.btn_reached.setVisibility(View.VISIBLE);
                } else if (this.currentTrip.getTrip_status().equals(TripStatus.Loading.name())) {
                    this.btn_start.setVisibility(View.VISIBLE);
                    this.btn_loading_done.setVisibility(View.GONE);
                    MyApp.setSharedPrefString("button", "btn_loading_done");
                } else if (this.currentTrip.getTrip_status().equals(TripStatus.Unloading.name())) {
                    this.btn_unloading_done.setVisibility(View.GONE);
                    MyApp.setSharedPrefString("button", "btn_unloading_done");
                    this.btn_stop.setVisibility(View.VISIBLE);
                } else if (this.currentTrip.getTrip_status().equals(TripStatus.Reached.name())) {
                    this.btn_unloading_done.setVisibility(View.VISIBLE);
                    this.btn_start.setVisibility(View.GONE);
                    MyApp.setSharedPrefString("button", "btn_start");
                }
                if (this.currentTrip.getTrip_status().equals(TripStatus.Finished.name())) {
                    calculateFinalFareStatusAndShowInvoice();
                }
            }
        } else if (o.optString("status").equals("OK") && callNumber == 7) {
//            MyApp.setSharedPrefString("button", "invoice");
            try {
                this.payment = new Gson().fromJson(o.getJSONObject("response").toString(), Payment.class);
                if (this.payment != null) {
                    this.btn_stop.setVisibility(View.GONE);
                    this.btn_arrived.setVisibility(View.GONE);
                    this.btn_start.setVisibility(View.GONE);
//                    p = new RequestParams();
//                    p.put("trip_id", this.currentTrip.getTrip_id());
//                    p.put("trip_fare", tripFare);
//                    p.put("trip_pay_amount", this.payment.getPay_amount());
//                    p.put("trip_promo_amt", payment.getPay_promo_amt());
//                    postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "", 26);
                    openPaymentWithFeedback();
                    isInvoiceGenerated = true;
                    return;
                }
                MyApp.showMassage(getContext(), "Parsing error, please try again.");
            } catch (JSONException e22) {
                e22.printStackTrace();
            }
        } else if (o.optString("status").equals("OK") && callNumber == 11) {
            changeTripStatus(TripStatus.Finished.name());
        } else if (callNumber == 22) {
            MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
            startActivity(new Intent(getContext(), MainActivity.class));
            MyApp.setSharedPrefString("button", "");
            finishAffinity();
        } else if(callNumber == 25){
            double sourceLat = Double.parseDouble(this.currentTrip.getTrip_scheduled_pick_lat());
            double sourceLng = Double.parseDouble(this.currentTrip.getTrip_scheduled_pick_lng());
            if (this.sourceMarker != null) {
                this.sourceMarker.remove();
            }
            this.sourceMarker = this.mMap.addMarker(new MarkerOptions().position(new LatLng(sourceLat, sourceLng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)));
        }
    }

    private void openPaymentWithFeedback() {
        if (this.payment == null) {
            MyApp.showMassage(getContext(), "Trip needs to finish...");
            return;
        }
        this.txt_pay_mode.setText("Pay mode : " + this.currentTrip.getTrip_pay_mode());
        RequestParams p = new RequestParams();
        p.put("trip_id", this.currentTrip.getTrip_id());
        p.put("floter_id", this.payment.getPayment_id());
        p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
        postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "", 26);
        this.txt_trip_payment.setText("Rs. " + this.payment.getPay_amount());
        this.slideUp.show();
        RequestParams pp = new RequestParams();
        pp.put(MyApp.EXTRA_MESSAGE, "Your trip fare was\nRs. " + this.payment.getPay_amount());
        pp.put("trip_id", this.currentTrip.getTrip_id());
        pp.put("trip_status", "Payment");
        pp.put("android", this.currentTrip.getUser().getU_device_token());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MyApp.spinnerStop();
            }
        }, 30000);
        client.post("http://floter.in/floterapi/push/RiderPushNotification.php?", pp, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, String response) {
                Log.d("Response:", response.toString());
                MyApp.spinnerStop();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                MyApp.spinnerStop();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                MyApp.spinnerStop();
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MyApp.spinnerStop();
            }
        });
    }

    private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double deltaLat = Math.abs(sw.latitude - ne.latitude);
        double deltaLon = Math.abs(sw.longitude - ne.longitude);
        LatLng sw2;
        LatLng ne2;
        if (deltaLat < 0.005d) {
            sw2 = new LatLng(sw.latitude - (0.005d - (deltaLat / 2.0d)), sw.longitude);
            ne2 = new LatLng(ne.latitude + (0.005d - (deltaLat / 2.0d)), ne.longitude);
            ne = ne2;
            sw = sw2;
            return new LatLngBounds(sw2, ne2);
        } else if (deltaLon >= 0.005d) {
            return bounds;
        } else {
            sw2 = new LatLng(sw.latitude, sw.longitude - (0.005d - (deltaLon / 2.0d)));
            ne2 = new LatLng(ne.latitude, ne.longitude + (0.005d - (deltaLon / 2.0d)));
            ne = ne2;
            sw = sw2;
            return new LatLngBounds(sw2, ne2);
        }
    }

    public void onJsonArrayResponseReceived(JSONArray a, int callNumber) {
    }

    public void onErrorReceived(String error) {
//        MyApp.showMassage(getContext(), error);
    }

    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        return "https://maps.googleapis.com/maps/api/directions/" + "json" + "?" + (str_origin + "&" + ("destination=" + dest.latitude + "," + dest.longitude) + "&" + "sensor=false");
    }

    public void animateMarker(Marker marker, LatLng toPosition, boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = this.mMap.getProjection();
        final LatLng startLatLng = proj.fromScreenLocation(proj.toScreenLocation(marker.getPosition()));
        final Interpolator interpolator = new LinearInterpolator();
        final LatLng latLng = toPosition;
        final Marker marker2 = marker;
        final boolean z = hideMarker;
        handler.post(new Runnable() {
            public void run() {
                float t = interpolator.getInterpolation(((float) (SystemClock.uptimeMillis() - start)) / 500.0f);
                double lat = (((double) t) * latLng.latitude) + (((double) (1.0f - t)) * startLatLng.latitude);
                marker2.setPosition(new LatLng(lat, (((double) t) * latLng.longitude) + (((double) (1.0f - t)) * startLatLng.longitude)));
                if (((double) t) < 1.0d) {
                    handler.postDelayed(this, 16);
                } else if (z) {
                    marker2.setVisible(false);
                } else {
                    marker2.setVisible(true);
                }
            }
        });
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    LocationManager mLocationManager;
    private Runnable locationCallback = new Runnable() {
        @Override
        public void run() {


            Location myLocation = getLastKnownLocation();

//            Location location = locationManager.getLastKnownLocation(locationManager
//                    .getBestProvider(criteria, false));
            try {
                if (myLocation != null) {
                    currentLocation = myLocation;
                    if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                        currentLocationMarker = null;
                    }
                    currentLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_driver)));
                    changeMap(myLocation);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(sourceMarker.getPosition());
                    builder.include(destMarker.getPosition());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds
                            (adjustBoundsForMaxZoomLevel(builder.build()), 100));
                    RequestParams pp = new RequestParams();
                    pp.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
                    pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
                    pp.put("d_lat", currentLocation.getLatitude() + "");
                    pp.put("d_is_available", 0);
                    pp.put("d_lng", currentLocation.getLongitude() + "");
                    pp.put("d_degree", Float.valueOf(myLocation.getBearing()));
                    postCall(getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 10);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            locationHandler.postDelayed(locationCallback, 60000);

        }
    };

    public void rotateMarker(Marker marker, float toRotation, float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;
        final Interpolator interpolator = new LinearInterpolator();
        final float f = toRotation;
        final Marker marker2 = marker;
        handler.post(new Runnable() {
            public void run() {
                float t = interpolator.getInterpolation(((float) (SystemClock.uptimeMillis() - start)) / 1555.0f);
                float rot = (f * t) + ((1.0f - t) * startRotation);
                Marker marker = marker2;
                if ((-rot) > BitmapDescriptorFactory.HUE_CYAN) {
                    rot /= 2.0f;
                }
                marker.setRotation(rot);
                if (((double) t) < 1.0d) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void getTotalTimeOfTrip() {
        String dateStart = this.currentTrip.getTrip_created();
        String dateStop = this.currentTrip.getTrip_modified();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = d2.getTime() - d1.getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
    }
}
