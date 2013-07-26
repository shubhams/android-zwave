package com.azwave.androidzwave.zwave.utils;

import com.azwave.androidzwave.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Log extends ArrayAdapter<String> {

	private Context context;
	private int listItemResourceId;
	private int nomerLog = 1;

	public Log(Context context, int listItemResourceId) {
		super(context, listItemResourceId);
		this.context = context;
		this.listItemResourceId = listItemResourceId;
	}

	@Override
	public synchronized void add(final String object) {
		final Log x = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Activity y = (Activity) context;
				y.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						x.addSuper(object);
					}
				});
			}
		}).start();
	}

	private void addSuper(String object) {
		super.add(String.format("%05d| %s", nomerLog++, object));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(listItemResourceId, parent, false);

		TextView textView = (TextView) rowView.findViewById(R.id.log_text);
		textView.setText(this.getItem(position));

		return rowView;
	}

	private static final long serialVersionUID = 1137662789714599607L;

}
