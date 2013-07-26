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
import com.azwave.androidzwave.zwave.values.ValueButton;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueInt;
import com.azwave.androidzwave.zwave.values.ValueList;
import com.azwave.androidzwave.zwave.values.ValueShort;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class Configuration extends CommandClass {

	public static final byte COMMAND_CLASS_ID = 0x70;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_CONFIGURATION";

	public static final byte CONFIGURATION_CMD_SET = 0x04;
	public static final byte CONFIGURATION_CMD_GET = 0x05;
	public static final byte CONFIGURATION_CMD_REPORT = 0x06;

	public Configuration(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public boolean setValue(Value value) {
		byte param = value.getId().getIndex();
		switch (value.getId().getType()) {
		case BOOL:
			ValueBool valueBool = (ValueBool) value;
			set(param, (valueBool.getValue() ? 1 : 0), (byte) 1);
			return true;
		case BYTE:
			ValueByte valueByte = (ValueByte) value;
			set(param, SafeCast.toInt(valueByte.getValue()), (byte) 1);
			return true;
		case SHORT:
			ValueShort valueShort = (ValueShort) value;
			set(param, SafeCast.toInt(valueShort.getValue()), (byte) 2);
			return true;
		case INT:
			ValueInt valueInt = (ValueInt) value;
			set(param, valueInt.getValue(), (byte) 4);
			return true;
		case LIST:
			ValueList valueList = (ValueList) value;
			set(param, valueList.getItem().getValue(), valueList.getSize());
			return true;
		case BUTTON:
			ValueButton valueButton = (ValueButton) value;
			set(param, (valueButton.isPressed() ? 1 : 0), (byte) 1);
			return true;
		default:
			break;
		}
		return false;
	}

	public boolean requestValue(int requestFlags, byte parameter,
			byte instance, QueuePriority queue) {
		if (instance != 1) {
			return false;
		}

		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.append(getNodeId());
		msg.append((byte) 3);
		msg.append(getCommandClassId());
		msg.append(CONFIGURATION_CMD_GET);
		msg.append(parameter);
		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public void set(byte parameter, int value, byte size) {
		Msg msg = new Msg(node.getNodeId(), Defs.REQUEST,
				Defs.FUNC_ID_ZW_SEND_DATA, true);
		msg.append(getNodeId());
		msg.append((byte) (4 + SafeCast.toInt(size)));
		msg.append(getCommandClassId());
		msg.append(CONFIGURATION_CMD_SET);
		msg.append(parameter);
		msg.append(size);
		if (size > 2) {
			msg.append((byte) (value >> 24));
			msg.append((byte) (value >> 16));
		}
		if (size > 1) {
			msg.append((byte) (value >> 8));
		}
		msg.append((byte) (value));
		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
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
		if (CONFIGURATION_CMD_REPORT == data[0]) {
			byte parameter = data[1];
			byte size = (byte) (data[2] & 0x07);
			int paramValue = 0;

			for (int i = 0; i < size; i++) {
				paramValue <<= 8;
				paramValue |= SafeCast.toInt(data[i + 3]);
			}

			Value value = getValue((byte) 1, parameter);
			if (value != null) {
				switch (value.getId().getType()) {
				case BOOL:
					((ValueBool) value).onValueRefreshed(paramValue != 0);
					break;
				case BYTE:
					((ValueByte) value).onValueRefreshed((byte) paramValue);
					break;
				case SHORT:
					((ValueShort) value).onValueRefreshed((short) paramValue);
					break;
				case INT:
					((ValueInt) value).onValueRefreshed(paramValue);
					break;
				case LIST:
					((ValueList) value).onValueRefreshed(paramValue);
					break;
				default:
					break;
				}
			} else {
				String label = String.format("Parameter #%d", parameter);
				if (node != null) {
					switch (size) {
					case 1:
						node.getValueManager().createValueByte(
								ValueGenre.CONFIG, getCommandClassId(),
								(byte) instance, parameter, label, "", false,
								false, (byte) paramValue, (byte) 0);
						break;
					case 2:
						node.getValueManager().createValueShort(
								ValueGenre.CONFIG, getCommandClassId(),
								(byte) instance, parameter, label, "", false,
								false, (short) paramValue, (byte) 0);
						break;
					case 4:
						node.getValueManager().createValueInt(
								ValueGenre.CONFIG, getCommandClassId(),
								(byte) instance, parameter, label, "", false,
								false, paramValue, (byte) 0);
						break;
					default:
						break;
					}
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
	public boolean requestState(int requestFlags, byte instance,
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
