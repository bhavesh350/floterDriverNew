package cargo.floter.driver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.model.Driver;
import cargo.floter.driver.utils.AppConstants;
import cz.msebera.android.httpclient.Header;

/**
 * Created by SONI on 12/17/2017.
 */

public class LocationService extends Service {
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return START_STICKY;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1, listener);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 1, listener);
        } catch (Exception e) {
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 500;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    private Handler updateHandler = new Handler();
    private boolean isCallable = false;

    private Runnable updateTimerCallback = new Runnable() {
        @Override
        public void run() {
            isCallable = true;
            updateHandler.postDelayed(updateTimerCallback, 60000);
        }
    };

    private boolean isOnceInvoked = false;

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {

            if (!isOnceInvoked) {
                isOnceInvoked = true;
                updateHandler.postDelayed(updateTimerCallback, 60000);
            }


            Log.i("------>>>>", "Location changed");
            if (isBetterLocation(loc, previousBestLocation) && isCallable) {
                isCallable = false;
                loc.getLatitude();
                loc.getLongitude();
                intent.putExtra("Latitude", loc.getLatitude());
                intent.putExtra("Longitude", loc.getLongitude());
                intent.putExtra("Provider", loc.getProvider());
                RequestParams pp = new RequestParams();
                Driver u = MyApp.getApplication().readDriver();
                pp.put("driver_id", u.getDriver_id());
                pp.put("d_lat", loc.getLatitude() + "");
                pp.put("d_lng", loc.getLongitude() + "");
                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(10000);
                client.post(AppConstants.BASE_URL + "updatedriverprofile?", pp, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                        Log.d("Response:", response.toString());
                    }
                });
            }
        }

        public void onProviderDisabled(String provider) {
//            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
//            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }
}