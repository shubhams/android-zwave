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
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class SwitchMultiLevel extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x26;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_SWITCH_MULTILEVEL";

	public static final byte SWITCH_MULTILEVEL_CMD_SET = 0x01;
	public static final byte SWITCH_MULTILEVEL_CMD_GET = 0x02;
	public static final byte SWITCH_MULTILEVEL_CMD_REPORT = 0x03;
	public static final byte SWITCH_MULTILEVEL_CMD_START_LEVEL_CHANGE = 0x04;
	public static final byte SWITCH_MULTILEVEL_CMD_STOP_LEVEL_CHANGE = 0x05;
	public static final byte SWITCH_MULTILEVEL_CMD_SUPPORTED_GET = 0x06;
	public static final byte SWITCH_MULTILEVEL_CMD_SUPPORTED_REPORT = 0x07;

	public enum SwitchMultilevelDirection {
		Up, Down, Inc, Dec
	};

	public enum SwitchMultilevelIndex {
		Level, Bright, Dim, IgnoreStartLevel, StartLevel, Duration, Step, Inc, Dec
	};

	private static final String[] switchLabelsPos = { "Undefined", "On", "Up",
			"Open", "Clockwise", "Right", "Forward", "Push" };
	private static final String[] switchLabelsNeg = { "Undefined", "Off",
			"Down", "Close", "Counter-Clockwise", "Left", "Reverse", "Pull" };
	private static final byte[] directionParams = { 0x18, 0x58, (byte) 0xc0,
			(byte) 0xc8 };

	public SwitchMultiLevel(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
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
				SWITCH_MULTILEVEL_CMD_GET,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public void setVersion(byte version) {
		super.setVersion(version);
		if (version == 3) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
					SWITCH_MULTILEVEL_CMD_SUPPORTED_GET,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, QueuePriority.Send);
			setStaticRequest(STATIC_REQUEST_VERSION);
		}
	}

	public boolean setValue(Value val) {
		boolean res = false;
		byte instance = val.getId().getInstance();

		ValueButton button = null;
		ValueByte value = null;
		ValueBool bool = null;

		switch (SwitchMultilevelIndex.values()[val.getId().getIndex()]) {
		case Level:
			value = (ValueByte) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Level.ordinal());
			if (value != null) {
				res = setLevel(instance, ((ValueByte) val).getValue());
			}
			break;
		case Bright:
			button = (ValueButton) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Bright.ordinal());
			if (button != null) {
				if (button.isPressed()) {
					res = startLevelChange(instance,
							SwitchMultilevelDirection.Up.ordinal());
				} else {
					res = stopLevelChange(instance);
				}
			}
			break;
		case Dim:
			button = (ValueButton) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Dim.ordinal());
			if (button != null) {
				if (button.isPressed()) {
					res = startLevelChange(instance,
							SwitchMultilevelDirection.Down.ordinal());
				} else {
					res = stopLevelChange(instance);
				}
			}
			break;
		case IgnoreStartLevel:
			bool = (ValueBool) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.IgnoreStartLevel.ordinal());
			if (bool != null) {
				bool.onValueRefreshed(((ValueBool) val).getValue());
			}
			res = true;
			break;
		case StartLevel:
			value = (ValueByte) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.StartLevel.ordinal());
			if (value != null) {
				value.onValueRefreshed(((ValueByte) val).getValue());
			}
			res = true;
			break;
		case Duration:
			value = (ValueByte) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Duration.ordinal());
			if (value != null) {
				value.onValueRefreshed(((ValueByte) val).getValue());
			}
			res = true;
			break;
		case Step:
			value = (ValueByte) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Step.ordinal());
			if (value != null) {
				value.onValueRefreshed(((ValueByte) val).getValue());
			}
			res = true;
			break;
		case Inc:
			button = (ValueButton) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Inc.ordinal());
			if (button != null) {
				if (button.isPressed()) {
					res = startLevelChange(instance,
							SwitchMultilevelDirection.Inc.ordinal());
				} else {
					res = stopLevelChange(instance);
				}
			}
			break;
		case Dec:
			button = (ValueButton) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Dec.ordinal());
			if (button != null) {
				if (button.isPressed()) {
					res = startLevelChange(instance,
							SwitchMultilevelDirection.Dec.ordinal());
				} else {
					res = stopLevelChange(instance);
				}
			}
			break;
		}

		return res;
	}

	public boolean setLevel(byte instance, byte level) {
		Msg msg = new Msg(node.getNodeId(), Defs.REQUEST,
				Defs.FUNC_ID_ZW_SEND_DATA, true);
		msg.setInstance(this, instance);
		msg.append(node.getNodeId());

		ValueByte durationValue = (ValueByte) getValue((byte) instance,
				(byte) SwitchMultilevelIndex.Duration.ordinal());
		if (durationValue != null) {
			byte duration = durationValue.getValue();
			msg.append((byte) 4);
			msg.append(COMMAND_CLASS_ID);
			msg.append(SWITCH_MULTILEVEL_CMD_SET);
			msg.append(level);
			msg.append(duration);
		} else {
			msg.append((byte) 3);
			msg.append(COMMAND_CLASS_ID);
			msg.append(SWITCH_MULTILEVEL_CMD_SET);
			msg.append(level);
		}
		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
		return true;
	}

	public boolean startLevelChange(byte instance, int direct) {
		byte length = 4;
		byte direction = directionParams[direct];

		ValueBool ignoreStartLevel = (ValueBool) getValue((byte) instance,
				(byte) SwitchMultilevelIndex.IgnoreStartLevel.ordinal());
		if (ignoreStartLevel != null && ignoreStartLevel.getValue()) {
			direction |= (byte) 0x20;
		}

		byte startLevel = 0;
		ValueByte startLevelValue = (ValueByte) getValue((byte) instance,
				(byte) SwitchMultilevelIndex.StartLevel.ordinal());
		if (startLevelValue != null) {
			startLevel = startLevelValue.getValue();
		}

		byte duration = 0;
		ValueByte durationValue = (ValueByte) getValue((byte) instance,
				(byte) SwitchMultilevelIndex.Duration.ordinal());
		if (durationValue != null) {
			length = 5;
			duration = durationValue.getValue();
		}

		byte step = 0;
		if (SwitchMultilevelDirection.Inc.ordinal() == direct
				|| SwitchMultilevelDirection.Dec.ordinal() == direct) {
			ValueByte stepValue = (ValueByte) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Step.ordinal());
			if (durationValue != null) {
				length = 6;
				duration = stepValue.getValue();
			}
		}

		Msg msg = new Msg(node.getNodeId(), Defs.REQUEST,
				Defs.FUNC_ID_ZW_SEND_DATA, true);
		msg.setInstance(this, instance);
		msg.append(node.getNodeId());
		msg.append(length);
		msg.append(COMMAND_CLASS_ID);
		msg.append(SWITCH_MULTILEVEL_CMD_START_LEVEL_CHANGE);
		msg.append(direction);
		msg.append(startLevel);

		if (length >= 5) {
			msg.append(duration);
		}
		if (length == 6) {
			msg.append(step);
		}

		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
		return true;
	}

	public boolean stopLevelChange(byte instance) {
		Msg msg = new Msg(node.getNodeId(), Defs.REQUEST,
				Defs.FUNC_ID_ZW_SEND_DATA, true);
		msg.setInstance(this, instance);
		msg.append(node.getNodeId());
		msg.append((byte) 2);
		msg.append(COMMAND_CLASS_ID);
		msg.append(SWITCH_MULTILEVEL_CMD_STOP_LEVEL_CHANGE);
		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
		return true;
	}

	public void createVars(byte instance) {
		if (node != null) {
			switch (getVersion()) {
			case 3: // sengaja jatuh ke 2
				node.getValueManager().createValueByte(ValueGenre.USER,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.Step.ordinal(),
						"Step Size", "", false, false, (byte) 0, (byte) 0);
				node.getValueManager().createValueButton(ValueGenre.USER,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.Inc.ordinal(), "Inc",
						(byte) 0);
				node.getValueManager().createValueButton(ValueGenre.USER,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.Dec.ordinal(), "Dec",
						(byte) 0);
			case 2: // sengaja jatuh ke 1
				node.getValueManager().createValueByte(ValueGenre.SYSTEM,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.Duration.ordinal(),
						"Dimming Duration", "", false, false, (byte) 0xFF,
						(byte) 0);
			case 1:
				node.getValueManager().createValueByte(ValueGenre.USER,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.Level.ordinal(), "Level",
						"", false, false, (byte) 0, (byte) 0);
				node.getValueManager().createValueButton(ValueGenre.USER,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.Bright.ordinal(),
						"Bright", (byte) 0);
				node.getValueManager().createValueButton(ValueGenre.USER,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.Dim.ordinal(), "Dim",
						(byte) 0);
				node.getValueManager()
						.createValueBool(
								ValueGenre.SYSTEM,
								getCommandClassId(),
								instance,
								(byte) SwitchMultilevelIndex.IgnoreStartLevel
										.ordinal(), "Ignore Start Level", "",
								false, false, true, (byte) 0);
				node.getValueManager().createValueByte(ValueGenre.SYSTEM,
						getCommandClassId(), instance,
						(byte) SwitchMultilevelIndex.StartLevel.ordinal(),
						"Start Level", "", false, false, (byte) 0, (byte) 0);
				break;
			}
		}
	}

	public byte getMaxVersion() {
		return 3;
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
		if (SWITCH_MULTILEVEL_CMD_REPORT == data[0]) {
			ValueByte value = (ValueByte) getValue((byte) instance,
					(byte) SwitchMultilevelIndex.Level.ordinal());
			if (value != null) {
				value.onValueRefreshed(data[1]);
			}
			return true;
		}

		if (SWITCH_MULTILEVEL_CMD_SUPPORTED_GET == data[0]) {
			byte switchType1 = (byte) (data[1] & 0x1F);
			byte switchType2 = (byte) (data[2] & 0x1F);

			clearStaticRequest(STATIC_REQUEST_VERSION);

			ValueButton button;
			if (switchType1 != 0) {
				button = (ValueButton) getValue((byte) instance,
						(byte) SwitchMultilevelIndex.Bright.ordinal());
				if (button != null) {
					button.setLabel(switchLabelsPos[SafeCast.toInt(switchType1)]);
				}

				button = (ValueButton) getValue((byte) instance,
						(byte) SwitchMultilevelIndex.Dim.ordinal());
				if (button != null) {
					button.setLabel(switchLabelsNeg[SafeCast.toInt(switchType1)]);
				}
			}

			if (switchType2 != 0) {
				button = (ValueButton) getValue((byte) instance,
						(byte) SwitchMultilevelIndex.Inc.ordinal());
				if (button != null) {
					button.setLabel(switchLabelsPos[SafeCast.toInt(switchType2)]);
				}

				button = (ValueButton) getValue((byte) instance,
						(byte) SwitchMultilevelIndex.Dec.ordinal());
				if (button != null) {
					button.setLabel(switchLabelsNeg[SafeCast.toInt(switchType2)]);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte value) {
		requestValue(0, (byte) 0, instance, QueuePriority.Send);
		if (node != null) {
			WakeUp wu = (WakeUp) node.getCommandClassManager().getCommandClass(
					WakeUp.COMMAND_CLASS_ID);
			if (wu != null && !wu.isAwake()) {
				ValueByte val = (ValueByte) getValue(instance,
						(byte) SwitchMultilevelIndex.Level.ordinal());
				if (val != null) {
					val.onValueRefreshed(value);
				}
			}
		}
	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

}
