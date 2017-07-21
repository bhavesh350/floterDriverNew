package cargo.floter.driver.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cargo.floter.driver.HistoryDetailsActivity;
import cargo.floter.driver.R;
import cargo.floter.driver.application.SingleInstance;
import cargo.floter.driver.model.NearbyUser;
import cargo.floter.driver.model.Trip;
import cargo.floter.driver.utils.MyAnimationUtils;

import java.util.List;

/**
 * Created by DJ-PC on 4/29/2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {

    private List<Trip> list;
    private Context mContext;
    int previousPosition = 0;


    public HistoryAdapter(Context context, List<Trip> list) {
        this.list = list;
        mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new Holder(itemView);

    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Trip t = list.get(position);
        holder.BookingStatus.setText(t.getTrip_status());
        if (TextUtils.isEmpty(holder.BookingStatus.getText().toString())) {
            holder.BookingStatus.setText("Unknown");
        }
        holder.Date.setText(t.getTrip_modified().split(" ")[0]);
        holder.Source.setText(t.getTrip_from_loc());
        holder.Destination.setText(t.getTrip_to_loc());
        holder.BookId.setText("FDA-" + t.getTrip_id());
        if (position > previousPosition) {
            new MyAnimationUtils().animate(holder, true);
        } else {
            new MyAnimationUtils().animate(holder, false);
        }
        previousPosition = position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView BookingStatus, Date, Source, Destination, BookId, Details;
        ImageView UserImg;

        public Holder(View itemView) {
            super(itemView);
            BookingStatus = (TextView) itemView.findViewById(R.id.book_status);
            Date = (TextView) itemView.findViewById(R.id.book_date);
            Source = (TextView) itemView.findViewById(R.id.address_src);
            Destination = (TextView) itemView.findViewById(R.id.address_dest);
            BookId = (TextView) itemView.findViewById(R.id.book_id);
            Details = (TextView) itemView.findViewById(R.id.details);
//            UserImg=(ImageView)itemView.findViewById(R.id.img_user);
            Details.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.details:
                    Intent intent = new Intent(mContext, HistoryDetailsActivity.class);
                    SingleInstance.getInstance().setHistoryTrip(list.get(getLayoutPosition()));
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}
