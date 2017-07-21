package cargo.floter.driver;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.application.SingleInstance;
import cargo.floter.driver.fragments.FragmentDrawer;
import cargo.floter.driver.model.Driver;
import cargo.floter.driver.model.NearbyUser;
import cargo.floter.driver.model.RateCard;
import cargo.floter.driver.model.Trip;
import cargo.floter.driver.model.TripStatus;
import cargo.floter.driver.utils.AppConstants;
import cargo.floter.driver.utils.CircleCountDownView;
import cargo.floter.driver.utils.LocationProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends CustomActivity implements CustomActivity.ResponseCallback,
        FragmentDrawer.FragmentDrawerListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, LocationProvider.LocationCallback, LocationProvider.PermissionCallback {

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private GoogleMap mMap;
    private ImageButton img_job_status;
    private LatLng mCenterLatLong;
    protected static final String TAG = "MainActivity";
    private Location currentLocation = null;
    private LocationProvider locationProvider;
    private GoogleApiClient googleApiClient;
    private List<Marker> markers = new ArrayList<>();
    private boolean isTimerDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyApp.getStatus(AppConstants.IS_ON_TRIP)) {
            if (TextUtils.isEmpty(MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID))) {
                MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            } else {
                startActivity(new Intent(getContext(), OnTripActivity.class));
                finish();
                return;
            }

        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        locationProvider = new LocationProvider(this, this, this);
