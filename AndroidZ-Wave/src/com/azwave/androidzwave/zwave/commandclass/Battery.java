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

import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class Battery extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x80;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_BATTERY";

	public static final byte BATTERY_CMD_GET = 0x02;
	public static final byte BATTERY_CMD_REPORT = 0x03;

	public Battery(Node node) {
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

	public boolean requestValue(int requestFlags, byte dummy1, byte instance,
			QueuePriority queue) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				BATTERY_CMD_GET, node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public void createVars(byte instance) {
		if (node != null) {
			node.getValueManager().createValueByte(ValueGenre.USER,
					getCommandClassId(), instance, (byte) 0, "Battery Level",
					"%", true, false, (byte) 100, (byte) 0);
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
		if (BATTERY_CMD_REPORT == data[0]) {
			byte batteryLvl = data[1];
			if (batteryLvl == (byte) 0xFF) {
				batteryLvl = 0;
			}

			ValueByte value = (ValueByte) getValue((byte) instance, (byte) 0);
			if (value != null) {
				value.onValueRefreshed(batteryLvl);
			}
			return true;
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setValue(Value value) {
		// TODO Auto-generated method stub
		return false;
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
