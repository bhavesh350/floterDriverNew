package cargo.floter.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import cargo.floter.driver.R;

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