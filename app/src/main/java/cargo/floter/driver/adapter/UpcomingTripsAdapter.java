package cargo.floter.driver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import cargo.floter.driver.R;
import cargo.floter.driver.UpcomingTrips;
import cargo.floter.driver.model.Trip;
import cargo.floter.driver.utils.MyAnimationUtils;

import java.util.List;

public class UpcomingTripsAdapter extends Adapter<UpcomingTripsAdapter.Holder> {
    private List<Trip> list;
    private Context mContext;
    int previousPosition = 0;

    public class Holder extends ViewHolder implements OnClickListener {
        TextView BookId;
        TextView Destination;
        TextView Source;
        TextView book_date;
        TextView txt_book_cancel;
        TextView txt_book_start;

        public Holder(View itemView) {
            super(itemView);
            this.book_date = (TextView) itemView.findViewById(R.id.book_date);
            this.Source = (TextView) itemView.findViewById(R.id.address_src);
            this.Destination = (TextView) itemView.findViewById(R.id.address_dest);
            this.BookId = (TextView) itemView.findViewById(R.id.book_id);
            this.txt_book_start = (TextView) itemView.findViewById(R.id.txt_book_start);
            this.txt_book_cancel = (TextView) itemView.findViewById(R.id.txt_book_cancel);
            this.txt_book_cancel.setOnClickListener(this);
            this.txt_book_start.setOnClickListener(this);
        }

        public void onClick(View v) {
            if (v == this.txt_book_start) {
                ((UpcomingTrips) UpcomingTripsAdapter.this.mContext).startBooking((Trip) UpcomingTripsAdapter.this.list.get(getLayoutPosition()));
            } else if (v == this.txt_book_cancel) {
                ((UpcomingTrips) UpcomingTripsAdapter.this.mContext).cancelBooking((Trip) UpcomingTripsAdapter.this.list.get(getLayoutPosition()));
            }
        }
    }

    public UpcomingTripsAdapter(Context context, List<Trip> list) {
        this.list = list;
        this.mContext = context;
    }

    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_item, parent, false));
    }

    public void onBindViewHolder(Holder holder, int position) {
        Trip t = this.list.get(position);
        String s = t.getLater_booking_time();
        holder.book_date.setText(s.substring(0, s.length() - 3));
        holder.Source.setText(t.getTrip_from_loc());
        holder.Destination.setText(t.getTrip_to_loc());
        holder.BookId.setText("FDA-" + t.getTrip_id());
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
