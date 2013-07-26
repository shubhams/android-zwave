package com.azwave.androidzwave.zwave.utils;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.items.Msg;

public class SafeCast {

	public static short toShort(byte data) {
		return (short) toInt(data);
	}

	public static int toInt(byte data) {
		return ((int) data) & 0xFF;
	}

	public static int toInt(short data) {
		return ((int) data) & 0xFFFF;
	}

	public static long toLong(byte data) {
		return toLong(toInt(data));
	}

	public static long toLong(short data) {
		return toLong(toInt(data));
	}

	public static long toLong(int data) {
		return ((long) data) & 0xFFFFFFFFL;
	}

	public static byte nodeIdFromMsg(Msg msg) {
		return msg == null ? 0 : msg.getNodeId();
	}

	public static byte nodeIdFromStream(byte[] data) {
		byte nodeId = 0;

		if (data[1] >= 5) {
			switch (data[3]) {
			case Defs.FUNC_ID_APPLICATION_COMMAND_HANDLER:
			case Defs.FUNC_ID_ZW_APPLICATION_UPDATE:
				nodeId = data[5];
				break;
			}
		}

		return nodeId;
	}
}
