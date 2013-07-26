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

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;

public class QueueItem {

	public enum QueueCommand {
		SendMessage, QueryStageComplete, Controller
	}

	public enum QueuePriority {
		Command, NoOperation, Controller, WakeUp, Send, Query, Poll
	}

	private QueueCommand command = QueueCommand.SendMessage;
	private QueuePriority priority = QueuePriority.Send;
	private Msg message = null;
	private ControllerCmd cci = null;
	private byte nodeId = Defs.NODE_BROADCAST;
	private QueryStage queryStage = QueryStage.None;
	private boolean retry = false;
	private long queueCount = 0;

	public QueueItem() {
	}

	public QueueItem(Msg msg) {
		message = msg;
		nodeId = message.getNodeId();
	}

	public QueueCommand getCommand() {
		return command;
	}

	public void setCommand(QueueCommand command) {
		this.command = command;
	}

	public QueuePriority getPriority() {
		return priority;
	}

	public void setPriority(QueuePriority priority) {
		this.priority = priority;
	}

	public Msg getMsg() {
		return message;
	}

	public void setMsg(Msg message) {
		this.message = message;
	}

	public ControllerCmd getCci() {
		return cci;
	}

	public void setCci(ControllerCmd cci) {
		this.cci = cci;
	}

	public byte getNodeId() {
		return nodeId;
	}

	public void setNodeId(byte nodeId) {
		this.nodeId = nodeId;
	}

	public QueryStage getQueryStage() {
		return queryStage;
	}

	public void setQueryStage(QueryStage queryStage) {
		this.queryStage = queryStage;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public long getQueueCount() {
		return queueCount;
	}

	public void setQueueCount(long queueCount) {
		this.queueCount = queueCount;
	}

}
