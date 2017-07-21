package cargo.floter.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.model.Driver;
import cargo.floter.driver.utils.AppConstants;


public class ProfileActivity extends CustomActivity {
    private Toolbar toolbar;
    private TextView info_mobile, info_name, info_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        Driver d = MyApp.getApplication().readDriver();
        info_mobile = (TextView) findViewById(R.id.info_mobile);
        info_name = (TextView) findViewById(R.id.info_name);
        info_email=(TextView)findViewById(R.id.info_email);
        info_mobile.setText(d.getD_phone());
        info_email.setText(d.getD_email());
        info_name.setText(d.getD_fname() + " " + d.getD_lname());

        setTouchNClick(R.id.logout);

    }



    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() != R.id.logout) {
            return;
        }
        if (MyApp.getStatus(AppConstants.IS_ON_TRIP)) {
            MyApp.popMessage("Alert!", "You are associated with a trip, please complete first", this);
            return;
        }
        MyApp.setStatus(AppConstants.IS_LOGIN, false);
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }
}