package com.azwave.androidzwave.zwave.commandclass;

//-----------------------------------------------------------------------------
//Copyright (c) 2012 Greg Satz <satz@iranger.com>
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
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;

public class NoOperation extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x00;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_NO_OPERATION";

	public NoOperation(Node node) {
		super(node);
	}

	public void set(boolean route) {
		set(route, QueuePriority.NoOperation);
	}

	public void set(boolean route, QueuePriority priority) {
		node.getLog().add(COMMAND_CLASS_NAME + "_SET() -- Node : " + String.valueOf(SafeCast.toInt(node.getNodeId())));
		Msg msg = new Msg(node.getNodeId(), Defs.REQUEST,
				Defs.FUNC_ID_ZW_SEND_DATA, true);
		msg.append(node.getNodeId());
		msg.append((byte) 2);
		msg.append(COMMAND_CLASS_ID);
		msg.append((byte) 0);

		if (route) {
			msg.append(node.getQueueManager().getTransmitOptions());
		} else {
			msg.append((byte) (Defs.TRANSMIT_OPTION_ACK | Defs.TRANSMIT_OPTION_NO_ROUTE));
		}

		node.getQueueManager().sendMsg(msg, priority);
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		return true;
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
	public void createVars(byte mInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

}
