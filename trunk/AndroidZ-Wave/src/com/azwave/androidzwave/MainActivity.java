package com.azwave.androidzwave;

import java.util.ArrayList;

import com.azwave.androidzwave.R;
import com.azwave.androidzwave.module.NodeListAdapter;
import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.Manager;
import com.azwave.androidzwave.zwave.items.ControllerActionListener;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerError;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.NodeListener;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements NodeListener,
		ControllerActionListener, OnClickListener {

	private ListView zwaveNodeList, zwaveLogList;
	private Button   clipboard;
	private UsbManager usbManager;
	private UsbSerialDriver serialDriver;
	private Manager zwaveManager;

	private NodeListAdapter nodelistAdapter;
	private ListViewUpdate listViewUpdate;

	private volatile long initStartTime = 0;
	private volatile long initEndTime = 0;
	
	private ClipboardManager clip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

		zwaveNodeList = (ListView) findViewById(R.id.list_node);
		zwaveLogList = (ListView) findViewById(R.id.list_log);
		clipboard = (Button) findViewById(R.id.clipboard);
		clipboard.setOnClickListener(this);

		initUsbDriver();
	}

	@Override
	public void finish() {
		if (serialDriver != null) {
			try {
				serialDriver.close();
				zwaveManager.close();
				listViewUpdate.close();
			} catch (Exception x) {
			}
		}
		super.finish();
	}

	private void initUsbDriver() {
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		serialDriver = UsbSerialProber.acquire(usbManager);
		try {
			zwaveManager = new Manager(this, serialDriver);
			nodelistAdapter = new NodeListAdapter(this,
					R.layout.activity_main_listitem_node);
			nodelistAdapter.setNotifyOnChange(false);

			zwaveLogList.setAdapter(zwaveManager.getLog());
			zwaveNodeList.setAdapter(nodelistAdapter);

			listViewUpdate = new ListViewUpdate();
			listViewUpdate.execute(zwaveManager);

			zwaveManager.setNodeListener(this);
			zwaveManager.setControllerActionListener(this);

			serialDriver.open();
			initStartTime = System.currentTimeMillis();
			zwaveManager.open();
		} catch (Exception x) {
			finish();
		}
	}

	private class ListViewUpdate extends
			AsyncTask<Manager, ArrayList<Node>, Void> {

		public volatile boolean lock = true;
		public volatile int size = 0;
		public volatile boolean foundupdate = false;

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Manager... arg0) {
			// while (!arg0[0].allNodesQueried()) {
			// }

			ArrayList<Node> nodes = null;
			while (lock) {
				if (foundupdate) {
					nodes = arg0[0].getAllNodesAlive();
					publishProgress(nodes);
					if (foundupdate) {
						foundupdate = false;
					}
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(ArrayList<Node>... progress) {
			if (progress[0] != null) {
				nodelistAdapter.clear();
				for (int i = 0; i < progress[0].size(); i++) {
					nodelistAdapter.add(progress[0].get(i));
				}
				nodelistAdapter.notifyDataSetChanged();
			}
		}

		public synchronized void close() {
			lock = false;
		}
	}

	@Override
	public void onNodeAliveListener(boolean alive) {
		// TODO Auto-generated method stub
		listViewUpdate.foundupdate = true;
	}

	@Override
	public void onNodeQueryStageCompleteListener() {
		// TODO Auto-generated method stub
		listViewUpdate.foundupdate = true;
		final Activity nowActivity = this;
		if (zwaveManager != null && zwaveManager.isAllNodesQueried()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					nowActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							initEndTime = System.currentTimeMillis();
							// TODO Auto-generated method stub
							Toast.makeText(
									nowActivity,
									"Z-Wave Initialization Complete -- times (ms) : "
											+ String.valueOf(initEndTime
													- initStartTime),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).run();
		}
	}

	@Override
	public void onNodeAddedToList() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNodeRemovedToList() {
		// TODO Auto-generated method stub
		listViewUpdate.foundupdate = true;
	}

	@Override
	public void onAction(final ControllerState state, ControllerError error,
			Object context) {
		final Activity nowActivity = this;
		new Thread(new Runnable() {
			public void run() {
				nowActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch (state) {
						case Normal:
							break;
						case Starting:
							break;
						case Cancel:
							break;
						case Error:
							break;
						case Waiting:
							Toast.makeText(nowActivity,
									"Waiting for node initiator ...",
									Toast.LENGTH_LONG).show();
							break;
						case Sleeping:
							break;
						case InProgress:
							Toast.makeText(nowActivity, "Plase wait ...",
									Toast.LENGTH_LONG).show();
							break;
						case Completed:
							Toast.makeText(nowActivity,
									"Controller command complete ...",
									Toast.LENGTH_LONG).show();
							break;
						case Failed:
							Toast.makeText(nowActivity,
									"Controller command failed ...",
									Toast.LENGTH_LONG).show();
							break;
						case NodeOK:
							break;
						case NodeFailed:
							break;
						}
					}
				});
			}
		}).run();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (arg0.getId() == R.id.clipboard) {
			//String datax = "";
			StringBuilder strbuild = new StringBuilder();
			
			synchronized(zwaveManager.getLog()) {
				for (int i = 0; i < zwaveManager.getLog().getCount(); i++) {
					strbuild.append(zwaveManager.getLog().getItem(i));
					strbuild.append('\n');
				}
			}
			
			ClipData clipdata = ClipData.newPlainText("label", strbuild.toString());
			clip.setPrimaryClip(clipdata);
		}
	}

}
