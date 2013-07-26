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

public class ControllerReplication extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x21;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_CONTROLLER_REPLICATION";

	public static final byte CONT_REP_CMD_TRANSFERGROUP = 0x31;
	public static final byte CONT_REP_CMD_TRANSFERGROUP_NAME = 0x32;
	public static final byte CONT_REP_CMD_TRANSFERSCENE = 0x33;
	public static final byte CONT_REP_CMD_TRANSFERSCENE_NAME = 0x34;

	public enum ControllerReplicationIndex {
		NodeId, Function, Replication
	}

	public ControllerReplication(Node node) {
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
	public boolean handleMsg(byte[] data, int length, byte instance) {
		switch (data[0]) {
		case CONT_REP_CMD_TRANSFERGROUP:
			break;
		case CONT_REP_CMD_TRANSFERGROUP_NAME:
			break;
		case CONT_REP_CMD_TRANSFERSCENE:
			break;
		case CONT_REP_CMD_TRANSFERSCENE_NAME:
			break;
		}

		Msg msg = new Msg(getNodeId(), Defs.REQUEST,
				Defs.FUNC_ID_ZW_REPLICATION_COMMAND_COMPLETE, false, false);
		node.getQueueManager().sendMsg(msg, QueuePriority.Command);
		return true;
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
