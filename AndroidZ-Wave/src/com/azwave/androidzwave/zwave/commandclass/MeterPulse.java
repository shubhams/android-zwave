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
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueInt;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class MeterPulse extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x35;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_METER_PULSE";

	public static final byte METER_PULSE_CMD_GET = (byte) 0x04;
	public static final byte METER_PULSE_CMD_REPORT = (byte) 0x05;

	public MeterPulse(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte getCommandClassId() {
		// TODO Auto-generated method stub
		return COMMAND_CLASS_ID;
	}

	@Override
	public String getCommandClassName() {
		// TODO Auto-generated method stub
		return COMMAND_CLASS_NAME;
	}

	@Override
	public byte getMaxVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (METER_PULSE_CMD_REPORT == data[0]) {
			int count = 0;
			for (int i = 0; i < 4; i++) {
				count <<= 8;
				count |= SafeCast.toInt(data[i + 1]);
			}

			ValueInt value = (ValueInt) getValue(instance, (byte) 0);
			if (value != null) {
				value.onValueRefreshed(count);
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
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				METER_PULSE_CMD_GET,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance) {
		if (node != null) {
			node.getValueManager().createValueInt(ValueGenre.USER,
					getCommandClassId(), mInstance, (byte) 0, "Count", "",
					true, false, 0, (byte) 0);
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
