package com.azwave.androidzwave.module;

import com.azwave.androidzwave.R;
import com.azwave.androidzwave.zwave.commandclass.Basic;
import com.azwave.androidzwave.zwave.commandclass.SwitchBinary;
import com.azwave.androidzwave.zwave.commandclass.SwitchMultiLevel;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerCommand;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueBool;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueChangedListener;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NodeListAdapter extends ArrayAdapter<Node> implements
		ValueChangedListener {

	private int listItemResourceId;

	public NodeListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.listItemResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(listItemResourceId, parent, false);

		final Node node = getItem(position);
		final TextView nodeName = (TextView) rowView
				.findViewById(R.id.node_name);
		nodeName.setText(String.format("Node %d -- %s",
				SafeCast.toInt(node.getNodeId()), node.getProductName()));

		final ToggleButton nodeButton = (ToggleButton) rowView
				.findViewById(R.id.node_toggle);
		final SeekBar nodeSeekbar = (SeekBar) rowView
				.findViewById(R.id.node_seek);
		final LinearLayout nodeController = (LinearLayout) rowView
				.findViewById(R.id.node_controller);
		final Button nodeAdd = (Button) rowView
				.findViewById(R.id.node_add_controller);
		final Button nodeRemove = (Button) rowView
				.findViewById(R.id.node_remove_controller);

		if (node.isController()) {
			nodeButton.setVisibility(View.GONE);
			nodeSeekbar.setVisibility(View.GONE);
			nodeAdd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					node.getQueueManager().sendControllerCommand(
							ControllerCommand.AddDevice, false,
							node.getNodeId());
				}
			});

			nodeRemove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					node.getQueueManager().sendControllerCommand(
							ControllerCommand.RemoveDevice, false,
							node.getNodeId());
				}
			});

		} else if (node.getCommandClassManager().getCommandClass(
				SwitchBinary.COMMAND_CLASS_ID) != null
				&& node.getCommandClassManager().getCommandClass(
						Basic.COMMAND_CLASS_ID) != null) {
			nodeSeekbar.setVisibility(View.GONE);
			nodeController.setVisibility(View.GONE);
			final ValueByte value = (ValueByte) node.getValueManager()
					.getValue(Basic.COMMAND_CLASS_ID, (byte) 1, (byte) 0);
			value.setValueChangedListener(this);

			nodeButton.setChecked(value.getValue() == 0 ? false : true);
			nodeButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								node.setOn();
							} else {
								node.setOff();
							}
						}
					});
		} else if (node.getCommandClassManager().getCommandClass(
				SwitchMultiLevel.COMMAND_CLASS_ID) != null
				&& node.getCommandClassManager().getCommandClass(
						Basic.COMMAND_CLASS_ID) != null) {
			nodeButton.setVisibility(View.GONE);
			nodeController.setVisibility(View.GONE);
			final ValueByte value = (ValueByte) node.getValueManager()
					.getValue(Basic.COMMAND_CLASS_ID, (byte) 1, (byte) 0);
			value.setValueChangedListener(this);

			int progress = 0;
			final int values = SafeCast.toInt(value.getValue());
			if (values == 255 || values == 99) {
				progress = 5;
			} else if (values >= 0 && values < 99) {
				progress = values / 20;
			} else {
				progress = 5;
			}

			nodeSeekbar.setProgress(progress);
			nodeSeekbar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						private byte level = 0;

						@Override
						public void onProgressChanged(SeekBar arg0, int arg1,
								boolean arg2) {
							// TODO Auto-generated method stub
							level = (byte) ((arg1 * 20) >= 100 ? 99
									: (arg1 * 20));
						}

						@Override
						public void onStartTrackingTouch(SeekBar arg0) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onStopTrackingTouch(SeekBar arg0) {
							// TODO Auto-generated method stub
							node.setLevel(level);
						}

					});
		}

		return rowView;
	}

	@Override
	public void onValueChanged(Value value) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Activity y = (Activity) getContext();
				y.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}).start();
	}

}
