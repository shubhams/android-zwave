package com.azwave.androidzwave.zwave;

//-----------------------------------------------------------------------------
//Copyright (c) 2010 Mal Lansell <openzwave@lansell.org>
//
//SOFTWARE NOTICE AND LICENSE
//
//This file is part of OpenZWave.
//
//OpenZWave is free software: you can redistribute it and/or modify
//it under the terms of the GNU Lesser General Public License as published
//by the Free Software Foundation, either version 3 of the License,
//or (at your option) any later version.
//
//OpenZWave is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public License
//along with OpenZWave.  If not, see <http://www.gnu.org/licenses/>.
//
//-----------------------------------------------------------------------------
//
//Ported to Java by: Peradnya Dinata <peradnya@gmail.com>
//
//-----------------------------------------------------------------------------

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;

import com.azwave.androidzwave.R;
import com.azwave.androidzwave.zwave.items.ControllerActionListener;
import com.azwave.androidzwave.zwave.items.QueueManager;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.NodeListener;
import com.azwave.androidzwave.zwave.nodes.NodeManager;
import com.azwave.androidzwave.zwave.utils.Log;
import com.azwave.androidzwave.zwave.utils.XMLManager;
import com.azwave.androidzwave.zwave.utils.XMLManagerAndroid;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

public class Manager {

	private Driver zwaveDriver;
	private Log zwaveLog;
	private XMLManager zwaveXMLManager;
	private NodeManager zwaveNodeManager;
	private QueueManager zwavequeueManager;

	private Context context;
	private UsbSerialDriver serialDriver;

	public Manager(Context context, UsbSerialDriver serialDriver) {
		this.context = context;
		this.serialDriver = serialDriver;

		zwaveLog = new Log(context, R.layout.activity_main_listitem_log);
		zwaveXMLManager = new XMLManagerAndroid(context, zwaveLog);
		zwavequeueManager = new QueueManager(zwaveLog);
		zwaveNodeManager = new NodeManager(zwavequeueManager, zwaveXMLManager,
				zwaveLog);
		zwaveDriver = new Driver(zwavequeueManager, zwaveNodeManager,
				zwaveXMLManager, serialDriver, zwaveLog);
	}

	public void setNodeListener(NodeListener listener) {
		zwaveNodeManager.setListener(listener);
	}

	public void open() throws IOException {
		zwaveLog.add("Start Android Z-Wave");
		zwaveLog.add("Initializing & Reading Z-Wave XML Data");
		zwaveXMLManager.readDeviceClasses();
		zwaveXMLManager.readManufacturerSpecific();

		zwaveDriver.start();
	}

	public synchronized void setControllerActionListener(
			ControllerActionListener listener) {
		zwavequeueManager.setControllerActionListener(listener);
	}

	public boolean isAllNodesQueried() {
		return zwaveNodeManager.isAllNodesQueried();
	}

	public ArrayList<Node> getAllNodesAlive() {
		return zwaveNodeManager.toArrayListNodeAlive();
	}

	public ArrayList<Node> getAllNodes() {
		return zwaveNodeManager.toArrayList();
	}

	public int nodesAliveCount() {
		return zwaveNodeManager.nodesAliveCount();
	}

	public int nodesCount() {
		return zwaveNodeManager.nodesCount();
	}

	public void close() {
		zwaveDriver.close();
	}

	public Log getLog() {
		return zwaveLog;
	}

}