//        mLocationMarkerText = (TextView) findViewById(R.id.locMarkertext);
        img_job_status = (ImageButton) findViewById(R.id.img_job_status);
        setupUiElements();
        if (MyApp.getStatus(AppConstants.ON_JOB)) {
            img_job_status.setImageResource(R.drawable.ic_onjob);
        } else {
            img_job_status.setImageResource(R.drawable.ic_offjob);
        }
        mapFragment.getMapAsync(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        /*mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("FLOTER");*/

        actionBar.setTitle("");
        setResponseListener(this);

        drawerFragment = (FragmentDrawer) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
        saveRateCard();
        onNewIntent(getIntent());
    }

    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey("TYPE") && !this.isTimerDialogShown) {
            if (intent.getStringExtra("TYPE").equals("NEW_TRIP")) {
                this.isTimerDialogShown = true;
                JSONObject o = SingleInstance.getInstance().getJsonTripPayload();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View mView = getLayoutInflater().inflate(R.layout.activit_dialog, null);
                Button decline = (Button) mView.findViewById(R.id.ride_decline);
                Button accept = (Button) mView.findViewById(R.id.ride_accept);
                TextView txt_user_name = (TextView) mView.findViewById(R.id.txt_user_name);
                TextView txt_source_address = (TextView) mView.findViewById(R.id.txt_source_address);
                TextView txt_request = (TextView) mView.findViewById(R.id.txt_request);
                TextView txt_dest_address = (TextView) mView.findViewById(R.id.txt_dest_address);
                TextView txt_est_time = (TextView) mView.findViewById(R.id.txt_est_time);
                TextView txt_est_distance = (TextView) mView.findViewById(R.id.txt_est_distance);
                TextView txt_est_price = (TextView) mView.findViewById(R.id.txt_est_price);
                String estCost = o.optString("est_amount");
                txt_est_price.setText("Est. Cost\nRs. " + estCost);
                txt_user_name.setText(o.optString("u_name"));
                txt_source_address.setText(o.optString("source_address"));
                txt_dest_address.setText(o.optString("dest_address"));
                TextView textView = txt_est_time;
                textView.setText("Est. Time : " + o.optString("est_time"));
                textView = txt_est_distance;
                textView.setText("Distance : " + o.optString("est_dist") + " Km");
                textView = txt_request;
                textView.setText("Pickup Request for\n" + o.optString("goods_type"));
                final CircleCountDownView countDownView = (CircleCountDownView) mView.findViewById(R.id.countDownView);
                builder.setView(mView);
                final AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                this.progress = 1;
                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.new_booking);
                mp.start();
                mp.setLooping(true);
                final CountDownTimer countDownTimer = new CountDownTimer((long) 30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        countDownView.setProgress(MainActivity.this.progress, 30);
                        MainActivity.this.progress = MainActivity.this.progress + 1;
                    }

                    public void onFinish() {
                        countDownView.setProgress(MainActivity.this.progress, 30);
                        dialog.dismiss();
                        mp.stop();
                        dialog.dismiss();
                        MainActivity.this.isTimerDialogShown = false;
                    }
                };
                countDownTimer.start();
                decline.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MainActivity.this.isTimerDialogShown = false;
                        Toast.makeText(MainActivity.this, "Ride Declined", Toast.LENGTH_SHORT).show();
                        countDownTimer.cancel();
                        mp.stop();
                        mp.release();
                        dialog.dismiss();
                        MainActivity.this.callAcceptDeclineApi(false, SingleInstance.getInstance().getJsonTripPayload().toString());
                    }
                });
                accept.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "Ride Accepted", Toast.LENGTH_SHORT).show();
                        countDownTimer.cancel();
                        mp.stop();
                        mp.release();
                        dialog.dismiss();
                        MainActivity.this.callAcceptDeclineApi(true, SingleInstance.getInstance().getJsonTripPayload().toString());
                    }
                });
                dialog.show();
            }
        }
    }

    private void saveRateCard() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        client.post("http://floter.in/floterapi/index.php/carapi/getratecard", new RateCardResponseCallback());
    }

    class RateCardResponseCallback extends JsonHttpResponseHandler {
        RateCardResponseCallback() {
        }

        public void onSuccess(int statusCode, Header[] headers, JSONObject o) {
            if (o.optString("status").equals("OK")) {
                MyApp.getApplication().writeRateCard(new Gson().fromJson(o.toString(), RateCard.class));
            }
        }

        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (MyApp.getStatus("ALLOW_TRIP")) {
            MyApp.setStatus("ALLOW_TRIP", false);
            Intent i = new Intent();
            i.putExtra("TYPE", "NEW_TRIP");
            onNewIntent(i);
        }
    }

    private void setupUiElements() {
        setTouchNClick(R.id.today_earning);
        setTouchNClick(R.id.today_trip);
        setTouchNClick(R.id.img_job_status);
    }


    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, filter);
        locationProvider.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(this.receiver, this.filter);
        if (MyApp.getStatus(AppConstants.IS_ON_TRIP)) {
            if (TextUtils.isEmpty(MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID))) {
                MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
            } else {
                startActivity(new Intent(getContext(), OnTripActivity.class));
                finish();
                return;
            }
        }
        MyApp.setStatus(AppConstants.IS_OPEN, true);
        if (!MyApp.isLocationEnabled(getContext())) {
            enableGPS();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setStatus(AppConstants.IS_OPEN, false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MyApp.setStatus(AppConstants.IS_OPEN, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApp.setStatus(AppConstants.IS_OPEN, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.setStatus(AppConstants.IS_OPEN, false);
        locationProvider.disconnect();
        unregisterReceiver(receiver);
    }

    private int progress;
    IntentFilter filter = new IntentFilter("cargo.floter.driver.RIDE");
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String value = intent.getStringExtra("TYPE");
            if (value.equals("NEW_TRIP") && !isTimerDialogShown) {
                try {
                    MainActivity.this.isTimerDialogShown = true;
                    JSONObject o = new JSONObject(SingleInstance.getInstance().getJsonTripPayload().toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    View mView = MainActivity.this.getLayoutInflater().inflate(R.layout.activit_dialog, null);
                    Button decline = (Button) mView.findViewById(R.id.ride_decline);
                    Button accept = (Button) mView.findViewById(R.id.ride_accept);
                    TextView txt_user_name = (TextView) mView.findViewById(R.id.txt_user_name);
                    TextView txt_source_address = (TextView) mView.findViewById(R.id.txt_source_address);
                    TextView txt_dest_address = (TextView) mView.findViewById(R.id.txt_dest_address);
                    TextView txt_est_time = (TextView) mView.findViewById(R.id.txt_est_time);
                    TextView txt_est_distance = (TextView) mView.findViewById(R.id.txt_est_distance);
                    TextView txt_request = (TextView) mView.findViewById(R.id.txt_request);
                    TextView txt_est_price = (TextView) mView.findViewById(R.id.txt_est_price);
                    String estCost = o.optString("est_amount");
                    txt_est_price.setText("Est. Cost\nRs. " + estCost);
                    txt_user_name.setText(o.optString("u_name"));
                    txt_source_address.setText(o.optString("source_address"));
                    txt_dest_address.setText(o.optString("dest_address"));
                    TextView textView = txt_est_time;
                    textView.setText("Est. Time : " + o.optString("est_time"));
                    textView = txt_est_distance;
                    textView.setText("Distance : " + o.optString("est_dist") + " Km");
                    textView = txt_request;
                    textView.setText("Pickup Request for\n" + o.optString("goods_type"));
                    final CircleCountDownView countDownView = (CircleCountDownView) mView.findViewById(R.id.countDownView);
                    builder.setView(mView);
                    final AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    MainActivity.this.progress = 1;
                    final MediaPlayer mp = MediaPlayer.create(MainActivity.this.getContext(), R.raw.new_booking);
                    mp.start();
                    mp.setLooping(true);
                    final CountDownTimer countDownTimer = new CountDownTimer((long) 30000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            countDownView.setProgress(MainActivity.this.progress, 30);
                            MainActivity.this.progress = MainActivity.this.progress + 1;
                        }

                        public void onFinish() {
                            countDownView.setProgress(MainActivity.this.progress, 30);
                            dialog.dismiss();
                            mp.stop();
                            dialog.dismiss();
                            MainActivity.this.isTimerDialogShown = false;
                        }
                    };
                    countDownTimer.start();
                    decline.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            MainActivity.this.isTimerDialogShown = false;
                            Toast.makeText(MainActivity.this, "Ride Declined", Toast.LENGTH_SHORT).show();
                            countDownTimer.cancel();
                            mp.stop();
                            mp.release();
                            dialog.dismiss();
                            MainActivity.this.callAcceptDeclineApi(false, SingleInstance.getInstance().getJsonTripPayload().toString());
                        }
                    });
                    accept.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            Toast.makeText(MainActivity.this, "Ride Accepted", Toast.LENGTH_SHORT).show();
                            countDownTimer.cancel();
                            mp.stop();
                            mp.release();
                            dialog.dismiss();
                            MainActivity.this.callAcceptDeclineApi(true, SingleInstance.getInstance().getJsonTripPayload().toString());
                        }
                    });
                    dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.today_earning) {
            startActivity(new Intent(getContext(), PaymentsActivity.class).putExtra(AppConstants.EXTRA_1, true));
        } else if (v.getId() == R.id.today_trip) {
            startActivity(new Intent(getContext(), HistoryActivity.class).putExtra(AppConstants.EXTRA_1, true));
        } else if (v.getId() == R.id.img_job_status) {
            if (MyApp.getStatus(AppConstants.ON_JOB)) {
                MyApp.setStatus(AppConstants.ON_JOB, !MyApp.getStatus(AppConstants.ON_JOB));
                img_job_status.setImageResource(R.drawable.ic_offjob);
                updateDriverStatus(false);
            } else {
                MyApp.setStatus(AppConstants.ON_JOB, !MyApp.getStatus(AppConstants.ON_JOB));
                img_job_status.setImageResource(R.drawable.ic_onjob);
                updateDriverStatus(true);
            }
        }
    }


    private void callAcceptDeclineApi(boolean b, String payload) {
        if (TextUtils.isEmpty(AppConstants.CURRENT_TRIP_ID)) {
            MyApp.showMassage(getContext(), "Invalid trip");
            return;
        }

        try {
            MyApp.cancelNotification(getContext(), Integer.parseInt(MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID)));
        } catch (Exception e) {
            MyApp.cancelNotification(getContext(), 0);
        }

