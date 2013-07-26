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
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueBool;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;

public class Basic extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x20;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_BASIC";

	public static final byte BASIC_CMD_SET = 0x01;
	public static final byte BASIC_CMD_GET = 0x02;
	public static final byte BASIC_CMD_REPORT = 0x03;

	private byte mapping = 0;
	private boolean ignoreMapping = false;
	private boolean setAsReport = false;

	public Basic(Node node) {
		super(node);
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	public boolean requestValue(int requestFlags, byte dummy1, byte instance,
			QueuePriority queue) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				BASIC_CMD_GET, node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public boolean setValue(Value value) {
		if (ValueType.BYTE == value.getId().getType()) {
			ValueByte vb = (ValueByte) value;
			Msg msg = new Msg(getNodeId(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_SEND_DATA, true);
			msg.setInstance(this, value.getId().getInstance());
			msg.appends(new byte[] { getNodeId(), 3, COMMAND_CLASS_ID,
					BASIC_CMD_SET, vb.getValue(),
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, QueuePriority.Send);
			return true;
		}
		return false;
	}

	public void createVars(byte instance) {
		if (mapping == 0 && node != null) {
			node.getValueManager().createValueByte(ValueGenre.BASIC,
					getCommandClassId(), instance, (byte) 0, "Basic", "",
					false, false, (byte) 0, (byte) 0);
		}
	}

	public void set(byte level) {
		node.getLog().add(COMMAND_CLASS_NAME + String.format("_SET(%d) -- Node : %d", SafeCast.toInt(level), SafeCast.toInt(node.getNodeId())));
		ValueByte value = (ValueByte) getValue((byte) 1, (byte) 0);
		if (value != null) {
			value.set(level);
		}
		/*
		 * if (!ignoreMapping && mapping != 0) { CommandClass cc =
		 * node.getCommandClassManager().getCommandClass(mapping); if (cc !=
		 * null) { Value val = cc.getValue((byte) 1, (byte) 0); if (val != null)
		 * { switch(val.getId().getType()) { case BOOL: ValueBool valbool =
		 * (ValueBool) val; valbool.set(level == 0 ? false : true); break; case
		 * BYTE: ValueByte valbyte = (ValueByte) val; valbyte.set(level); break;
		 * default: break; } } } } else { ValueByte value = (ValueByte)
		 * getValue((byte) 1, (byte) 0); if (value != null) { value.set(level);
		 * } }
		 */
	}

	public boolean setMapping(byte commandClassId) {
		boolean result = false;

		if (commandClassId != NoOperation.COMMAND_CLASS_ID) {
			mapping = commandClassId;
			// removeValue((byte) 1, (byte) 0);
			result = true;
		}

		return result;
	}

	public byte getMapping() {
		return mapping;
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
		if (BASIC_CMD_REPORT == data[0]) {
			if (!ignoreMapping && mapping != 0) {
				updateMappedClass((byte) instance, mapping, data[1]);
			} else {

			}
			ValueByte value = (ValueByte) getValue((byte) instance, (byte) 0);
			if (value != null) {
				value.onValueRefreshed(data[1]);
			}
			node.getLog().add(COMMAND_CLASS_NAME + String.format("_GET(%d) -- Node : %d", SafeCast.toInt(data[1]), SafeCast.toInt(node.getNodeId())));
			return true;
		}

		if (BASIC_CMD_SET == data[0]) {
			if (setAsReport) {
				if (!ignoreMapping && mapping != 0) {
					updateMappedClass((byte) instance, mapping, data[1]);
				} else {

				}
				ValueByte value = (ValueByte) getValue((byte) instance,
						(byte) 0);
				if (value != null) {
					value.onValueRefreshed(data[1]);
				}
			}
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
