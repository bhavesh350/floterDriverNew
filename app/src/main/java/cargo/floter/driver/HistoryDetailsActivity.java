package cargo.floter.driver;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.application.SingleInstance;
import cargo.floter.driver.model.Trip;

public class HistoryDetailsActivity extends CustomActivity {

    private Toolbar toolbar;
    private TextView txt_date_time, txt_cost, txt_truck_type, payment_mode, address_src, address_dest, user_name,
            trip_fare, discount, subtotal, total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail__history);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        setupUiElements();
    }

    private void setupUiElements() {
        txt_date_time = (TextView) findViewById(R.id.txt_date_time);
        txt_cost = (TextView) findViewById(R.id.txt_cost);
        txt_truck_type = (TextView) findViewById(R.id.txt_truck_type);
        payment_mode = (TextView) findViewById(R.id.payment_mode);
        address_src = (TextView) findViewById(R.id.address_src);
        address_dest = (TextView) findViewById(R.id.address_dest);
        user_name = (TextView) findViewById(R.id.user_name);
        trip_fare = (TextView) findViewById(R.id.trip_fare);
        discount = (TextView) findViewById(R.id.discount);
        subtotal = (TextView) findViewById(R.id.subtotal);
        total = (TextView) findViewById(R.id.total);

        Trip t = SingleInstance.getInstance().getHistoryTrip();
        txt_date_time.setText(MyApp.convertTime(t.getTrip_modified()).replace(" ", " at "));
        txt_cost.setText("Rs. " + t.getTrip_pay_amount());
        txt_truck_type.setText(t.getDriver().getCar_name());
        payment_mode.setText(t.getTrip_pay_mode());
        address_src.setText(t.getTrip_from_loc());
        address_dest.setText(t.getTrip_to_loc());
        user_name.setText(t.getUser().getU_fname() + " " + t.getUser().getU_lname());
        trip_fare.setText(t.getTrip_pay_amount());
        subtotal.setText(t.getTrip_pay_amount());
        total.setText(t.getTrip_pay_amount());
        discount.setText(t.getTrip_promo_amt());

    }
}
