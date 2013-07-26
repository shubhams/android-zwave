package com.azwave.androidzwave.zwave.items;

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

import java.util.Arrays;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.commandclass.CommandClass;
import com.azwave.androidzwave.zwave.commandclass.MultiInstance;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.HexDump;

public class Msg {

	public static final byte MULTI_CHANNEL = 0x01; // Indicate MultiChannel
	// encapsulation
	public static final byte MULTI_INSTANCE = 0x02; // Indicate MultiInstance
	// encapsulation

	private byte nodeId = 0;
	private byte type = 0;
	private byte function = 0;
	private boolean callbackRequired = false;
	private boolean replyRequired = true;
	private byte expectedReply = 0;
	private byte expectedCommandClass = 0;
	private byte callbackId = 0;

	private byte[] buffer = new byte[Defs.MAX_TOTAL_NODES];
	private byte length = 0;

	private boolean isFinal = false;
	private byte sendAttempts = 0;
	private byte maxSendAttempts = Defs.MAX_MAX_TRIES;

	private byte instance = 1;
	private byte flags = 0;
	private byte endPoint = 0;

	private static byte nextCallbackId = 1;

	public Msg() {
	}

	public Msg(byte nodeId, byte type, byte function, boolean callbackRequired) {
		this(nodeId, type, function, callbackRequired, true);
	}

	public Msg(byte nodeId, byte type, byte function, boolean callbackRequired,
			boolean replyRequired) {
		this(nodeId, type, function, callbackRequired, replyRequired, (byte) 0);
	}

	public Msg(byte nodeId, byte type, byte function, boolean callbackRequired,
			boolean replyRequired, byte expectedReply) {
		this(nodeId, type, function, callbackRequired, replyRequired,
				expectedReply, (byte) 0);
	}

	public Msg(byte nodeId, byte type, byte function, boolean callbackRequired,
			boolean replyRequired, byte expectedReply, byte expectedCommandClass) {
		this.nodeId = nodeId;
		this.type = type;
		this.function = function;
		this.callbackRequired = callbackRequired;
		this.replyRequired = replyRequired;
		this.expectedReply = (replyRequired) ? function : 0;
		this.expectedCommandClass = expectedCommandClass;

		buffer[0] = Defs.SOF;
		buffer[1] = 0;
		buffer[2] = type;
		buffer[3] = function;
		length = 4;

	}

	public void setInstance(CommandClass cc, byte newInstance) {
		Node node = cc.getNode();
		if (node != null) {
			MultiInstance micc = (MultiInstance) node.getCommandClassManager()
					.getCommandClass(MultiInstance.COMMAND_CLASS_ID);
			instance = newInstance;
			if (micc != null) {
				if (micc.getVersion() > 1) {
					endPoint = cc.getEndPoint(newInstance);
					if (endPoint != 0) {
						flags |= MULTI_CHANNEL;
						expectedCommandClass = MultiInstance.COMMAND_CLASS_ID;
					}
				} else if (instance > 1) {
					flags |= MULTI_INSTANCE;
					expectedCommandClass = MultiInstance.COMMAND_CLASS_ID;
				}
			}
		}
	}

	public void updateCallBackId() {
		if (callbackRequired) {
			buffer[length - 2] = nextCallbackId;
			callbackId = nextCallbackId++;

			byte checksum = (byte) 0xFF;
			for (int i = 1; i < length - 1; ++i) {
				checksum ^= buffer[i];
			}
			buffer[length - 1] = checksum;
		}
	}

	public byte[] toArray() {
		if (length > 1) {
			buildArray();
		}
		return Arrays.copyOf(buffer, length);
	}

	public void multiEncap() {
		if (buffer[3] != Defs.FUNC_ID_ZW_SEND_DATA) {
			return;
		}
		if ((flags & MULTI_CHANNEL) != 0) {
			for (int i = length - 1; i >= 6; --i) {
				buffer[i + 4] = buffer[i];
			}
			buffer[5] += 4;
			buffer[6] = MultiInstance.COMMAND_CLASS_ID;
			buffer[7] = MultiInstance.MULTI_CHANNEL_CMD_ENCAP;
			buffer[8] = 1;
			buffer[9] = endPoint;
			length += 4;
		} else {
			for (int i = length - 1; i >= 6; --i) {
				buffer[i + 3] = buffer[i];
			}
			buffer[5] += 3;
			buffer[6] = MultiInstance.COMMAND_CLASS_ID;
			buffer[7] = MultiInstance.MULTI_INSTANCE_CMD_ENCAP;
			buffer[8] = instance;
			length += 3;
		}
	}

