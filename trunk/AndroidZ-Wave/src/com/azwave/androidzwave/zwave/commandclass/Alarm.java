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

public class Alarm extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x71;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_ALARM";

	public static final byte ALARM_CMD_GET = 0x04;
	public static final byte ALARM_CMD_REPORT = 0x05;

	public enum AlarmIndex {
		Type, Level
	}

	public Alarm(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
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
	public byte getMaxVersion() {
		return 2;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (ALARM_CMD_REPORT == data[0]) {
			ValueByte value;
			if ((value = (ValueByte) getValue(instance,
					(byte) AlarmIndex.Type.ordinal())) != null) {
				value.onValueRefreshed(data[1]);
			}
			if ((value = (ValueByte) getValue(instance,
					(byte) AlarmIndex.Level.ordinal())) != null) {
				value.onValueRefreshed(data[2]);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		if (isGetSupported()) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.setInstance(this, instance);
			msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
					ALARM_CMD_GET, node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, queue);
			return true;
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance) {
		if (node != null) {
			node.getValueManager().createValueByte(ValueGenre.USER,
					getCommandClassId(), mInstance,
					(byte) AlarmIndex.Type.ordinal(), "Alarm Type", "", true,
					false, (byte) 0, (byte) 0);
			node.getValueManager().createValueByte(ValueGenre.USER,
					getCommandClassId(), mInstance,
					(byte) AlarmIndex.Level.ordinal(), "Alarm Level", "", true,
					false, (byte) 0, (byte) 0);
		}
	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setValue(Value value) {
		// TODO Auto-generated method stub
		return false;
	}

}
