package cargo.floter.driver;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import cargo.floter.driver.CustomActivity.ResponseCallback;
import cargo.floter.driver.adapter.PaymentsAdapter;
import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.model.Payment;
import cargo.floter.driver.utils.AppConstants;

public class PaymentsActivity extends CustomActivity implements ResponseCallback {
    private PaymentsAdapter adapter;
    private boolean isToday = false;
    private RecyclerView rv_history;
    private Toolbar toolbar;
    private TextView toolbar_title;
    private TextView txt_total;

    class C05361 extends TypeToken<List<Payment>> {
        C05361() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResponseListener(this);
        setContentView(R.layout.activity_payments);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar_title = (TextView) this.toolbar.findViewById(R.id.toolbar_title);
        this.txt_total = (TextView) findViewById(R.id.txt_total);
        setSupportActionBar(this.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        this.rv_history = (RecyclerView) findViewById(R.id.rc_history);
        this.toolbar_title.setText("Today's Earnings");
        this.txt_total.setText("");
        this.isToday = getIntent().getBooleanExtra(AppConstants.EXTRA_1, false);
        loadHistory();
    }

    private void loadHistory() {
        RequestParams p = new RequestParams();
        if (this.isToday) {
            p.put("pay_date", MyApp.millsToDate2(System.currentTimeMillis()));
        } else {
            this.toolbar_title.setText("Payments");
        }
        p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
        postCall(getContext(), AppConstants.BASE_PAYMENT + "getpayments", p, "Please wait...", 1);
    }

    public void onJsonObjectResponseReceived(JSONObject o, int callNumber) {
        if (callNumber == 1 && o.optString("status").equals("OK")) {
            try {
                List<Payment> trips = new Gson().fromJson(o.getJSONArray("response").toString(), new C05361().getType());
                this.adapter = new PaymentsAdapter(this, trips, this.isToday);
                this.rv_history.setLayoutManager(new LinearLayoutManager(this, 1, false));
                this.rv_history.setAdapter(this.adapter);
                int total = 0;
                for (Payment p : trips) {
                    if (p.getPay_status().equals("PAID")) {
                        total += Integer.parseInt(p.getPay_amount());
                    }
                }
                this.txt_total.setText("Today's Total: Rs." + total);
            } catch (JSONException e) {
                MyApp.popMessageAndFinish("Message", "No trip booked yet.", this);
                e.printStackTrace();
            } catch (Exception e2) {
                MyApp.popMessageAndFinish("Message", "No trip booked yet.", this);
                e2.printStackTrace();
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

}
