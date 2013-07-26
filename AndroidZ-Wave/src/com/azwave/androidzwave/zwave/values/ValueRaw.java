package com.azwave.androidzwave.zwave.values;

//-----------------------------------------------------------------------------
//Copyright (c) 2012 Greg Satz <satz@iranger.com>
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

import com.azwave.androidzwave.zwave.utils.SafeCast;

public class ValueRaw extends Value {

	private byte[] value = null; // the current value
	private byte[] valueCheck = null; // the previous value (used for
	// double-checking spurious value reads)
	private byte[] newValue = null; // a new value to be set on the appropriate
	// device
	private byte valueLength = 0; // fixed length for this instance

	public ValueRaw(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, byte[] value, byte length, byte pollIntensity) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
		this.valueLength = length;
		this.value = new byte[length];

		System.arraycopy(value, 0, this.value, 0, length);

		max = 0;
		min = 0;
	}

	public ValueRaw() {
		max = 0;
		min = 0;
	}

	public String toString() {
		return getString();
	}

	public String getString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < SafeCast.toInt(valueLength); ++i) {
			if (i != 0) {
				builder.append(' ');
			}
			builder.append(String.format("0x%02x", value[i]));
		}
		return builder.toString();
	}

	public boolean setString(String value) {
		String temp[] = value.split("\\s+");
		byte[] val = new byte[temp.length];

		for (int i = 0; i < temp.length; i++) {
			val[i] = (byte) Integer.decode(temp[i]).intValue();
		}

		boolean bRet = false;
		if (temp.length <= valueLength) {
			bRet = set(val, (byte) temp.length);
		}

		return bRet;
	}

	public boolean set(byte[] value, byte length) {
		value = Arrays.copyOf(value, SafeCast.toInt(length));
		valueLength = length;
		boolean ret = super.set();

		return ret;
	}

	public byte[] getValue() {
		return value;
	}

	public byte getLength() {
		return valueLength;
	}

	public void onValueRefreshed(byte[] val, byte length) {
		switch (verifyRefreshedValue(value, valueCheck, val, 6,
				SafeCast.toInt(length))) {
		case 0: // value hasn't changed, nothing to do
			break;
		case 1: // value has changed (not confirmed yet), save _value in
			// m_valueCheck
			valueCheck = Arrays.copyOf(val, SafeCast.toInt(length));
			break;
		case 2: // value has changed (confirmed), save _value in m_value
			value = Arrays.copyOf(val, SafeCast.toInt(length));
			callValueChangedListener();
			break;
		case 3: // all three values are different, so wait for next refresh to
			// try again
			break;
		}
	}

}