//         http://floter.in/floterapi/index.php/tripapi/updatetrip

        RequestParams p = new RequestParams();
        p.put("trip_id", MyApp.getSharedPrefString(AppConstants.CURRENT_TRIP_ID));
        if (b) {
            p.put("trip_status", TripStatus.Accepted.name());
            postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "Please wait...", 5);
        } else {
            p.put("trip_status", TripStatus.Declined.name());
            postCall(getContext(), AppConstants.BASE_URL_TRIP + "updatetrip", p, "Please wait...", 5);
            MyApp.setSharedPrefString(AppConstants.CURRENT_TRIP_ID, "");
        }

    }


    @Override
    public void onDrawerItemSelected(View view, int position) {
        if (position == 6) {
            MyApp.setStatus(AppConstants.IS_LOGIN, false);
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        } else if (position == 3) {
            startActivity(new Intent(getContext(), ProfileActivity.class));
        } else if (position == 1) {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        } else if (position == 4) {
            startActivity(new Intent(getContext(), PaymentsActivity.class).putExtra(AppConstants.EXTRA_1, false));
        } else if (position == 2) {
            startActivity(new Intent(getContext(), UpcomingTrips.class));
        } else if (position == 5) {
            startActivity(new Intent(getContext(), SupportActivity.class));
        }
    }

    public void enableGPS() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(
                            new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(
                                        ConnectionResult connectionResult) {

                                    MyApp.showMassage(
                                            getContext(),
                                            "Location error "
                                                    + connectionResult
                                                    .getErrorCode());
                                }
                            }).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(MainActivity.this,
                                        44);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            });
        } else {
            LocationRequest locationRequest = LocationRequest.create();
            // locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {

                                status.startResolutionForResult(MainActivity.this,
                                        44);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "OnMapReady");
        mMap = googleMap;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("Camera postion change" + "", cameraPosition + "");
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        }
    }


    @Override
    public void handleNewLocation(Location location) {
        currentLocation = location;
        try {
            if (location != null) {
                getNearbyUsers(location.getLatitude() + "", location.getLongitude() + "");
                changeMap(location);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNearbyUsers(String lat, String lng) {
        RequestParams p = new RequestParams();
//         http://floter.in/floterapi/index.php/userapi/getnearbyuserlists?lat=25&lng=75&api_key=2869e53d41c273e80bb1e6e55fcdba55&miles=50
        p.put("lat", lat);
        p.put("lng", lng);
        p.put("miles", 20);
        p.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
        postCall(getContext(), AppConstants.BASE_URL.replace("driverapi", "userapi") + "getnearbyuserlists?", p, "", 1);

//         http://floter.in/floterapi/index.php/driverapi/updatedriverprofile?api_key=ee059a1e2596c265fd61c44f1855875e&driver_id=93&d_address=delhi
        RequestParams pp = new RequestParams();
        Driver u = MyApp.getApplication().readDriver();
        pp.put("driver_id", u.getDriver_id());
        pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
        pp.put("d_lat", lat);
        pp.put("d_is_available", MyApp.getStatus(AppConstants.ON_JOB) ? 1 : 0);
        pp.put("d_lng", lng);
        pp.put("d_device_token", MyApp.getSharedPrefString(AppConstants.DEVICE_TOKEN));
        pp.put("d_device_type", "Android");

        postCall(getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 0);
    }

    private void updateDriverStatus(boolean status) {
        RequestParams pp = new RequestParams();
        Driver u = MyApp.getApplication().readDriver();
        pp.put("driver_id", u.getDriver_id());
        pp.put("d_is_available", status ? 1 : 0);
        pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
        pp.put("d_lat", currentLocation.getLatitude() + "");
        pp.put("d_lng", currentLocation.getLongitude() + "");

        postCall(getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "Updating status...", 2);
    }

    private Context getContext() {
        return MainActivity.this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1010) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_DENIED) {
                MyApp.showMassage(getContext(), "Location Denied, you cannot use the app");
            }
        }
    }

    private void changeMap(Location location) {
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            LatLng latLong;
            latLong = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(15.5f).tilt(0).build();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1010);
                // for ActivityCompat#requestPermissions for more details.
            } else {
                mMap.setMyLocationEnabled(true);
            }

            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
