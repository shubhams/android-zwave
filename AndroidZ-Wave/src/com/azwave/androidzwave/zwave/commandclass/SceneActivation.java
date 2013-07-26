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

import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.Value;

public class SceneActivation extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x2B;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_SCENE_ACTIVATION";

	public static final byte SCENE_ACTIVATION_CMD_SET = (byte) 0x01;

	public SceneActivation(Node node) {
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
		if (SCENE_ACTIVATION_CMD_SET == data[0]) {
			byte sceneId = data[1];
			byte time = data[2];
			return true;
		}
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
