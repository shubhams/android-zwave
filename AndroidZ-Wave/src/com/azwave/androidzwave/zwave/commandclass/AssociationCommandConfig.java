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
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueBool;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueShort;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class AssociationCommandConfig extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x9B;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_ASSOCIATION_COMMAND_CONFIGURATION";

	public static final byte ASSOC_CMD_CONF_CMD_SUPPORTED_RECORD_GET = (byte) 0x01;
	public static final byte ASSOC_CMD_CONF_CMD_SUPPORTED_RECORD_REPORT = (byte) 0x02;
	public static final byte ASSOC_CMD_CONF_CMD_SET = (byte) 0x03;
	public static final byte ASSOC_CMD_CONF_CMD_GET = (byte) 0x04;
	public static final byte ASSOC_CMD_CONF_CMD_REPORT = (byte) 0x05;

	public enum AssociationCommandConfigurationIndex {
		MaxCommandLength, CommandsAreValues, CommandsAreConfigurable, NumFreeCommands, MaxCommands
	}

	public AssociationCommandConfig(Node node) {
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
	public byte getMaxVersion() {
		return 1;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (ASSOC_CMD_CONF_CMD_SUPPORTED_RECORD_REPORT == data[0]) {
			byte maxCommandLength = (byte) (data[1] >> 2);
			boolean commandsAreValues = (data[1] & (byte) 0x02) != 0;
			boolean commandsAreConfigurable = (data[1] & (byte) 0x01) != 0;
			short numFreeCommands = (short) (SafeCast.toShort(data[2]) << 16 | SafeCast
					.toShort(data[3]));
			short maxCommands = (short) (SafeCast.toShort(data[4]) << 16 | SafeCast
					.toShort(data[5]));

			ValueBool valueBool;
			ValueByte valueByte;
			ValueShort valueShort;

			if ((valueByte = (ValueByte) getValue(
					instance,
					(byte) AssociationCommandConfigurationIndex.MaxCommandLength
							.ordinal())) != null) {
				valueByte.onValueRefreshed(maxCommandLength);
			}

			if ((valueBool = (ValueBool) getValue(
					instance,
					(byte) AssociationCommandConfigurationIndex.CommandsAreValues
							.ordinal())) != null) {
				valueBool.onValueRefreshed(commandsAreValues);
			}

			if ((valueBool = (ValueBool) getValue(
					instance,
					(byte) AssociationCommandConfigurationIndex.CommandsAreConfigurable
							.ordinal())) != null) {
				valueBool.onValueRefreshed(commandsAreConfigurable);
			}

			if ((valueShort = (ValueShort) getValue(instance,
					(byte) AssociationCommandConfigurationIndex.NumFreeCommands
							.ordinal())) != null) {
				valueShort.onValueRefreshed(numFreeCommands);
			}

			if ((valueShort = (ValueShort) getValue(instance,
					(byte) AssociationCommandConfigurationIndex.MaxCommands
							.ordinal())) != null) {
				valueShort.onValueRefreshed(maxCommands);
			}

			return true;
		}

		if (ASSOC_CMD_CONF_CMD_REPORT == data[0]) {
			byte groupIdx = data[1];
			byte nodeIdx = data[2];
			boolean firstReports = (data[3] & (byte) 0x80) != 0; // True if this
			// is the first
			// message
			// containing
			// commands for
			// this group
			// and node.
			byte numReports = (byte) (data[3] & (byte) 0x0f);

			if (node != null) {
				// TODO: Group
				/*
				 * Group* group = node->GetGroup( groupIdx ); if( NULL == group
				 * ) { if( firstReports ) { // This is the first report message,
				 * so we should clear any existing command data
				 * group->ClearCommands( nodeIdx ); }
				 * 
				 * uint8 const* start = &_data[4];
				 * 
				 * for( uint8 i=0; i<numReports; ++i ) { uint8 length =
				 * start[0]; group->AddCommand( nodeIdx, length, start+1 );
				 * start += length; } }
				 */
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_SESSION) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		if (instance != 0) {
			return false;
		}
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				ASSOC_CMD_CONF_CMD_SUPPORTED_RECORD_GET,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public void requestCommand(byte groupIdx, byte nodeId) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.appends(new byte[] { getNodeId(), 4, COMMAND_CLASS_ID,
				ASSOC_CMD_CONF_CMD_GET, groupIdx, nodeId,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
	}

	public void setCommand(byte groupIdx, byte nodeId, byte length, byte[] data) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.append(getNodeId());
		msg.append((byte) (SafeCast.toInt(length) + 5));
		msg.append(COMMAND_CLASS_ID);
		msg.append(ASSOC_CMD_CONF_CMD_SET);
		msg.append(groupIdx);
		msg.append(nodeId);
		msg.append(length);

		for (int i = 0; i < length; i++) {
			msg.append(data[i]);
		}

		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance) {
		if (node != null) {
			node.getValueManager()
					.createValueByte(
							ValueGenre.SYSTEM,
							getCommandClassId(),
							mInstance,
							(byte) AssociationCommandConfigurationIndex.MaxCommandLength
									.ordinal(), "Max Command Length", "", true,
							false, (byte) 0, (byte) 0);
			node.getValueManager()
					.createValueBool(
							ValueGenre.SYSTEM,
							getCommandClassId(),
							mInstance,
							(byte) AssociationCommandConfigurationIndex.CommandsAreValues
									.ordinal(), "Commands are Values", "",
							true, false, false, (byte) 0);
			node.getValueManager()
					.createValueBool(
							ValueGenre.SYSTEM,
							getCommandClassId(),
							mInstance,
							(byte) AssociationCommandConfigurationIndex.CommandsAreConfigurable
									.ordinal(), "Commands are Configurable",
							"", true, false, false, (byte) 0);
			node.getValueManager().createValueShort(
					ValueGenre.SYSTEM,
					getCommandClassId(),
					mInstance,
					(byte) AssociationCommandConfigurationIndex.NumFreeCommands
							.ordinal(), "Free Commands", "", true, false,
					(short) 0, (byte) 0);
			node.getValueManager().createValueShort(
					ValueGenre.SYSTEM,
					getCommandClassId(),
					mInstance,
					(byte) AssociationCommandConfigurationIndex.MaxCommands
							.ordinal(), "Max Commands", "", true, false,
					(short) 0, (byte) 0);
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
