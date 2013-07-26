package com.azwave.androidzwave.zwave.values;

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
import java.util.concurrent.ConcurrentHashMap;

import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;

public class ValueManager extends ConcurrentHashMap<Integer, Value> {

	private static final long serialVersionUID = 4143815574944962013L;
	private Node node;

	public ValueManager(Node node) {
		super();
		this.node = node;
	}

	public boolean addValue(Value value) {
		if (value == null) {
			return false;
		}

		int key = value.getId().getValueStoreKey();
		if (this.containsKey(key)) {
			return false;
		}

		this.put(key, value);
		return true;
	}

	public boolean removeValue(byte commandClassId, byte instance,
			byte valueIndex) {
		return removeValue(ValueId.getValueStoreKey(commandClassId, instance,
				valueIndex));
	}

	public boolean removeValue(int key) {
		if (this.containsKey(key)) {
			this.remove(key);
			return true;
		}
		return false;
	}

	public void removeCommandClassValues(byte cmdClassId) {
		for (Entry<Integer, Value> entry : this.entrySet()) {
			Value val = entry.getValue();
			ValueId valId = val.getId();

			if (cmdClassId == valId.getCommandClassId()) {
				this.remove(entry.getKey());
			}
		}
	}

	public Value getValue(ValueId id) {
		return getValue(id.getValueStoreKey());
	}

	public Value getValue(int key) {
		return this.get(key);
	}

	public Value getValue(byte commandClassId, byte instance, byte valueIndex) {
		return getValue(ValueId.getValueStoreKey(commandClassId, instance,
				valueIndex));
	}

	// -----------------------------------------------------------------------------------------
	// Create Value Method
	// -----------------------------------------------------------------------------------------
	public boolean createValueByte(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, byte value, byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.BYTE);
		ValueByte val = new ValueByte(id, label, units, readOnly, writeOnly,
				value, pollIntensity);
		return addValue(val);
	}

	public boolean createValueInt(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, int value, byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.INT);
		ValueInt val = new ValueInt(id, label, units, readOnly, writeOnly,
				value, pollIntensity);
		return addValue(val);
	}

	public boolean createValueShort(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, short value, byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.SHORT);
		ValueShort val = new ValueShort(id, label, units, readOnly, writeOnly,
				value, pollIntensity);
		return addValue(val);
	}

	public boolean createValueString(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, String value,
			byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.STRING);
		ValueString val = new ValueString(id, label, units, readOnly,
				writeOnly, value, pollIntensity);
		return addValue(val);
	}

	public boolean createValueDecimal(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, String value,
			byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.DECIMAL);
		ValueDecimal val = new ValueDecimal(id, label, units, readOnly,
				writeOnly, value, pollIntensity);
		return addValue(val);
	}

	public boolean createValueBool(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, boolean value,
			byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.BOOL);
		ValueBool val = new ValueBool(id, label, units, readOnly, writeOnly,
				value, pollIntensity);
		return addValue(val);
	}

	public boolean createValueList(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, byte size,
			ArrayList<ValueListItem> items, int value, byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.LIST);
		ValueList val = new ValueList(id, label, units, readOnly, writeOnly,
				items, value, pollIntensity, size);
		return addValue(val);
	}

	public boolean createValueRaw(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, byte[] value, byte length,
			byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.RAW);
		ValueRaw val = new ValueRaw(id, label, units, readOnly, writeOnly,
				value, length, pollIntensity);
		return addValue(val);
	}

	public boolean createValueButton(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.BUTTON);
		ValueButton val = new ValueButton(id, label, pollIntensity);
		return addValue(val);
	}

	public boolean createValueSchedule(ValueGenre genre, byte cmdClassId,
			byte instance, byte index, String label, String units,
			boolean readOnly, boolean writeOnly, byte pollIntensity) {
		ValueId id = new ValueId(node, genre, cmdClassId, instance, index,
				ValueType.SCHEDULE);
		ValueSchedule val = new ValueSchedule(id, label, units, readOnly,
				writeOnly, pollIntensity);
		return addValue(val);
	}

}
