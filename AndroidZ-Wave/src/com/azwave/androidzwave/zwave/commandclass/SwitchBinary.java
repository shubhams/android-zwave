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

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.commandclass.SwitchMultiLevel.SwitchMultilevelIndex;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueBool;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;

public class SwitchBinary extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x25;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_SWITCH_BINARY";

	public static final byte SWITCH_BINARY_CMD_SET = 0x01;
	public static final byte SWITCH_BINARY_CMD_GET = 0x02;
	public static final byte SWITCH_BINARY_CMD_REPORT = 0x03;

	public SwitchBinary(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	public boolean requestValue(int requestFlags, byte dummy, byte instance,
			QueuePriority queue) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				SWITCH_BINARY_CMD_GET,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public boolean setValue(Value val) {
		if (ValueType.BOOL == val.getId().getType()) {
			ValueBool value = (ValueBool) val;

			Msg msg = new Msg(getNodeId(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_SEND_DATA, true);
			msg.setInstance(this, value.getId().getInstance());
			msg.appends(new byte[] { getNodeId(), 3, getCommandClassId(),
					SWITCH_BINARY_CMD_SET,
					value.getValue() ? (byte) 0xFF : 0x00,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, QueuePriority.Send);
			return true;
		}
		return false;
	}

	public void createVars(byte instance) {
		if (node != null) {
			node.getValueManager().createValueBool(ValueGenre.USER,
					getCommandClassId(), instance, (byte) 0, "Switch", "",
					false, false, false, (byte) 0);
		}
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
		if (SWITCH_BINARY_CMD_REPORT == data[0]) {
			ValueBool value = (ValueBool) getValue((byte) instance, (byte) 0);
			if (value != null) {
				value.onValueRefreshed(data[1] != 0);
			}
			node.getLog().add(COMMAND_CLASS_NAME + String.format("_GET(%b) -- Node : %d", data[1] != 0, SafeCast.toInt(node.getNodeId())));
			return true;
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte value) {
		requestValue(0, (byte) 0, instance, QueuePriority.Send);
		if (node != null) {
			WakeUp wu = (WakeUp) node.getCommandClassManager().getCommandClass(
					WakeUp.COMMAND_CLASS_ID);
			if (wu != null && !wu.isAwake()) {
				ValueBool val = (ValueBool) getValue(instance, (byte) 0);
				if (val != null) {
					val.onValueRefreshed(value != 0);
				}
			}
		}
	}

	@Override
	public byte getMaxVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

}
