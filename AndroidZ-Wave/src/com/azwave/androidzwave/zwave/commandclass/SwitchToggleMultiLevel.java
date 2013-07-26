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
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class SwitchToggleMultiLevel extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x29;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_SWITCH_TOGGLE_MULTILEVEL";

	public static final byte SWITCH_TOGGLE_MULTI_LVL_CMD_SET = 0x01;
	public static final byte SWITCH_TOGGLE_MULTI_LVL_CMD_GET = 0x02;
	public static final byte SWITCH_TOGGLE_MULTI_LVL_CMD_REPORT = 0x03;
	public static final byte SWITCH_TOGGLE_MULTI_LVL_CMD_START_LVL_CHANGE = 0x04;
	public static final byte SWITCH_TOGGLE_MULTI_LVL_CMD_STOP_LVL_CHANGE = 0x05;

	public enum SwitchToggleMultilevelDirection {
		Up, Down
	}

	private static final byte DIRECTION_UP = 0x00;
	private static final byte DIRECTION_DOWN = 0x40;

	public SwitchToggleMultiLevel(Node node) {
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
		return 1;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (SWITCH_TOGGLE_MULTI_LVL_CMD_REPORT == data[0]) {
			ValueByte value = (ValueByte) getValue(instance, (byte) 0);
			if (value != null) {
				value.onValueRefreshed(data[1]);
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
				SWITCH_TOGGLE_MULTI_LVL_CMD_GET,
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
			node.getValueManager().createValueByte(ValueGenre.USER,
					getCommandClassId(), mInstance, (byte) 0, "Level", "",
					false, false, (byte) 0, (byte) 0);
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

	public void startLevelChange(SwitchToggleMultilevelDirection direction,
			boolean ignoreStartLevel, boolean rollover) {
		byte param = direction == SwitchToggleMultilevelDirection.Up ? DIRECTION_UP
				: DIRECTION_DOWN;
		param |= (byte) (ignoreStartLevel ? 0x20 : 0x00);
		param |= (byte) (rollover ? 0x80 : 0x00);

		Msg msg = new Msg(getNodeId(), Defs.REQUEST, Defs.FUNC_ID_ZW_SEND_DATA,
				true);
		msg.appends(new byte[] { getNodeId(), 3, getCommandClassId(),
				SWITCH_TOGGLE_MULTI_LVL_CMD_START_LVL_CHANGE, param,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
	}

	public void stopLevelChange() {
		Msg msg = new Msg(getNodeId(), Defs.REQUEST, Defs.FUNC_ID_ZW_SEND_DATA,
				true);
		msg.appends(new byte[] { getNodeId(), 2, getCommandClassId(),
				SWITCH_TOGGLE_MULTI_LVL_CMD_STOP_LVL_CHANGE,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
	}

}