	private void buildArray() {
		if (isFinal) {
			return;
		}

		if ((flags & (MULTI_CHANNEL | MULTI_INSTANCE)) != 0) {
			multiEncap();
		}

		if (callbackRequired) {
			buffer[1] = length;
			if (0 == nextCallbackId) {
				nextCallbackId = 1;
			}
			append(nextCallbackId);
			callbackId = nextCallbackId++;
		} else {
			buffer[1] = (byte) (length - 1);
		}

		byte checksum = (byte) 0xFF;
		for (int i = 1; i < length; ++i) {
			checksum ^= buffer[i];
		}
		append(checksum);

		isFinal = true;
	}

	@Override
	public String toString() {
		return HexDump.dumpHexString(toArray());
	}

	public void appends(byte[] array) {
		for (byte item : array)
			append(item);
	}

	public void append(byte data) {
		buffer[length++] = data;
	}

	public byte getNodeId() {
		return nodeId;
	}

	public int getLength() {
		return length;
	}

	public byte getType() {
		return type;
	}

	public byte getFunction() {
		return function;
	}

	public byte getExpectedReply() {
		return expectedReply;
	}

	public byte getExpectedCommandClass() {
		return expectedCommandClass;
	}

	public byte getCallbackId() {
		return callbackId;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public boolean isCallbackRequired() {
		return callbackRequired;
	}

	public boolean isReplyRequired() {
		return replyRequired;
	}

	public boolean isNoOperation() {
		return (length == 11) && (buffer[3] == 0x13) && (buffer[6] == 0x00)
				&& (buffer[7] == 0x00);
	}

	public boolean isWakeUpNoMoreInformationCommand() {
		return (length == 11) && (buffer[3] == 0x13) && (buffer[6] == 0x84)
				&& (buffer[7] == 0x08);
	}

	public byte getSendAttempts() {
		return sendAttempts;
	}

	public void setSendAttempts(byte count) {
		sendAttempts = count;
	}

	public byte getMaxSendAttempts() {
		return maxSendAttempts;
	}

	public void setMaxSendAttempts(byte count) {
		if (count < Defs.MAX_MAX_TRIES)
			maxSendAttempts = count;
	}
	
	/*
	 * Static function inspired from Domotics Dog2 (Simone Pecchenino)
	 */
	public static Msg createACK() {
		Msg msg = new Msg();
		msg.append(Defs.ACK);
		return msg;
	}

	public static Msg createNAK() {
		Msg msg = new Msg();
		msg.append(Defs.NAK);
		return msg;
	}

	public static Msg createCAN() {
		Msg msg = new Msg();
		msg.append(Defs.CAN);
		return msg;
	}
	
	public static Msg createZWaveGetVersion(byte nodeId) {
		return new Msg(nodeId, Defs.REQUEST, Defs.FUNC_ID_ZW_GET_VERSION, false);
	}

	public static Msg createZWaveMemoryGetId(byte nodeId) {
		return new Msg(nodeId, Defs.REQUEST, Defs.FUNC_ID_ZW_MEMORY_GET_ID,
				false);
	}

	public static Msg createZWaveGetControllerCapabilities(byte nodeId) {
		return new Msg(nodeId, Defs.REQUEST,
				Defs.FUNC_ID_ZW_GET_CONTROLLER_CAPABILITIES, false);
	}

	public static Msg createZWaveSerialAPIGetCapabilities(byte nodeId) {
		return new Msg(nodeId, Defs.REQUEST,
				Defs.FUNC_ID_SERIAL_API_GET_CAPABILITIES, false);
	}

	public static Msg createZWaveGetSUCNodeId(byte nodeId) {
		return new Msg(nodeId, Defs.REQUEST, Defs.FUNC_ID_ZW_GET_SUC_NODE_ID,
				false);
	}

	public static Msg createZWaveSetDefault(byte nodeId) {
		return new Msg(nodeId, Defs.REQUEST, Defs.FUNC_ID_ZW_SET_DEFAULT, false);
	}

	public static Msg createZWaveApplicationCommandHandler(byte nodeId,
			byte commandClassId) {
		return new Msg(nodeId, Defs.REQUEST, Defs.FUNC_ID_ZW_SEND_DATA, true,
				true, Defs.FUNC_ID_APPLICATION_COMMAND_HANDLER, commandClassId);
	}

}