//            getCompleteAddressString(location.getLatitude(), location.getLongitude());

        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public void handleManualPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1010);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onJsonObjectResponseReceived(JSONObject o, int callNumber) {
        if (callNumber == 1) {
            if (o.optString("status").equals("OK")) {
                NearbyUser nd = new Gson().fromJson(o.toString(), NearbyUser.class);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                if (nd.getResponse().size() > 0) {
                    mMap.clear();
                    markers.clear();
                }

                for (int i = 0; i < nd.getResponse().size(); i++) {
                    LatLng ll = new LatLng(Double.parseDouble(nd.getResponse().get(i).getU_lat()),
                            Double.parseDouble(nd.getResponse().get(i).getU_lng()));
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_user)));
//                    rotateMarker(marker,0.8f,0.1f);
                    builder.include(marker.getPosition());
                    animateMarker(marker, ll, false);

                    markers.add(marker);
                    refreshMarkerAnimation();
                }
                CameraUpdateFactory.newLatLngBounds(adjustBoundsForMaxZoomLevel(builder.build()), 100);
            }
        } else if (o.optString("status").equals("OK") && callNumber == 2) {
            MyApp.showMassage(getContext(), "Status updated");
            Driver d = null;
            try {
                d = new Gson().fromJson(o.getJSONObject("response").toString(), Driver.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (d != null) {
                MyApp.getApplication().writeDriver(d);
                return;
            }
        } else if (o.optString("status").equals("OK") && callNumber == 0) {
            try {
                if (o.getJSONObject("response").optString("d_is_verified").equals("0")) {
                    RequestParams pp = new RequestParams();
                    Driver u = MyApp.getApplication().readDriver();
                    pp.put("driver_id", u.getDriver_id());
                    pp.put("d_is_available", "0");
                    pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");

                    postCall(getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 4);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (o.optString("status").equals("OK") && callNumber == 4) {
            try {
                if (o.getJSONObject("response").optString("d_is_verified").equals("0")) {
                    MyApp.setStatus(AppConstants.IS_LOGIN, false);
                    MyApp.showMassage(getContext(), "Verification required, please login again.");
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    finishAffinity();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (o.optString("status").equals("OK") && callNumber == 5) {
            Trip t = null;
            try {
                t = new Gson().fromJson(o.getJSONObject("response").toString(), Trip.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (t == null) {
                MyApp.popMessage("Error!", "Trip not found...", getContext());
                return;
            } else if (t.getTrip_status().equals(TripStatus.Cancelled.name()) || t.getTrip_status().equals(TripStatus.Declined.name())) {
                if(t.getTrip_status().equals(TripStatus.Declined.name())){
                    RequestParams pp = new RequestParams();
                    pp.put("message", "Trip Accepted");
                    pp.put("trip_id", t.getTrip_id());
                    pp.put("trip_status", TripStatus.Declined.name());
                    pp.put("android", t.getUser().getU_device_token());
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.setTimeout(30000);
//             http://floter.in/floterapi/push/RiderPushNotification?message={"json":"json"}&android=1hTgw2d_BrDwhYH_lN&trip_id=10&trip_status=accept&object={"json":"json"}
                    client.post("http://floter.in/floterapi/push/RiderPushNotification?",
                            pp, new JsonHttpResponseHandler() {

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, final String response) {
                                    Log.d("Response:", response.toString());
                                    MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
//                                    startActivity(new Intent(getContext(), OnTripActivity.class));
//                                    RequestParams pp = new RequestParams();
//                                    pp.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
//                                    pp.put("d_is_available", 1);
//                                    pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
//                                    MainActivity.this.postCall(MainActivity.this.getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 13);
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    if (statusCode == 0) {
                                        MyApp.popMessage("Error!", "Timeout error, wait for other ride.", getContext());
                                    } else {
                                        MyApp.setStatus(AppConstants.IS_ON_TRIP, false);
//                                        startActivity(new Intent(getContext(), OnTripActivity.class));
//                                        RequestParams pp = new RequestParams();
//                                        pp.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
//                                        pp.put("d_is_available", 0);
//                                        pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
//                                        MainActivity.this.postCall(MainActivity.this.getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 13);
                                    }
                                }
                            });
                }
                return;
            }

            MyApp.getApplication().writeTrip(t);

            MyApp.spinnerStart(getContext(), "Please wait...");

            RequestParams pp = new RequestParams();
            pp.put("message", "Trip Accepted");
            pp.put("trip_id", t.getTrip_id());
            pp.put("trip_status", TripStatus.Accepted.name());
            pp.put("android", t.getUser().getU_device_token());
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30000);
//             http://floter.in/floterapi/push/RiderPushNotification?message={"json":"json"}&android=1hTgw2d_BrDwhYH_lN&trip_id=10&trip_status=accept&object={"json":"json"}
            client.post("http://floter.in/floterapi/push/RiderPushNotification?",
                    pp, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, final String response) {
                            Log.d("Response:", response.toString());
                            MyApp.setStatus(AppConstants.IS_ON_TRIP, true);
                            startActivity(new Intent(getContext(), OnTripActivity.class));
                            RequestParams pp = new RequestParams();
                            pp.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
                            pp.put("d_is_available", 0);
                            pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
                            MainActivity.this.postCall(MainActivity.this.getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 13);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            if (statusCode == 0) {
                                MyApp.popMessage("Error!", "Timeout error, wait for other ride.", getContext());
                            } else {
                                MyApp.setStatus(AppConstants.IS_ON_TRIP, true);
                                startActivity(new Intent(getContext(), OnTripActivity.class));
                                RequestParams pp = new RequestParams();
                                pp.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
                                pp.put("d_is_available", 0);
                                pp.put("api_key", "ee059a1e2596c265fd61c44f1855875e");
                                MainActivity.this.postCall(MainActivity.this.getContext(), AppConstants.BASE_URL + "updatedriverprofile?", pp, "", 13);
                            }
                        }
                    });
        }
    }

    private void refreshMarkerAnimation() {

    }

    private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double deltaLat = Math.abs((sw.latitude - this.currentLocation.getLatitude()) - (ne.latitude - this.currentLocation.getLatitude()));
        double deltaLon = Math.abs((sw.longitude - this.currentLocation.getLongitude()) - (ne.longitude - this.currentLocation.getLongitude()));
        LatLng latLng;
        LatLng ne2;
        LatLngBounds latLngBounds;
        if (deltaLat < 0.005d) {
            latLng = new LatLng(sw.latitude - (0.005d - (deltaLat / 2.0d)), sw.longitude);
            ne2 = new LatLng(ne.latitude + (0.005d - (deltaLat / 2.0d)), ne.longitude);
            latLngBounds = new LatLngBounds(latLng, ne2);
            ne = ne2;
            sw = latLng;
        } else if (deltaLon < 0.005d) {
            latLng = new LatLng(sw.latitude, sw.longitude - (0.005d - (deltaLon / 2.0d)));
            ne2 = new LatLng(ne.latitude, ne.longitude + (0.005d - (deltaLon / 2.0d)));
            latLngBounds = new LatLngBounds(latLng, ne2);
            ne = ne2;
            sw = latLng;
        }
        LatLngBounds.Builder displayBuilder = new LatLngBounds.Builder();
        displayBuilder.include(new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude()));
        displayBuilder.include(new LatLng(this.currentLocation.getLatitude() + deltaLat, this.currentLocation.getLongitude() + deltaLon));
        displayBuilder.include(new LatLng(this.currentLocation.getLatitude() - deltaLat, this.currentLocation.getLongitude() - deltaLon));
        this.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(displayBuilder.build(), 100));
        this.mMap.setMaxZoomPreference(15.5f);
        return bounds;
    }
    @Override
    public void onJsonArrayResponseReceived(JSONArray a, int callNumber) {

    }

    @Override
    public void onErrorReceived(String error) {
        MyApp.showMassage(getContext(), error);
    }


    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;

    }

    private class ReadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);


            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

    }

    public class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
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

    public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            JSONObject jDistance = null;
            JSONObject jDuration = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                    for (int j = 0; j < jLegs.length(); j++) {

                        jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                        HashMap<String, String> hmDistance = new HashMap<String, String>();
                        hmDistance.put("distance", jDistance.getString("text"));

                        /** Getting duration from the json data */
                        jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                        HashMap<String, String> hmDuration = new HashMap<String, String>();
                        hmDuration.put("duration", jDuration.getString("text"));
                        /** Adding distance object to the path */
                        path.add(hmDistance);

                        /** Adding duration object to the path */
                        path.add(hmDuration);

                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
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
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            String distance = "";
            String duration = "";
            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(10);
                polyLineOptions.color(Color.parseColor("#156CB3"));
            }
//            MyApp.popMessage("distance", "Distance:" + distance + ", Duration:" + duration, getContext());

            mMap.addPolyline(polyLineOptions);

        }
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}
