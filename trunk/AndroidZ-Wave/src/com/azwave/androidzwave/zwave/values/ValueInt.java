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

public class ValueInt extends Value {
	private Integer value = 0;
	private Integer valueCheck = 0;
	private Integer newValue = 0;

	public ValueInt(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, int value, byte pollIntensity) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
		this.value = value;
		this.valueCheck = 0;
		this.newValue = 0;

		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
	}

	public ValueInt() {
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
	}

	@Override
	public String toString() {
		return getString();
	}

	public String getString() {
		return String.valueOf(getValue());
	}

	public boolean setString(String value) {
		return set(Integer.parseInt(value));
	}

	public int getValue() {
		return value;
	}

	public boolean set(int value) {
		this.value = value;
		boolean ret = super.set();

		return ret;
	}

	public void onValueRefreshed(int value) {
		Integer val = value;
		switch (verifyRefreshedValue(this.value, this.valueCheck, val, 3)) {
		case 0: // value hasn't changed, nothing to do
			break;
		case 1: // value has changed (not confirmed yet), save _value in
			// m_valueCheck
			this.valueCheck = val.intValue();
			break;
		case 2: // value has changed (confirmed), save _value in m_value
			this.value = val.intValue();
			callValueChangedListener();
			break;
		case 3: // all three values are different, so wait for next refresh to
			// try again
			break;
		}
	}
}
