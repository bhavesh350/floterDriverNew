package cargo.floter.driver.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



import java.util.Collections;
import java.util.List;

import cargo.floter.driver.R;


public class NavigationDrawerAdapter extends
		RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {
	List<String> data = Collections.emptyList();
	private LayoutInflater inflater;
	private Context context;

	public NavigationDrawerAdapter(Context context, List<String> data) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.data = data;
	}

	public void delete(int position) {
		data.remove(position);
		notifyItemRemoved(position);
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
		MyViewHolder holder = new MyViewHolder(view);
		return holder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		String current = data.get(position);
		holder.title.setText(current);
		TypedArray imgs = context.getResources().obtainTypedArray(R.array.minusIcons);
		holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, imgs.getResourceId(position, -1), 0);
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	class MyViewHolder extends RecyclerView.ViewHolder {
		TextView title;

		public MyViewHolder(View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.title);
		}
	}
}