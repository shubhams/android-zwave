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

import com.azwave.androidzwave.zwave.utils.SafeCast;

public class ValueByte extends Value {

	private Byte value = 0; // the current value
	private Byte valueCheck = 0; // the previous value (used for double-checking
	// spurious value reads)
	private Byte newValue = 0;

	public ValueByte(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, byte value, byte pollIntensity) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
		this.value = value;
		this.valueCheck = 0;
		this.newValue = 0;

		min = 0;
		max = 255;
	}

	public ValueByte() {
		min = 0;
		max = 255;
	}

	public byte getValue() {
		return value;
	}

	public boolean set(byte value) {
		this.value = value;
		return set();
	}

	public boolean setString(String value) {
		int val = Integer.decode(value);
		return val < 256 ? set((byte) val) : false;
	}

	@Override
	public String toString() {
		return getString();
	}

	public String getString() {
		return String.valueOf(SafeCast.toInt(getValue()));
	}

	public void onValueRefreshed(byte value) {
		Byte val = value;

		switch (verifyRefreshedValue(this.value, this.valueCheck, val, 4)) {
		case 0: // value hasn't changed, nothing to do
			break;
		case 1: // value has changed (not confirmed yet), save _value in
			// m_valueCheck
			// isValueChanged = true;
			this.valueCheck = val.byteValue();
			break;
		case 2: // value has changed (confirmed), save _value in m_value
			this.value = val.byteValue();
			callValueChangedListener();
			break;
		case 3: // all three values are different, so wait for next refresh to
			// try again
			break;
		}
	}

}
