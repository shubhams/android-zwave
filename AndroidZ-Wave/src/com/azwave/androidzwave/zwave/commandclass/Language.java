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
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueString;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class Language extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x89;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_LANGUAGE";

	public static final byte LANGUAGE_CMD_SET = (byte) 0x01;
	public static final byte LANGUAGE_CMD_GET = (byte) 0x02;
	public static final byte LANGUAGE_CMD_REPORT = (byte) 0x03;

	public enum LanguageIndex {
		Language, Country
	}

	public Language(Node node) {
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
		if (LANGUAGE_CMD_REPORT == data[0]) {
			String language = new String(
					new byte[] { data[1], data[2], data[3] });
			String country = new String(new byte[] { data[5], data[6] });

			clearStaticRequest(STATIC_REQUEST_VALUES);
			ValueString value;
			value = (ValueString) getValue(instance,
					(byte) LanguageIndex.Language.ordinal());
			if (value != null) {
				value.onValueRefreshed(language);
			}
			value = (ValueString) getValue(instance,
					(byte) LanguageIndex.Country.ordinal());
			if (value != null) {
				value.onValueRefreshed(country);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_STATIC) != 0
				&& hasStaticRequest(STATIC_REQUEST_VALUES)) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		if (instance != 1) {
			return false;
		}
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				LANGUAGE_CMD_GET, node.getQueueManager().getTransmitOptions() });
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
			node.getValueManager().createValueString(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) LanguageIndex.Language.ordinal(), "Language", "",
					false, false, "", (byte) 0);
			node.getValueManager().createValueString(ValueGenre.SYSTEM,
					getCommandClassId(), mInstance,
					(byte) LanguageIndex.Country.ordinal(), "Country", "",
					false, false, "", (byte) 0);
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
