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

import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;

public class ApplicationStatus extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x22;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_APPLICATION_STATUS";

	public static final byte APP_STATUS_CMD_BUSY = 0x01;
	public static final byte APP_STATUS_CMD_REJECTED_REQ = 0x02;

	public ApplicationStatus(Node node) {
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
		if (APP_STATUS_CMD_BUSY == data[0]) {
			String temp;
			switch (data[1]) {
			case 0:
				temp = "Try again later";
				break;
			case 1:
				temp = "Try again in "
						+ String.valueOf(SafeCast.toInt(data[2])) + " seconds";
				break;
			case 2:
				temp = "Request queued, will be executed later";
				break;
			default:
				temp = "Unknown status "
						+ String.valueOf(SafeCast.toInt(data[2]));
				break;
			}
			node.getLog().add("Received Application Status Busy: " + temp);
			return true;
		}

		if (APP_STATUS_CMD_REJECTED_REQ == data[0]) {
			node.getLog().add(
					"Received Application Rejected Request: Status="
							+ String.valueOf(SafeCast.toInt(data[1])));
			return true;
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

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

	@Override
	public boolean setValue(Value value) {
		// TODO Auto-generated method stub
		return false;
	}

}
