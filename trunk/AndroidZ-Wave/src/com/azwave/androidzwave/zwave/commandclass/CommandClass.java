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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.azwave.androidzwave.zwave.BitField;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;

public abstract class CommandClass {

	public static final int REQUEST_FLAG_STATIC = 1;
	public static final int REQUEST_FLAG_SESSION = 2;
	public static final int REQUEST_FLAG_DYNAMIC = 4;

	public static final byte STATIC_REQUEST_INSTANCES = 0x01;
	public static final byte STATIC_REQUEST_VALUES = 0x02;
	public static final byte STATIC_REQUEST_VERSION = 0x04;

	protected static final byte SIZE_MASK = (byte) 0x07;
	protected static final byte SCALE_MASK = (byte) 0x18;
	protected static final byte SCALE_SHIFT = (byte) 0x03;
	protected static final byte PRECISION_MASK = (byte) 0xE0;
	protected static final byte PRECISION_SHIFT = (byte) 0x05;

	protected Node node = null;

	protected boolean afterMark = false;
	protected boolean createVars = true;
	protected boolean getSupported = true;

	protected byte version = 1;
	protected byte overridePrecision = -1;
	protected byte staticRequests = 0;

	protected BitField instances = new BitField();
	protected HashMap<Byte, Byte> endPointMap = new HashMap<Byte, Byte>();

	public CommandClass(Node node) {
		this.node = node;
	}

	// -----------------------------------------------------------------------------------------
	// Class Info Methods
	// -----------------------------------------------------------------------------------------
	public Node getNode() {
		return node;
	}

	public boolean handleMsg(byte[] data, int length) {
		return handleMsg(data, length, (byte) 1);
	}

	public byte getNodeId() {
		return node.getNodeId();
	}

	public int getHomeId() {
		return node.getHomeId();
	}

	public void setAfterMark() {
		setAfterMark(true);
	}

	public void setAfterMark(boolean mark) {
		this.afterMark = mark;
	}

	public boolean isAfterMark() {
		return afterMark;
	}

	public boolean isCreateVars() {
		return createVars;
	}

	public void setCreateVars(boolean set) {
		this.createVars = set;
	}

	public void setOverridePrecision(byte precision) {
		this.overridePrecision = precision;
	}

