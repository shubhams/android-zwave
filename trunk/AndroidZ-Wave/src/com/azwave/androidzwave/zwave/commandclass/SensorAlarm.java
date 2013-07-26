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
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class SensorAlarm extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x9C;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_SENSOR_ALARM";

	public static final byte SENSOR_ALARM_CMD_GET = (byte) 0x01;
	public static final byte SENSOR_ALARM_CMD_REPORT = (byte) 0x02;
	public static final byte SENSOR_ALARM_CMD_SUPPORTED_GET = (byte) 0x03;
	public static final byte SENSOR_ALARM_CMD_SUPPORTED_REPORT = (byte) 0x04;

	public enum SensorAlarmEnum {
		General, Smoke, CarbonMonoxide, CarbonDioxide, Heat, Flood, Count
	}

	private static final String alarmTypeName[] = { "General", "Smoke",
			"Carbon Monoxide", "Carbon Dioxide", "Heat", "Flood" };

	public SensorAlarm(Node node) {
		super(node);
		setStaticRequest(STATIC_REQUEST_VALUES);
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
		if (SENSOR_ALARM_CMD_REPORT == data[0]) {
			ValueByte value = (ValueByte) getValue(instance, data[2]);
			if (value != null) {
				byte sourceNodeId = data[1];
				byte state = data[3];

				value.onValueRefreshed(state);
			}
			return true;
		}

		if (SENSOR_ALARM_CMD_SUPPORTED_REPORT == data[0] && node != null) {
			byte numBytes = data[1];
			for (int i = 0; i < SafeCast.toInt(numBytes); ++i) {
				for (int bit = 0; bit < 8; ++bit) {
					if ((data[i + 2] & (byte) (1 << bit)) != 0) {
						int index = (i << 3) + bit;
						if (index < SensorAlarmEnum.Count.ordinal()) {
							node.getValueManager().createValueByte(
									ValueGenre.USER, getCommandClassId(),
									instance, (byte) index,
									alarmTypeName[index], "", true, false,
									(byte) 0, (byte) 0);
						}
					}
				}
			}
			clearStaticRequest(STATIC_REQUEST_VALUES);
			return true;
		}
		return false;
	}

	@Override
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		boolean request = false;
		if ((requestFlags & REQUEST_FLAG_STATIC) != 0
				&& hasStaticRequest(STATIC_REQUEST_VALUES)) {
			request = requestValue(requestFlags, (byte) 0xFF, instance, queue);
		}

		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
			for (int i = 0; i < SensorAlarmEnum.Count.ordinal(); i++) {
				Value value = getValue(instance, (byte) i);
				if (value != null) {
					request = requestValue(requestFlags, (byte) i, instance,
							queue);
				}
			}
		}

		return request;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		if (index == (byte) 0xff) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.setInstance(this, instance);
			msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
					SENSOR_ALARM_CMD_SUPPORTED_GET,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, queue);
		} else {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.setInstance(this, instance);
			msg.appends(new byte[] { getNodeId(), 3, COMMAND_CLASS_ID,
					SENSOR_ALARM_CMD_GET, index,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, queue);
		}
		return true;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance) {
		// TODO Auto-generated method stub

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
