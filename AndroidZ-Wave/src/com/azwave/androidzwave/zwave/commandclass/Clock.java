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

import java.util.ArrayList;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueList;
import com.azwave.androidzwave.zwave.values.ValueListItem;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class Clock extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x81;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_CLOCK";

	public static final byte CLOCK_CMD_SET = 0x04;
	public static final byte CLOCK_CMD_GET = 0x05;
	public static final byte CLOCK_CMD_REPORT = 0x06;

	public enum ClockIndex {
		Day, Hour, Minute
	}

	private static final String dayNames[] = { "Invalid", "Monday", "Tuesday",
			"Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	public Clock(Node node) {
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
		if (CLOCK_CMD_REPORT == data[0]) {
			byte day = (byte) (data[1] >> 5);
			byte hour = (byte) (data[1] & (byte) 0x1F);
			byte minute = data[2];

			ValueList dayValue = (ValueList) getValue(instance,
					(byte) ClockIndex.Day.ordinal());
			if (dayValue != null) {
				dayValue.onValueRefreshed(day);
			}

			ValueByte hourValue = (ValueByte) getValue(instance,
					(byte) ClockIndex.Hour.ordinal());
			if (hourValue != null) {
				hourValue.onValueRefreshed(hour);
			}

			ValueByte minuteValue = (ValueByte) getValue(instance,
					(byte) ClockIndex.Minute.ordinal());
			if (minuteValue != null) {
				minuteValue.onValueRefreshed(minute);
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
				CLOCK_CMD_GET, node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
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
		if (node != null) {
			ArrayList<ValueListItem> items = new ArrayList<ValueListItem>();
			for (int i = 1; i <= 7; ++i) {
				ValueListItem item = new ValueListItem();
				item.setLabel(dayNames[i]);
				item.setValue(i);
				items.add(item);
			}

			node.getValueManager().createValueList(ValueGenre.USER,
					getCommandClassId(), mInstance,
					(byte) ClockIndex.Day.ordinal(), "Day", "", false, false,
					(byte) 1, items, (byte) 0, (byte) 0);
			node.getValueManager().createValueByte(ValueGenre.USER,
					getCommandClassId(), mInstance,
					(byte) ClockIndex.Hour.ordinal(), "Hour", "", false, false,
					(byte) 12, (byte) 0);
			node.getValueManager().createValueByte(ValueGenre.USER,
					getCommandClassId(), mInstance,
					(byte) ClockIndex.Minute.ordinal(), "Minute", "", false,
					false, (byte) 0, (byte) 0);
		}
	}

	@Override
	public boolean setValue(Value value) {
		boolean ret = false;
		byte instance = value.getId().getInstance();

		ValueList dayValue = (ValueList) getValue(instance,
				(byte) ClockIndex.Day.ordinal());
		ValueByte hourValue = (ValueByte) getValue(instance,
				(byte) ClockIndex.Hour.ordinal());
		ValueByte minuteValue = (ValueByte) getValue(instance,
				(byte) ClockIndex.Minute.ordinal());

		if (dayValue != null && hourValue != null && minuteValue != null) {
			byte day = (byte) dayValue.getItem().getValue();
			byte hour = hourValue.getValue();
			byte minute = minuteValue.getValue();

			Msg msg = new Msg(getNodeId(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_SEND_DATA, true);
			msg.setInstance(this, value.getId().getInstance());
			msg.appends(new byte[] { getNodeId(), 4, COMMAND_CLASS_ID,
					CLOCK_CMD_SET, (byte) ((day << 5) | hour), minute,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, QueuePriority.Send);

			ret = true;
		}

		return ret;
	}

}
