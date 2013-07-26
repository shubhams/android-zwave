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
import com.azwave.androidzwave.zwave.values.ValueButton;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;

public class BasicWindowCovering extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x50;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_BASIC_WINDOW_COVERING";

	public static final byte BASIC_WINDOW_COVERING_CMD_START_LVL_CHANGE = (byte) 0x01;
	public static final byte BASIC_WINDOW_COVERING_CMD_STOP_LVL_CHANGE = (byte) 0x02;

	public enum BasicWindowCoveringIndex {
		Open, Close
	}

	public BasicWindowCovering(Node node) {
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance) {
		if (node != null) {
			node.getValueManager().createValueButton(ValueGenre.USER,
					getCommandClassId(), mInstance,
					(byte) BasicWindowCoveringIndex.Open.ordinal(), "Open",
					(byte) 0);
			node.getValueManager().createValueButton(ValueGenre.USER,
					getCommandClassId(), mInstance,
					(byte) BasicWindowCoveringIndex.Close.ordinal(), "Close",
					(byte) 0);
		}
	}

	@Override
	public void createVars(byte mInstance, byte index) {

	}

	@Override
	public boolean setValue(Value value) {
		if (ValueType.BUTTON == value.getId().getType()) {
			ValueButton button = (ValueButton) value;

			byte action = (byte) 0x40;
			Msg msg = null;
			if (button != null && button.getId().getIndex() != 0) {
				action = 0;
			}

			if (button != null && button.isPressed()) {
				msg = new Msg(getNodeId(), Defs.REQUEST,
						Defs.FUNC_ID_ZW_SEND_DATA, true);
				msg.setInstance(this, value.getId().getInstance());
				msg.append(getNodeId());
				msg.append((byte) 3);
				msg.append(getCommandClassId());
				msg.append(BASIC_WINDOW_COVERING_CMD_START_LVL_CHANGE);
				msg.append(action);
				msg.append(node.getQueueManager().getTransmitOptions());
				node.getQueueManager().sendMsg(msg, QueuePriority.Send);
				return true;
			} else {
				msg = new Msg(getNodeId(), Defs.REQUEST,
						Defs.FUNC_ID_ZW_SEND_DATA, true);
				msg.setInstance(this, value.getId().getInstance());
				msg.append(getNodeId());
				msg.append((byte) 2);
				msg.append(getCommandClassId());
				msg.append(BASIC_WINDOW_COVERING_CMD_STOP_LVL_CHANGE);
				msg.append(node.getQueueManager().getTransmitOptions());
				node.getQueueManager().sendMsg(msg, QueuePriority.Send);
				return true;
			}
		}
		return false;
	}

}
