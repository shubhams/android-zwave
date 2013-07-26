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

public class ValueList extends Value {

	private ArrayList<ValueListItem> items = new ArrayList<ValueListItem>();
	private Integer valueIndex = 0; // the current index in the m_items vector
	private Integer valueIndexCheck = 0; // the previous index in the m_items
	// vector (used for double-checking
	// spurious value reads)
	private Integer newValueIndex = 0; // a new value to be set on the
	// appropriate device
	private byte size = 0;

	public ValueList() {
	}

	public ValueList(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, ArrayList<ValueListItem> item, int valueIdx,
			byte pollIntensity) {
		this(id, label, units, readOnly, writeOnly, item, valueIdx,
				pollIntensity, (byte) 4);
	}

	public ValueList(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, ArrayList<ValueListItem> item, int valueIdx,
			byte pollIntensity, byte size) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
		this.items = item;
		this.valueIndex = valueIdx;
		this.valueIndexCheck = 0;
		this.newValueIndex = 0;
		this.size = size;
	}

	public boolean setByValue(int value) {
		this.valueIndex = value;
		return set();
	}

	public boolean setByLabel(String label) {
		int index = getItemIndexByLabel(label);
		return index < 0 ? false : setByValue(index);
	}

	public void onValueRefreshed(int value) {
		Integer index = getItemIndexByValue(value);
		if (index < 0) {
			return;
		}

		switch (verifyRefreshedValue(valueIndex, valueIndexCheck, index, 3)) {
		case 0: // value hasn't changed, nothing to do
			break;
		case 1: // value has changed (not confirmed yet), save _value in
			// m_valueCheck
			// isValueChanged = true;
			valueIndexCheck = index.intValue();
			break;
		case 2: // value has changed (confirmed), save _value in m_value
			valueIndex = index.intValue();
			callValueChangedListener();
			break;
		case 3: // all three values are different, so wait for next refresh to
			// try again
			break;
		}
	}

	public int getItemIndexByLabel(String label) {
		for (int i = 0; i < items.size(); ++i) {
			if (label.compareTo(items.get(i).getLabel()) == 0) {
				return i;
			}
		}
		return -1;
	}

	public int getItemIndexByValue(int value) {
		for (int i = 0; i < items.size(); ++i) {
			if (value == items.get(i).getValue()) {
				return i;
			}
		}
		return -1;
	}

	public boolean getItemLabels(ArrayList<String> item) {
		if (item != null) {
			for (ValueListItem i : items) {
				item.add(i.getLabel());
			}
			return true;
		} else {
			return false;
		}
	}

	public String getString() {
		return getItem().getLabel();
	}

	public boolean setString(String value) {
		return setByLabel(value);
	}

	public ValueListItem getItem() {
		return items.get(valueIndex);
	}

	public ValueListItem getNewItem() {
		return items.get(newValueIndex);
	}

	public byte getSize() {
		return size;
	}

	public void setSize(byte size) {
		this.size = size;
	}

}