	public byte getOverridePrecision() {
		return overridePrecision;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte getVersion() {
		return version;
	}

	public boolean isGetSupported() {
		return getSupported;
	}

	public void setSupported(boolean supported) {
		this.getSupported = supported;
	}

	// -----------------------------------------------------------------------------------------
	// EndPoint Methods
	// -----------------------------------------------------------------------------------------
	public byte getEndPoint(byte instance) {
		return endPointMap.containsKey(instance) ? endPointMap.get(instance)
				: 0;
	}

	public void setEndPoint(byte instance, byte endpoint) {
		endPointMap.put(instance, endpoint);
	}

	// -----------------------------------------------------------------------------------------
	// Values Methods
	// -----------------------------------------------------------------------------------------
	public Value getValue(byte instance, byte index) {
		Value val = null;
		if (node != null) {
			val = node.getValueManager().getValue(getCommandClassId(),
					instance, index);
		}
		return val;
	}

	public boolean removeValue(byte instance, byte index) {
		if (node != null) {
			return node.getValueManager().removeValue(getCommandClassId(),
					instance, index);
		} else {
			return false;
		}
	}

	public String extractValue(byte[] data, byte[] scale, byte[] precision) {
		return extractValue(data, scale, precision, (byte) 1);
	}

	public String extractValue(byte[] data, byte[] scale, byte[] precision,
			byte valueOffset) {
		byte size = (byte) (data[0] & SIZE_MASK);
		byte prec = (byte) ((data[0] & PRECISION_MASK) >> PRECISION_SHIFT);

		if (scale != null && scale.length == 1) {
			scale[0] = (byte) ((data[0] & SCALE_MASK) >> SCALE_SHIFT);
		}

		if (precision != null && precision.length == 1) {
			precision[0] = prec;
		}

		int value = 0;
		for (int i = 0; i < size; ++i) {
			value <<= 8;
			value |= SafeCast.toInt(data[i + SafeCast.toInt(valueOffset)]);
		}

		String res = "";
		if ((data[SafeCast.toInt(valueOffset)] & 0x80) != 0) {
			res = "-";
			if (size == 1) {
				value |= 0xffffff00;
			} else if (size == 2) {
				value |= 0xffff0000;
			}
		}

		if (prec == 0) {
			res = String.format("%d", (long) value);
		} else {
			char[] buf = String.format("%011d", (long) value).toCharArray();
			int decimal = 10 - prec;
			int start = -1;

			for (int i = 0; i < decimal; ++i) {
				buf[i] = buf[i + 1];
				if ((start < 0) && (buf[i] != '0')) {
					start = i;
				}
			}

			if (start < 0) {
				start = decimal - 1;
			}
			buf[decimal] = '.';
			res += String.valueOf(buf);
		}
		return res;
	}

	public void appendValue(Msg msg, String value, byte scale) {
		byte[] precision = { 0 };
		byte[] size = { 0 };
		int val = valueToInteger(value, precision, size);

		msg.append((byte) ((precision[0] << PRECISION_SHIFT)
				| (scale << SCALE_SHIFT) | size[0]));

		int shift = (size[0] - 1) << 3;
		for (int i = size[0]; i > 0; --i, shift -= 8) {
			msg.append((byte) (val >> shift));
		}
	}

	public byte getAppendValueSize(String value) {
		byte[] size = { 0 };
		valueToInteger(value, null, size);
		return size[0];
	}

	public int valueToInteger(String value, byte[] oPrecision, byte[] oSize) {
		int val;
		byte precision;

		int pos = value.indexOf('.');
		if (pos == -1) {
			precision = 0;
			val = Integer.parseInt(value);
		} else {
			precision = (byte) ((value.length() - pos) - 1);
			val = Integer.parseInt(value.substring(0, pos)
					+ value.substring(pos + 1));
		}

		if (overridePrecision > 0) {
			while (precision < overridePrecision) {
				precision++;
				val *= 10;
			}
		}

		if (oPrecision != null && oPrecision.length == 1)
			oPrecision[0] = precision;
		if (oSize != null && oSize.length == 1) {
			oSize[0] = 4;
			if (val < 0) {
				if ((val & 0xffffff80) == 0xffffff80) {
					oSize[0] = 1;
				} else if ((val & 0xffff8000) == 0xffff8000) {
					oSize[0] = 2;
				}
			} else {
				if ((val & 0xffffff00) == 0) {
					oSize[0] = 1;
				} else if ((val & 0xffff0000) == 0) {
					oSize[0] = 2;
				}
			}
		}

		return val;
	}

	public void updateMappedClass(byte instance, byte ccClassId, byte level) {
		if (ccClassId != node.getPrimaryNodeId()) {
			if (node != null) {
				CommandClass cc = node.getCommandClassManager()
						.getCommandClass(ccClassId);
				if (node.getQueryStage() == QueryStage.Complete && cc != null) {
					cc.setValueBasic(instance, level);
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------
	// Instances Methods
	// -----------------------------------------------------------------------------------------
	public BitField getInstances() {
		return instances;
	}

	public void setInstances(byte instances) {
		if (!isAfterMark()) {
			for (int i = 0; i < instances; ++i) {
				setInstance((byte) (i + 1));
			}
		}
	}

	public void setInstance(byte endPoint) {
		if (!instances.isSet(endPoint)) {
			instances.set(endPoint);
			if (isCreateVars()) {
				createVars(endPoint);
			}
		}
	}

	public byte getInstance(byte endPoint) {
		for (Entry<Byte, Byte> entry : endPointMap.entrySet()) {
			if (endPoint == entry.getValue()) {
				return entry.getKey();
			}
		}
		return 0;
	}

	// -----------------------------------------------------------------------------------------
	// Request Methods
	// -----------------------------------------------------------------------------------------
	public boolean hasStaticRequest(byte request) {
		return (staticRequests & request) != 0;
	}

	public void setStaticRequest(byte request) {
		staticRequests |= request;
	}

	public void clearStaticRequest(byte request) {
		staticRequests &= ~request;
	}

	public boolean requestStateForAllInstances(int requestFlags,
			QueuePriority queue) {
		boolean res = false;
		if (createVars && node != null) {
			MultiInstance mi = (MultiInstance) node.getCommandClassManager()
					.getCommandClass(MultiInstance.COMMAND_CLASS_ID);
			if (mi != null) {
				Iterator<Integer> it = instances.iterator();
				while (it.hasNext()) {
					res |= requestState(requestFlags, it.next().byteValue(),
							queue);
				}
			} else {
				res = requestState(requestFlags, (byte) 1, queue);
			}
		}
		return res;
	}

	public abstract byte getCommandClassId();

	public abstract String getCommandClassName();

	public abstract byte getMaxVersion();

	public abstract boolean handleMsg(byte[] data, int length, byte instance);

	public abstract boolean requestState(int requestFlags, byte instance,
			QueuePriority queue);

	public abstract boolean requestValue(int requestFlags, byte index,
			byte instance, QueuePriority queue);

	public abstract void setValueBasic(byte instance, byte level);

	public abstract void createVars(byte mInstance);

	public abstract void createVars(byte mInstance, byte index);

	public abstract boolean setValue(Value value);

}
