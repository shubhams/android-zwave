package com.azwave.androidzwave.zwave.commandclass;

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
import com.azwave.androidzwave.zwave.items.ControllerCmd;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem;
import com.azwave.androidzwave.zwave.items.QueueItemComparator;
import com.azwave.androidzwave.zwave.items.QueueItem.QueueCommand;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueInt;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;

public class WakeUp extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x84;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_WAKE_UP";

	public static final byte INTERVAL_SET = (byte) 0x04;
	public static final byte INTERVAL_GET = (byte) 0x05;
	public static final byte INTERVAL_REPORT = (byte) 0x06;
	public static final byte INTERVAL_CAP_GET = (byte) 0x09;
	public static final byte INTERVAL_CAP_REP = (byte) 0x0A;

	public static final byte NOTIFICATION = (byte) 0x07;
	public static final byte NO_MORE_INFO = (byte) 0x08;

	private boolean awake = true;
	private boolean pollRequired = false;
	private boolean notification = false;

	private PriorityBlockingQueue<QueueItem> pendingQueue; // Messages waiting

	// to be sent when
	// the device wakes
	// up

	public WakeUp(Node node) {
		super(node);
		pendingQueue = new PriorityBlockingQueue<QueueItem>(13,
				new QueueItemComparator());
		setStaticRequest(STATIC_REQUEST_VALUES);
	}

	public void init() {
		requestState(REQUEST_FLAG_SESSION, (byte) 1, QueuePriority.WakeUp);
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		boolean requests = false;

		if ((requestFlags & REQUEST_FLAG_STATIC) != 0
				&& hasStaticRequest(STATIC_REQUEST_VALUES)) {
			if (getVersion() > 1) {
				requests |= requestValue(requestFlags, INTERVAL_CAP_GET,
						instance, queue);
			}
		}

		if ((requestFlags & REQUEST_FLAG_SESSION) != 0) {
			if (node != null && !node.isController()) {
				requests |= requestValue(requestFlags, (byte) 0, instance,
						queue);
			}
		}

		return requests;
	}

	public boolean requestValue(int requestFlags, byte getTypeEnum,
			byte instance, QueuePriority queue) {
		if (instance != 1) {
			return false;
		}
		if (getTypeEnum == INTERVAL_CAP_GET) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.append(getNodeId());
			msg.append((byte) 2);
			msg.append(getCommandClassId());
			msg.append(INTERVAL_CAP_GET);
			msg.append(node.getQueueManager().getTransmitOptions());
			node.getQueueManager().sendMsg(msg, queue);
		}

		if (getTypeEnum == 0) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.append(getNodeId());
			msg.append((byte) 2);
			msg.append(getCommandClassId());
			msg.append(INTERVAL_GET);
			msg.append(node.getQueueManager().getTransmitOptions());
			node.getQueueManager().sendMsg(msg, queue);
			return true;
		}

		return false;
	}

	public boolean setValue(Value value) {
		if (ValueType.INT == value.getId().getType()) {
			ValueInt val = (ValueInt) value;

			Msg msg = new Msg(getNodeId(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_SEND_DATA, true);
			msg.append(getNodeId());

			if (node.getCommandClassManager().getCommandClass(
					MultiCommand.COMMAND_CLASS_ID) != null) {
				msg.append((byte) 10);
				msg.append(MultiCommand.COMMAND_CLASS_ID);
				msg.append(MultiCommand.MULTI_COMMAND_CMD_ENCAP);
				msg.append((byte) 1);
			}

			int interval = val.getValue();

			msg.append((byte) 6);
			msg.append(getCommandClassId());
			msg.append(INTERVAL_SET);
			msg.append((byte) ((interval >> 16) & 0xFF));
			msg.append((byte) ((interval >> 8) & 0xFF));
			msg.append((byte) (interval & 0xFF));
			msg.append(node.getPrimaryNodeId());
			msg.append(node.getQueueManager().getTransmitOptions());

			node.getQueueManager().sendMsg(msg, QueuePriority.WakeUp);
			return true;
		}

		return false;
	}

	public void setVersion(byte version) {
		super.setVersion(version);
		createVars((byte) 1);
	}

	public void setAwake(boolean state) {
		if (awake != state) {
			awake = state;
		}
		if (awake) {
			if (pollRequired) {
				if (node != null) {
					node.setQueryStage(QueryStage.Dynamic);
				}
				pollRequired = false;
			}
			sendPending();
		}
	}

	public synchronized void queueItem(QueueItem item) {
		for (QueueItem it : pendingQueue) {
			if (item == it) {
				if (QueueCommand.SendMessage == item.getCommand()) {
					item.setMsg(null);
				} else if (QueueCommand.Controller == item.getCommand()) {
					item.setCci(null);
				}
				pendingQueue.remove(it);
			}
		}
		pendingQueue.add(item);
	}

	public void sendPending() {
		awake = true;
		synchronized (pendingQueue) {
			for (QueueItem item : pendingQueue) {
				if (QueueCommand.SendMessage == item.getCommand()) {
					node.getQueueManager().sendMsg(item.getMsg(),
							QueuePriority.WakeUp);
				} else if (QueueCommand.QueryStageComplete == item.getCommand()) {
					node.getQueueManager().sendQueryStageComplete(
							item.getNodeId(), item.getQueryStage());
				} else if (QueueCommand.Controller == item.getCommand()) {
					ControllerCmd cci = item.getCci();
					node.getQueueManager().sendControllerCommand(
							cci.getControllerCommand(),
							cci.getControllerCallback(),
							cci.getControllerCallbackContext(),
							cci.isHighPower(), cci.getControllerCommandNode(),
							cci.getControllerCommandArg());
					item.setCci(null);
				}
			}
		}

		// Send the device back to sleep, unless we have outstanding queries.
		boolean sendToSleep = notification;
		if (node != null) {
			if (!node.allQueriesCompleted()) {
				sendToSleep = false;
			}
		}

		if (sendToSleep) {
			notification = false;
			Msg msg = new Msg(getNodeId(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_SEND_DATA, true);
			msg.append(getNodeId());
			msg.append((byte) 2);
			msg.append(getCommandClassId());
			msg.append(NO_MORE_INFO);
			msg.append(node.getQueueManager().getTransmitOptions());
			node.getQueueManager().sendMsg(msg, QueuePriority.WakeUp);
		}
	}

	public void createVars(byte instance) {
		if (node != null) {
			if (!node.isController()) {
				switch (getVersion()) {
				case 1:
					node.getValueManager().createValueInt(ValueGenre.SYSTEM,
							getCommandClassId(), instance, (byte) 0,
							"Wake-up Interval", "Seconds", false, false, 3600,
							(byte) 0);
					break;
				case 2:
					node.getValueManager().createValueInt(ValueGenre.SYSTEM,
							getCommandClassId(), instance, (byte) 1,
							"Minimum Wake-up Interval", "Seconds", true, false,
							0, (byte) 0);
					node.getValueManager().createValueInt(ValueGenre.SYSTEM,
							getCommandClassId(), instance, (byte) 2,
							"Maximum Wake-up Interval", "Seconds", true, false,
							0, (byte) 0);
					node.getValueManager().createValueInt(ValueGenre.SYSTEM,
							getCommandClassId(), instance, (byte) 3,
							"Default Wake-up Interval", "Seconds", true, false,
							0, (byte) 0);
					node.getValueManager().createValueInt(ValueGenre.SYSTEM,
							getCommandClassId(), instance, (byte) 4,
							"Wake-up Interval Step", "Seconds", true, false, 0,
							(byte) 0);
					break;
				}
			}
		}
	}

	public boolean isAwake() {
		return awake;
	}

	public byte getMaxVersion() {
		return 2;
	}

	public void setPollRequired() {
		pollRequired = true;
	}

	@Override
	public byte getCommandClassId() {
		return COMMAND_CLASS_ID;
	}

	@Override
	public String getCommandClassName() {
		return COMMAND_CLASS_NAME;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (INTERVAL_REPORT == data[0]) {
			ValueInt value = (ValueInt) getValue((byte) instance, (byte) 0);
			if (value != null) {
				if (length < 6) {
					return false;
				}
				int interval = SafeCast.toInt(data[1]) << 16;
				interval |= SafeCast.toInt(data[2]) << 8;
				interval |= SafeCast.toInt(data[3]);

				byte targetNodeId = data[4];
				value.onValueRefreshed(interval);

				if (node.getPrimaryNodeId() != targetNodeId
						&& !node.isListeningDevice()) {
					setValue(value);
				}
			}
			return true;
		} else if (NOTIFICATION == data[0]) {
			notification = true;
			setAwake(true);
			return true;
		} else if (INTERVAL_CAP_REP == data[0]) {
			int mininterval = (SafeCast.toInt(data[1]) << 16)
					| (SafeCast.toInt(data[2]) << 8) | SafeCast.toInt(data[3]);
			int maxinterval = (SafeCast.toInt(data[4]) << 16)
					| (SafeCast.toInt(data[5]) << 8) | SafeCast.toInt(data[6]);
			int definterval = (SafeCast.toInt(data[7]) << 16)
					| (SafeCast.toInt(data[8]) << 8) | SafeCast.toInt(data[9]);
			int stepinterval = (SafeCast.toInt(data[10]) << 16)
					| (SafeCast.toInt(data[11]) << 8)
					| SafeCast.toInt(data[12]);

			ValueInt value = (ValueInt) getValue((byte) instance, (byte) 1);
			if (value != null) {
				value.onValueRefreshed(mininterval);
			}

			value = (ValueInt) getValue((byte) instance, (byte) 2);
			if (value != null) {
				value.onValueRefreshed(maxinterval);
			}

			value = (ValueInt) getValue((byte) instance, (byte) 3);
			if (value != null) {
				value.onValueRefreshed(definterval);
			}

			value = (ValueInt) getValue((byte) instance, (byte) 4);
			if (value != null) {
				value.onValueRefreshed(stepinterval);
			}

			clearStaticRequest(STATIC_REQUEST_VALUES);
			return true;
		}

		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

}
