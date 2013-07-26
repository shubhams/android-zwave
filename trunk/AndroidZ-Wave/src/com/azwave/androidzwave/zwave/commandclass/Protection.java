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
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueList;
import com.azwave.androidzwave.zwave.values.ValueListItem;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;

public class Protection extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x75;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_PROTECTION";

	public static final byte PROTECTION_CMD_SET = 0x01;
	public static final byte PROTECTION_CMD_GET = 0x02;
	public static final byte PROTECTION_CMD_REPORT = 0x03;

	public static final String protectionStateNames[] = { "Unprotected",
			"Protection by Sequence", "No Operation Possible" };

	public enum ProtectionEnum {
		Unprotected, Sequence, Nop
	}

	public Protection(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_SESSION) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	public boolean requestValue(int requestFlags, byte dummy, byte instance,
			QueuePriority queue) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				PROTECTION_CMD_GET,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public boolean setValue(Value val) {
		if (ValueType.LIST == val.getId().getType()) {
			ValueList value = (ValueList) val;
			ValueListItem item = value.getItem();

			Msg msg = new Msg(getNodeId(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_SEND_DATA, true);
			msg.setInstance(this, value.getId().getInstance());
			msg.appends(new byte[] { getNodeId(), 3, getCommandClassId(),
					PROTECTION_CMD_SET, (byte) item.getValue(),
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, QueuePriority.Send);
			return true;
		}

		return false;
	}

	public void createVars(byte instance) {
		if (node != null) {
			ArrayList<ValueListItem> items = new ArrayList<ValueListItem>();
			ValueListItem item;
			for (int i = 0; i < 3; ++i) {
				item = new ValueListItem();
				item.setLabel(protectionStateNames[i]);
				item.setValue(i);

				items.add(item);
			}

			node.getValueManager().createValueList(ValueGenre.SYSTEM,
					getCommandClassId(), instance, (byte) 0, "Protection", "",
					false, false, (byte) 1, items, (byte) 0, (byte) 0);
		}
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
		if (PROTECTION_CMD_REPORT == data[0]) {
			ValueList value = (ValueList) getValue((byte) instance, (byte) 0);
			if (value != null) {
				value.onValueRefreshed(SafeCast.toInt(data[1]));
			}
			node.getLog().add(COMMAND_CLASS_NAME + String.format("_GET(%d) -- Node : %d", SafeCast.toInt(data[1]), SafeCast.toInt(node.getNodeId())));
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
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

}
