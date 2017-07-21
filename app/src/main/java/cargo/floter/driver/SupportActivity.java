package cargo.floter.driver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import de.cketti.mailto.EmailIntentBuilder;

/**
 * Created by Aquad on 12-07-2017.
 */

public class SupportActivity extends CustomActivity {
    private Toolbar toolbar;
    private TextView txt_call;
    private TextView txt_mail_us;
    private TextView txt_terms;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(this.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        setupUiElements();
    }

    private void setupUiElements() {
        this.txt_mail_us = (TextView) findViewById(R.id.txt_mail_us);
        this.txt_call = (TextView) findViewById(R.id.txt_call);
        this.txt_terms = (TextView) findViewById(R.id.txt_terms);
        setTouchNClick(R.id.txt_mail_us);
        setTouchNClick(R.id.txt_call);
        setTouchNClick(R.id.txt_terms);
    }

    public void onClick(View v) {
        super.onClick(v);
        if (v == this.txt_mail_us) {
            EmailIntentBuilder.from(this).to("info@floter.net").subject("Support mail").body("").start();
        } else if (v == this.txt_call) {
            Intent intent = new Intent("android.intent.action.DIAL");
            intent.setData(Uri.parse("tel:+918895188575"));
            startActivity(intent);
        } else if (v == this.txt_terms) {
            startActivity(new Intent(this, TermsConditionsActivity.class));
        }
    }
}
