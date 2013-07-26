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

public class ValueBool extends Value {

	private Boolean value = false; // the current value
	private Boolean valueCheck = false; // the previous value (used for
	// double-checking spurious value reads)
	private Boolean newValue = false;

	public ValueBool() {
	};

	public ValueBool(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, boolean value, byte pollIntensity) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
		this.value = value;
		this.valueCheck = false;
		this.newValue = false;
	}

	public boolean set(boolean value) {
		this.value = value;
		return set();
	}

	public boolean getValue() {
		return value;
	}

	public boolean setString(String value) {
		if (value.compareToIgnoreCase("true") == 0) {
			return set(true);
		} else if (value.compareToIgnoreCase("false") == 0) {
			return set(false);
		}
		return false;
	}

	public void onValueRefreshed(boolean value) {
		Boolean val = value;

		switch (verifyRefreshedValue(this.value, valueCheck, val, 5)) {
		case 0: // value hasn't changed, nothing to do
			break;
		case 1: // value has changed (not confirmed yet), save _value in
			// m_valueCheck
			this.valueCheck = val.booleanValue();
			break;
		case 2: // value has changed (confirmed), save _value in m_value
			this.value = val.booleanValue();
			callValueChangedListener();
			break;
		case 3: // all three values are different, so wait for next refresh to
			// try again
			break;
		}
	}

}
