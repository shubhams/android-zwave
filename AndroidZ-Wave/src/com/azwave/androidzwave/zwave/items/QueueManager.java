package com.azwave.androidzwave.zwave.items;

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

import java.util.concurrent.PriorityBlockingQueue;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerCommand;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;
import com.azwave.androidzwave.zwave.items.QueueItem.QueueCommand;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;
import com.azwave.androidzwave.zwave.utils.Log;
import com.azwave.androidzwave.zwave.utils.SafeCast;

public class QueueManager {
	private PriorityBlockingQueue<QueueItem> queue;
	private Log logs;

	private boolean waitingForACK = false;
	private byte expectedCallbackId = 0;
	private byte expectedCommandClassId = 0;
	private byte expectedReply = 0;
	private byte expectedNodeId = 0;

	private byte transmitOptions = Defs.TRANSMIT_OPTION_ACK
			| Defs.TRANSMIT_OPTION_AUTO_ROUTE | Defs.TRANSMIT_OPTION_EXPLORE;

	private Msg currentMsg = null;
	private ControllerCmd currentControllerCmd = null;
	private ControllerActionListener controllerActionListener = null;

	private static long count = 0;

	public QueueManager(Log log) {
		queue = new PriorityBlockingQueue <QueueItem> (13,
				new QueueItemComparator());
		logs = log;
	}

	public void sendQueryStageComplete(byte nodeId, QueryStage query) {
		QueueItem item = new QueueItem();
		item.setCommand(QueueCommand.QueryStageComplete);
		item.setNodeId(nodeId);
		item.setQueryStage(query);
		item.setRetry(false);
		item.setPriority(QueuePriority.Query);
		item.setQueueCount(count++);
		if (count == Long.MAX_VALUE) {
			count = 0;
		}

		logs.add(String.format("Q: [Query] Node %d -- %s",
				SafeCast.toInt(nodeId), query.name()));
		queue.add(item);
	}

	public void retryQueryStageComplete(byte nodeId, QueryStage query) {
		QueueItem item = new QueueItem();
		item.setCommand(QueueCommand.QueryStageComplete);
		item.setNodeId(nodeId);
		item.setQueryStage(query);

		for (QueueItem entry : queue) {
			if (entry.getCommand() == item.getCommand()
					&& entry.getNodeId() == item.getNodeId()
					&& entry.getQueryStage() == item.getQueryStage()) {
				entry.setRetry(true);
				break;
			}
		}
	}

	public void sendMsg(Msg msg, QueuePriority priority) {
		QueueItem item = new QueueItem();
		item.setMsg(msg);
		item.setPriority(priority);
		item.setQueueCount(count++);
		if (count == Long.MAX_VALUE) {
			count = 0;
		}

		logs.add("Q: [Messg] " + msg.toString());
		queue.add(item);
	}

	public boolean sendControllerCommand(ControllerCommand command,
			boolean highPower, byte nodeId) {
		return sendControllerCommand(command, controllerActionListener, null,
				highPower, nodeId, (byte) 0);
	}

	public boolean sendControllerCommand(ControllerCommand command,
			ControllerActionListener callback, Object context,
			boolean highPower, byte nodeId, byte arg) {
		if (command == ControllerCommand.None) {
			return false;
		}
		ControllerCmd cci = new ControllerCmd();

		cci.setControllerCommand(command);
		cci.setControllerCallback(callback);
		cci.setControllerCallbackContext(context);
		cci.setHighPower(highPower);
		cci.setControllerCommandNode(nodeId);
		cci.setControllerCommandArg(arg);
		cci.setControllerState(ControllerState.Normal);
		cci.setControllerStateChanged(false);
		cci.setControllerCommandDone(false);

		return sendControllerCommand(cci);
	}

	public boolean sendControllerCommand(ControllerCmd cci) {
		QueueItem item = new QueueItem();

		item.setCci(cci);
		item.setCommand(QueueCommand.Controller);
		item.setPriority(QueuePriority.Controller);
		item.setQueueCount(count++);
		if (count == Long.MAX_VALUE) {
			count = 0;
		}

		queue.add(item);
		return true;
	}

	public void removeCurrentMsg() {
		removeExpectedAndACK();
		currentMsg = null;
	}

	public void removeExpectedAndACK() {
		setWaitingForACK(false);
		removeExpected();
	}

	public void removeExpected() {
		setExpectedCallbackId((byte) 0);
		setExpectedCommandClassId((byte) 0);
		setExpectedReply((byte) 0);
		setExpectedNodeId((byte) 0);
	}

	public void setCurrentMsg(Msg msg) {
		currentMsg = msg;
	}

	public Msg getCurrentMsg() {
		return currentMsg;
	}

	public int size() {
		return queue.size();
	}

	public QueueItem peek() {
		return queue.peek();
	}

	public QueueItem poll() {
		return queue.poll();
	}

	public PriorityBlockingQueue<QueueItem> getQueue() {
		return queue;
	}

	public boolean isWaitingForACK() {
		return waitingForACK;
	}

	public void setWaitingForACK(boolean waitingForACK) {
		this.waitingForACK = waitingForACK;
	}

	public byte getExpectedCallbackId() {
		return expectedCallbackId;
	}

	public void setExpectedCallbackId(byte expectedCallbackId) {
		this.expectedCallbackId = expectedCallbackId;
	}

	public byte getExpectedCommandClassId() {
		return expectedCommandClassId;
	}

	public void setExpectedCommandClassId(byte expectedCommandClassId) {
		this.expectedCommandClassId = expectedCommandClassId;
	}

	public byte getExpectedReply() {
		return expectedReply;
	}

	public void setExpectedReply(byte expectedReply) {
		this.expectedReply = expectedReply;
	}

	public byte getExpectedNodeId() {
		return expectedNodeId;
	}

	public void setExpectedNodeId(byte expectedNodeId) {
		this.expectedNodeId = expectedNodeId;
	}

	public byte getTransmitOptions() {
		return transmitOptions;
	}

	public void setTransmitOptions(byte transmitOption) {
		this.transmitOptions = transmitOption;
	}

	public boolean isExpectedReply(byte nodeId) {
		if (expectedNodeId == (byte) 255 || nodeId == (byte) 0) {
			return true;
		}
		if (expectedNodeId == Defs.FUNC_ID_ZW_GET_NODE_PROTOCOL_INFO
				|| expectedNodeId == Defs.FUNC_ID_ZW_REQUEST_NODE_INFO
				|| expectedNodeId == Defs.FUNC_ID_ZW_GET_ROUTING_INFO
				|| expectedNodeId == Defs.FUNC_ID_ZW_ASSIGN_RETURN_ROUTE
				|| expectedNodeId == Defs.FUNC_ID_ZW_DELETE_RETURN_ROUTE
				|| expectedNodeId == Defs.FUNC_ID_ZW_SEND_DATA
				|| expectedNodeId == Defs.FUNC_ID_ZW_SEND_NODE_INFORMATION
				|| expectedNodeId == Defs.FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE
				|| expectedNodeId == Defs.FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE_OPTIONS) {
			return true;
		}

		if (expectedNodeId == nodeId) {
			return true;
		}

		return false;
	}

	public ControllerCmd getCurrentControllerCmd() {
		return currentControllerCmd;
	}

	public void setCurrentControllerCmd(ControllerCmd currentControllerCmd) {
		this.currentControllerCmd = currentControllerCmd;
	}

	public ControllerActionListener getControllerActionListener() {
		return controllerActionListener;
	}

	public void setControllerActionListener(
			ControllerActionListener controllerActionListener) {
		this.controllerActionListener = controllerActionListener;
	}

}
