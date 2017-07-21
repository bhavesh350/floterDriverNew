package cargo.floter.driver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cargo.floter.driver.R;
import cargo.floter.driver.application.MyApp;
import cargo.floter.driver.model.Payment;
import cargo.floter.driver.utils.MyAnimationUtils;

public class PaymentsAdapter extends Adapter<PaymentsAdapter.Holder> {
    private boolean isToday;
    private List<Payment> list;
    private Context mContext;
    int previousPosition = 0;

    public class Holder extends ViewHolder implements OnClickListener {
        TextView txt_pay_id;
        TextView txt_payment;
        TextView txt_status;
        TextView txt_time_stamp;

        public Holder(View itemView) {
            super(itemView);
            this.txt_pay_id = (TextView) itemView.findViewById(R.id.txt_pay_id);
            this.txt_status = (TextView) itemView.findViewById(R.id.txt_status);
            this.txt_payment = (TextView) itemView.findViewById(R.id.txt_payment);
            this.txt_time_stamp = (TextView) itemView.findViewById(R.id.txt_time_stamp);
        }

        public void onClick(View view) {
            view.getId();
        }
    }

    public PaymentsAdapter(Context context, List<Payment> list, boolean isToday) {
        this.list = list;
        this.isToday = isToday;
        this.mContext = context;
    }

    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payments, parent, false));
    }

    public void onBindViewHolder(Holder holder, int position) {
        Payment t = this.list.get(position);
        holder.txt_pay_id.setText("ID: " + t.getTrip_id() + t.getPay_trans_id().substring(0, 2) + t.getPayment_id());
        holder.txt_payment.setText("Rs. " + t.getPay_amount() + " (" + t.getPay_mode() + ")");
        holder.txt_status.setText(t.getPay_status());
        if (this.isToday) {
            holder.txt_time_stamp.setText(MyApp.convertTime(t.getPay_created()).split(" ")[1]);
        } else {
            holder.txt_time_stamp.setText(t.getPay_created());
        }
        if (position > this.previousPosition) {
            MyAnimationUtils.animate(holder, true);
        } else {
            MyAnimationUtils.animate(holder, false);
        }
        this.previousPosition = position;
    }

    public int getItemCount() {
        return this.list.size();
    }
}
