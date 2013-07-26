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

public class ValueDecimal extends Value {
	private String value = ""; // the current value
	private String valueCheck = ""; // the previous value (used for
	// double-checking spurious value reads)
	private String newValue = ""; // a new value to be set on the appropriate
	// device

	private byte precision = 0;

	public ValueDecimal() {
	}

	public ValueDecimal(ValueId id, String label, String units,
			boolean readOnly, boolean writeOnly, String value,
			byte pollIntensity) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
		this.value = value;
		this.valueCheck = "";
		this.newValue = "";
		this.precision = 0;
	}

	public boolean set(String value) {
		this.value = value;
		boolean ret = super.set();

		return ret;
	}

	public void onValueRefreshed(String val) {
		switch (verifyRefreshedValue(value, valueCheck, val, 1)) {
		case 0: // value hasn't changed, nothing to do
			break;
		case 1: // value has changed (not confirmed yet), save _value in
			// m_valueCheck
			this.valueCheck = new String(val);
			break;
		case 2: // value has changed (confirmed), save _value in m_value
			this.value = new String(val);
			callValueChangedListener();
			break;
		case 3: // all three values are different, so wait for next refresh to
			// try again
			break;
		}
	}

	public String getValue() {
		return value;
	}

	public void setPrecision(byte precision) {
		this.precision = precision;
	}

	public byte getPrecision() {
		return precision;
	}
}
