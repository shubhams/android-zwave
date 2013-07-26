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
import com.azwave.androidzwave.zwave.values.ValueString;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class Version extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x86;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_VERSION";

	public static final byte VERSION_CMD_GET = (byte) 0x11;
	public static final byte VERSION_CMD_REPORT = (byte) 0x12;
	public static final byte VERSION_CMD_CMDCLASS_GET = (byte) 0x13;
	public static final byte VERSION_CMD_CMDCLASS_REPORT = (byte) 0x14;

	public enum VersionIndex {
		Library, Protocol, Application
	}

	private boolean classGetSupported = true;

	public Version(Node node) {
		super(node);
		setStaticRequest(STATIC_REQUEST_VALUES);
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0
				&& hasStaticRequest(STATIC_REQUEST_VALUES)) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	public boolean requestValue(int requestFlags, byte dummy1, byte instance,
			QueuePriority queue) {
		if (instance != 1) {
			return false;
		}

		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				VERSION_CMD_GET, node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public boolean requestCommandClassVersion(CommandClass cmdClass) {
		if (classGetSupported
				&& cmdClass.hasStaticRequest(STATIC_REQUEST_VALUES)) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.appends(new byte[] { getNodeId(), (byte) 3, COMMAND_CLASS_ID,
					VERSION_CMD_CMDCLASS_GET,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, QueuePriority.Send);
			return true;
		}
		return false;
	}

	public void createVars(byte instance) {
		if (node != null) {
			node.getValueManager().createValueString(ValueGenre.SYSTEM,
					getCommandClassId(), instance,
					(byte) VersionIndex.Library.ordinal(), "Library Version",
					"", true, false, "Unknown", (byte) 0);
			node.getValueManager().createValueString(ValueGenre.SYSTEM,
					getCommandClassId(), instance,
					(byte) VersionIndex.Protocol.ordinal(), "Protocol Version",
					"", true, false, "Unknown", (byte) 0);
			node.getValueManager()
					.createValueString(ValueGenre.SYSTEM, getCommandClassId(),
							instance,
							(byte) VersionIndex.Application.ordinal(),
							"Application Version", "", true, false, "Unknown",
							(byte) 0);
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
		if (node != null) {
			if (VERSION_CMD_REPORT == data[0]) {
				String library = String.format("%d", SafeCast.toInt(data[1]));
				String protocol = String.format("%d.%02d",
						SafeCast.toInt(data[2]), SafeCast.toInt(data[3]));
				String application = String.format("%d.%02d",
						SafeCast.toInt(data[4]), SafeCast.toInt(data[5]));

				clearStaticRequest(STATIC_REQUEST_VALUES);
				ValueString libraryValue = (ValueString) getValue(
						(byte) instance, (byte) VersionIndex.Library.ordinal());
				if (libraryValue != null) {
					libraryValue.onValueRefreshed(library);
				}

				ValueString protocolValue = (ValueString) getValue(
						(byte) instance, (byte) VersionIndex.Protocol.ordinal());
				if (protocolValue != null) {
					protocolValue.onValueRefreshed(protocol);
				}

				ValueString applicationValue = (ValueString) getValue(
						(byte) instance,
						(byte) VersionIndex.Application.ordinal());
				if (applicationValue != null) {
					applicationValue.onValueRefreshed(application);
				}

				return true;
			}

			if (VERSION_CMD_CMDCLASS_REPORT == data[0]) {
				CommandClass cc = node.getCommandClassManager()
						.getCommandClass(data[1]);
				if (cc != null) {
					cc.clearStaticRequest(STATIC_REQUEST_VERSION);
					cc.setVersion(data[2]);
				}
				return true;
			}
		}
		return false;
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
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}
}
