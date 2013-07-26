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

public class ValueShort extends Value {
	private Short value = 0;
	private Short valueCheck = 0;
	private Short newValue = 0;

	public ValueShort(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, short value, byte pollIntensity) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
		this.value = value;
		this.valueCheck = 0;
		this.newValue = 0;

		min = Short.MIN_VALUE;
		max = Short.MAX_VALUE;
	}

	public ValueShort() {
		min = Short.MIN_VALUE;
		max = Short.MAX_VALUE;
	}

	@Override
	public String toString() {
		return getString();
	}

	public String getString() {
		return String.valueOf(getValue());
	}

	public boolean setString(String value) {
		return set(Short.parseShort(value));
	}

	public boolean set(short value) {
		this.value = value;
		boolean ret = super.set();

		return ret;
	}

	public short getValue() {
		return value;
	}

	public void onValueRefreshed(short value) {
		Short val = value;
		switch (verifyRefreshedValue(this.value, this.valueCheck, val, 2)) {
		case 0: // value hasn't changed, nothing to do
			break;
		case 1: // value has changed (not confirmed yet), save _value in
			// m_valueCheck
			this.valueCheck = val.shortValue();
			break;
		case 2: // value has changed (confirmed), save _value in m_value
			this.value = val.shortValue();
			callValueChangedListener();
			break;
		case 3: // all three values are different, so wait for next refresh to
			// try again
			break;
		}
	}
}
