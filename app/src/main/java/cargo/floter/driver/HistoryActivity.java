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

import java.util.ArrayList;
import java.util.List;

import cargo.floter.driver.adapter.HistoryAdapter;
import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.model.Trip;
import cargo.floter.driver.model.TripStatus;
import cargo.floter.driver.utils.AppConstants;

public class HistoryActivity extends CustomActivity implements CustomActivity.ResponseCallback {
    private RecyclerView rv_history;
    private Toolbar toolbar;
    private HistoryAdapter adapter;
    private TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResponseListener(this);
        setContentView(R.layout.activity_history);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) this.toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        rv_history = (RecyclerView) findViewById(R.id.rc_history);

        if (getIntent().getBooleanExtra(AppConstants.EXTRA_1, false)) {
            loadTodaysHistory();
            this.toolbar_title.setText("Today's Trips");
            return;
        }
        this.toolbar_title.setText("History");
        loadHistory();
    }

    private void loadHistory() {
        RequestParams p = new RequestParams();
        p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
        p.put("trip_status", TripStatus.Finished.name());
        postCall(getContext(), AppConstants.BASE_URL_TRIP + "gettrips", p, "Getting all trips...", 1);
    }
    private void loadTodaysHistory() {
        RequestParams p = new RequestParams();
        p.put("driver_id", MyApp.getApplication().readDriver().getDriver_id());
        p.put("trip_date", MyApp.millsToDate2(System.currentTimeMillis()));
        p.put("trip_status", TripStatus.Finished.name());
        postCall(getContext(), AppConstants.BASE_URL_TRIP + "gettrips", p, "Getting today's trips...", 1);
    }

    @Override
    public void onJsonObjectResponseReceived(JSONObject o, int callNumber) {
        if (callNumber == 1 && o.optString("status").equals("OK")) {
            try {
                List<Trip> trips = new Gson().fromJson(o.getJSONArray("response").toString(), new TypeToken<List<Trip>>() {
                }.getType());
//                if (getIntent().getBooleanExtra(AppConstants.EXTRA_1, false)) {
//                    List<Trip> tripsToday = new ArrayList<>();
//                    for (int i = 0; i <trips.size() ; i++) {
//                        if(trips.get(i).getTrip_status().equals(TripStatus.Finished.name())){
//                            tripsToday.add(trips.get(i));
//                        }
//                    }
//                    adapter = new HistoryAdapter(this, tripsToday);
//                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//                    rv_history.setLayoutManager(linearLayoutManager);
//                    rv_history.setAdapter(adapter);
//                }else{
                    adapter = new HistoryAdapter(this, trips);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                    rv_history.setLayoutManager(linearLayoutManager);
                    rv_history.setAdapter(adapter);
//                }


            } catch (JSONException e) {
                MyApp.popMessageAndFinish("Message!","No trip data available.",HistoryActivity.this);
                e.printStackTrace();
            } catch (Exception e){
                MyApp.popMessageAndFinish("Message!","No trip data available",HistoryActivity.this);
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onJsonArrayResponseReceived(JSONArray a, int callNumber) {

    }

    @Override
    public void onErrorReceived(String error) {
        MyApp.popMessage("Error!", error, getContext());
    }

    private Context getContext() {
        return HistoryActivity.this;
    }
}
