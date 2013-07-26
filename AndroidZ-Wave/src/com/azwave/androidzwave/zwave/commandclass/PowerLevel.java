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
import java.util.Vector;

import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueButton;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueList;
import com.azwave.androidzwave.zwave.values.ValueListItem;
import com.azwave.androidzwave.zwave.values.ValueShort;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class PowerLevel extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x73;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_POWERLEVEL";

	public static final byte POWER_LVL_CMD_SET = 0x01;
	public static final byte POWER_LVL_CMD_GET = 0x02;
	public static final byte POWER_LVL_CMD_REPORT = 0x03;
	public static final byte POWER_LVL_CMD_TEST_NODE_SET = 0x04;
	public static final byte POWER_LVL_CMD_TEST_NODE_GET = 0x05;
	public static final byte POWER_LVL_CMD_TEST_NODE_REPORT = 0x06;

	public enum PowerLevelEnum {
		Normal, Minus1dB, Minus2dB, Minus3dB, Minus4dB, Minus5dB, Minus6dB, Minus7dB, Minus8dB, Minus9dB
	}

	public enum PowerLevelStatusEnum {
		Failed, Success, InProgress
	}

	public enum PowerlevelIndex {
		Powerlevel, Timeout, Set, TestNode, TestPowerlevel, TestFrames, Test, Report, TestStatus, TestAckFrames
	}

	public static final String[] powerLevelNames = { "Normal", "-1dB", "-2dB",
			"-3dB", "-4dB", "-5dB", "-6dB", "-7dB", "-8dB", "-9dB" };

	public static final String[] powerLevelStatusNames = { "Failed", "Success",
			"In Progress" };

	public PowerLevel(Node node) {
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
		
		ValueList vallist;
		ValueListItem valitem;
		ValueByte valbyte;
		ValueShort  valshort;
		
		if (data[0] == POWER_LVL_CMD_REPORT) {
			PowerLevelEnum powerlevel = PowerLevelEnum.values()[data[1]];
			byte timeout = data[2];
			
			vallist = (ValueList) getValue(instance, (byte) PowerlevelIndex.Powerlevel.ordinal());
			if (vallist != null) {
				vallist.onValueRefreshed(powerlevel.ordinal());
			}
			
			valbyte = (ValueByte) getValue(instance, (byte) PowerlevelIndex.Timeout.ordinal());
			if (valbyte != null) {
				valbyte.onValueRefreshed(timeout);
			}
			return true;
		}
		
		if (data[0] == POWER_LVL_CMD_TEST_NODE_REPORT) {
			byte testNode = data[1];
			PowerLevelStatusEnum status = PowerLevelStatusEnum.values()[data[2]];
			short ackCount = (short) (SafeCast.toInt(data[3]) << 8);
			
			valbyte = (ValueByte) getValue(instance, (byte) PowerlevelIndex.TestNode.ordinal());
			if (valbyte != null) {
				valbyte.onValueRefreshed(testNode);
			}
			
			vallist = (ValueList) getValue(instance, (byte) PowerlevelIndex.TestStatus.ordinal());
			if (vallist != null) {
				vallist.onValueRefreshed(status.ordinal());
			}
			
			valshort = (ValueShort) getValue(instance, (byte) PowerlevelIndex.TestAckFrames.ordinal());
			if (valshort != null) {
				valshort.onValueRefreshed(ackCount);
			}
			
			return true;
		}
		return false;
	}

	@Override
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_SESSION) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		if (index == 0) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.setInstance(this, instance);
			msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
					POWER_LVL_CMD_GET,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, queue);
			return true;
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	public boolean set(byte instance) {
		byte timeout = 0;
		PowerLevelEnum powerLevel = PowerLevelEnum.Normal;

		ValueList vallist = (ValueList) getValue(instance,
				(byte) PowerlevelIndex.Powerlevel.ordinal());
		if (vallist != null) {
			ValueListItem item = vallist.getItem();
			powerLevel = PowerLevelEnum.values()[item.getValue()];
		} else {
			return false;
		}

		ValueByte valbyte = (ValueByte) getValue(instance,
				(byte) PowerlevelIndex.Timeout.ordinal());
		if (valbyte != null) {
			timeout = valbyte.getValue();
		} else {
			return false;
		}

		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 4, COMMAND_CLASS_ID,
				POWER_LVL_CMD_SET, (byte) powerLevel.ordinal(), timeout,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
		return true;
	}

	public boolean test(byte instance) {
		byte testNodeId = 0;
		PowerLevelEnum powerLevel = PowerLevelEnum.Normal;
		short numFrames = 0;

		ValueByte valbyte = (ValueByte) getValue(instance,
				(byte) PowerlevelIndex.TestNode.ordinal());
		if (valbyte != null) {
			testNodeId = valbyte.getValue();
		} else {
			return false;
		}

		ValueList vallist = (ValueList) getValue(instance,
				(byte) PowerlevelIndex.TestPowerlevel.ordinal());
		if (vallist != null) {
			ValueListItem item = vallist.getItem();
			powerLevel = PowerLevelEnum.values()[item.getValue()];
		} else {
			return false;
		}

		ValueShort valshort = (ValueShort) getValue(instance,
				(byte) PowerlevelIndex.TestFrames.ordinal());
		if (valshort != null) {
			numFrames = valshort.getValue();
		} else {
			return false;
		}

		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 6, COMMAND_CLASS_ID,
				POWER_LVL_CMD_TEST_NODE_SET, testNodeId,
				(byte) powerLevel.ordinal(), (byte) (numFrames >> 8),
				(byte) (numFrames & 0x00FF),
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
		return true;
	}

	public boolean report(byte instance) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 6, COMMAND_CLASS_ID,
				POWER_LVL_CMD_TEST_NODE_GET,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
		return true;
	}

	@Override
	public void createVars(byte mInstance) {
		if (node != null) {
			ArrayList<ValueListItem> items = new ArrayList<ValueListItem>();
			ValueListItem item;
			for (int i = 0; i < 10; ++i) {
				item = new ValueListItem();
				item.setLabel(powerLevelNames[i]);
				item.setValue(i);
				items.add(item);
			}

			node.getValueManager().createValueList(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.Powerlevel.ordinal(), "Powerlevel",
					"dB", false, false, (byte) 1, items, 0, (byte) 0);
			node.getValueManager().createValueByte(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.Timeout.ordinal(), "Timeout",
					"seconds", false, false, (byte) 0, (byte) 0);
			node.getValueManager().createValueButton(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.Set.ordinal(), "Set Powerlevel",
					(byte) 0);
			node.getValueManager().createValueByte(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.TestNode.ordinal(), "Test Node", "",
					false, false, (byte) 0, (byte) 0);
			node.getValueManager().createValueList(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.TestPowerlevel.ordinal(),
					"Test Powerlevel", "dB", false, false, (byte) 1, items, 0,
					(byte) 0);
			node.getValueManager().createValueShort(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.TestFrames.ordinal(), "Frame Count",
					"", false, false, (byte) 0, (byte) 0);
			node.getValueManager().createValueButton(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.Test.ordinal(), "Test", (byte) 0);
			node.getValueManager()
					.createValueButton(ValueGenre.SYSTEM, getCommandClassId(),
							mInstance, (byte) PowerlevelIndex.Report.ordinal(),
							"Report", (byte) 0);

			items = new ArrayList<ValueListItem>();
			for (int i = 0; i < 3; ++i) {
				item = new ValueListItem();
				item.setLabel(powerLevelStatusNames[i]);
				item.setValue(i);
				items.add(item);
			}

			node.getValueManager().createValueList(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.TestStatus.ordinal(), "Test Status",
					"", true, false, (byte) 1, items, 0, (byte) 0);
			node.getValueManager().createValueShort(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) PowerlevelIndex.TestAckFrames.ordinal(),
					"Acked Frames", "", true, false, (byte) 0, (byte) 0);
		}
	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setValue(Value value) {
		boolean res = false;
		byte instance = value.getId().getInstance();
		
		ValueList vallist;
		ValueListItem valitem;
		ValueByte valbyte;
		ValueButton valbutton;
		ValueShort  valshort;

		switch (PowerlevelIndex.values()[SafeCast.toInt(value.getId()
				.getIndex())]) {
		case Powerlevel:
			vallist = (ValueList) getValue(instance, (byte) PowerlevelIndex.Powerlevel.ordinal());
			if (vallist != null) {
				valitem = ((ValueList) value).getItem();
				vallist.onValueRefreshed(valitem.getValue());
			}
			res = true;
			break;
		case Timeout:
			valbyte = (ValueByte) getValue(instance, (byte) PowerlevelIndex.Timeout.ordinal());
			if (valbyte != null) {
				valbyte.onValueRefreshed(((ValueByte) value).getValue());
			}
			res = true;
			break;
		case Set:
			valbutton = (ValueButton) getValue(instance, (byte) PowerlevelIndex.Set.ordinal());
			if (valbutton != null && valbutton.isPressed()) {
				res = set(instance);
			}
			break;
		case TestNode:
			valbyte = (ValueByte) getValue(instance, (byte) PowerlevelIndex.TestNode.ordinal());
			if (valbyte != null) {
				valbyte.onValueRefreshed(((ValueByte) value).getValue());
			}
			res = true;
			break;
		case TestPowerlevel:
			vallist = (ValueList) getValue(instance, (byte) PowerlevelIndex.TestPowerlevel.ordinal());
			if (vallist != null) {
				valitem = ((ValueList) value).getItem();
				vallist.onValueRefreshed(valitem.getValue());
			}
			res = true;
			break;
		case TestFrames:
			valshort = (ValueShort) getValue(instance, (byte) PowerlevelIndex.TestFrames.ordinal());
			if (valshort != null) {
				valshort.onValueRefreshed(((ValueShort) value).getValue());
			}
			res = true;
			break;
		case Test:
			valbutton = (ValueButton) getValue(instance, (byte) PowerlevelIndex.Test.ordinal());
			if (valbutton != null && valbutton.isPressed()) {
				res = test(instance);
			}
			break;
		case Report:
			valbutton = (ValueButton) getValue(instance, (byte) PowerlevelIndex.Report.ordinal());
			if (valbutton != null && valbutton.isPressed()) {
				res = report(instance);
			}
			break;
		default:
			break;
		}

		return res;
	}

}
