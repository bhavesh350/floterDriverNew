package cargo.floter.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import cargo.floter.driver.R;
import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.utils.AppConstants;

/**
 * Created by SONI on 3/20/2017.
 */

public class SplashActivity extends Activity {
    private static final int SPLASH_DURATION_MS = 2000;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        mHandler.postDelayed(mEndSplash, SPLASH_DURATION_MS);

        if (!MyApp.getStatus("NewApp")) {
            MyApp.setStatus("NewApp", true);
            MyApp.setStatus(AppConstants.IS_LOGIN, false);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    String token = FirebaseInstanceId.getInstance().getToken();
                    MyApp.setStatus(AppConstants.IS_LOGIN, false);
                }
            }.execute();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mEndSplash.run();
        return super.onTouchEvent(event);
    }

    private Runnable mEndSplash = new Runnable() {
        public void run() {
            if (!isFinishing()) {
                mHandler.removeCallbacks(this);
                startActivity(new Intent(
                        SplashActivity.this, LoginActivity.class
                ));

                finish();
            }
        };
    };
}