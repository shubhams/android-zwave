package com.azwave.androidzwave.zwave.values;

//-----------------------------------------------------------------------------
//	Copyright (c) 2010 Mal Lansell <openzwave@lansell.org>
//
//	SOFTWARE NOTICE AND LICENSE
//
//	This file is part of OpenZWave.
//
//	OpenZWave is free software: you can redistribute it and/or modify
//	it under the terms of the GNU Lesser General Public License as published
//	by the Free Software Foundation, either version 3 of the License,
//	or (at your option) any later version.
//
//	OpenZWave is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU Lesser General Public License for more details.
//
//	You should have received a copy of the GNU Lesser General Public License
//	along with OpenZWave.  If not, see <http://www.gnu.org/licenses/>.
//
//-----------------------------------------------------------------------------
//
// Ported to Java by: Peradnya Dinata <peradnya@gmail.com>
//
//-----------------------------------------------------------------------------

import java.util.Arrays;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.commandclass.CommandClass;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;

public abstract class Value {

	protected ValueChangedListener listener = null;

	protected ValueId valueId = null;

	protected String label = "";
	protected String units = "";
	protected String help = "";

	protected boolean readOnly = false;
	protected boolean writeOnly = false;
	protected boolean isSet = false;

	protected byte affectsLength = 0;
	protected byte[] affects = null;
	protected boolean affectsAll = false;

	protected boolean checkChange = false;
	protected byte pollIntensity = 0;

	protected int max = 0;
	protected int min = 0;

	protected boolean verifyChanges = false;

	public Value() {
	}

	public Value(ValueId id, String label, String units, boolean readOnly,
			boolean writeOnly, boolean isSet, byte pollIntensity) {
		valueId = id;
		this.label = label;
		this.units = units;
		this.readOnly = readOnly;
		this.writeOnly = writeOnly;
		this.isSet = isSet;
		this.pollIntensity = pollIntensity;
	}

	public ValueId getId() {
		return valueId;
	}

	public void setId(ValueId id) {
		this.valueId = id;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isWriteOnly() {
		return writeOnly;
	}

	public void setReadOnly(boolean set) {
		this.readOnly = set;
	}

	public void setWriteOnly(boolean set) {
		this.writeOnly = set;
	}

	public boolean isSet() {
		return isSet;
	}

	public boolean isPolled() {
		return pollIntensity != 0;
	}

	public Node getNode() {
		return valueId.getNode();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public byte getPollIntensity() {
		return pollIntensity;
	}

	public void setPollIntensity(byte intensity) {
		pollIntensity = intensity;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public String getString() {
		return "";
	}

	public boolean setString(String value) {
		return false;
	}

	public byte getAffectsLength() {
		return affectsLength;
	}

	public void setAffectsLength(byte length) {
		this.affectsLength = length;
	}

	public byte[] getAffects() {
		return affects;
	}

	public void setAffects(byte[] affects) {
		this.affects = affects;
	}

	public boolean isAffectsAll() {
		return affectsAll;
	}

	public void setAffectsAll(boolean affectsAll) {
		this.affectsAll = affectsAll;
	}

	public boolean isVerifyChanges() {
		return verifyChanges;
	}

	public void setVerifyChanges(boolean verify) {
		this.verifyChanges = verify;
	}

	public boolean set() {
		if (readOnly) {
			return false;
		}

		boolean res = false;
		Node node = getNode();
		if (node != null) {
			CommandClass cc = node.getCommandClassManager().getCommandClass(
					valueId.getCommandClassId());
			if (cc != null) {
				res = cc.setValue(this);
				cc.requestValue(0, valueId.getIndex(), valueId.getInstance(),
						QueuePriority.Send);
			}

			if (writeOnly) {
				if (affectsAll) {
					node.requestAllConfiguration(0);
				} else if (affectsLength > 0) {
					for (int i = 0; i < affectsLength; i++) {
						node.requestConfigurationParam(affects[i]);
					}
				}
			}
		}

		return res;
	}

	public static String getValueGenreName(ValueGenre genre) {
		return genre.name();
	}

	public static ValueGenre getValueGenre(String name) {
		if (name != null && name.length() > 0) {
			return ValueGenre.valueOf(name.toUpperCase());
		}
		return ValueGenre.SYSTEM;
	}

	public static String getValueTypeName(ValueType type) {
		return type.name();
	}

	public static ValueType getValueType(String name) {
		if (name != null && name.length() > 0) {
			return ValueType.valueOf(name.toUpperCase());
		}
		return ValueType.BOOL;
	}

	protected boolean isCheckingChange() {
		return checkChange;
	}

	protected void setCheckingChange(boolean check) {
		checkChange = check;
	}

	protected void onValueRefreshed() {
		if (writeOnly) {
			return;
		}
		isSet = true;
	}

	protected void onValueChanged() {
		if (writeOnly) {
			return;
		}
		isSet = true;
	}

	// -----------------------------------------------------------------------------------------
	// Listener Method
	// -----------------------------------------------------------------------------------------
	public void setValueChangedListener(ValueChangedListener listener) {
		this.listener = listener;
	}

	public ValueChangedListener getValueChangedListener() {
		return listener;
	}

	protected void callValueChangedListener() {
		if (listener != null) {
			listener.onValueChanged(this);
		}
		;
	}

	// -----------------------------------------------------------------------------------------
	// Verify Refreshed Value Method
	// -----------------------------------------------------------------------------------------
	protected int verifyRefreshedValue(Object originalValue, Object checkValue,
			Object newValue, int type) {
		return verifyRefreshedValue(originalValue, checkValue, newValue, type,
				0);
	}

	protected int verifyRefreshedValue(Object originalValue, Object checkValue,
			Object newValue, int type, int length) {
		if (isSet) {
			onValueChanged();
			return 2;
		} else {
			// for logs
		}

		if (!verifyChanges) {
			onValueChanged();
			return 2;
		}

		boolean originalEqual = false;
		switch (type) {
		case 1: // string
			originalEqual = ((String) originalValue)
					.compareTo((String) newValue) == 0;
			break;
		case 2: // short
			originalEqual = ((Short) originalValue).compareTo((Short) newValue) == 0;
			break;
		case 3: // int
			originalEqual = ((Integer) originalValue)
					.compareTo((Integer) newValue) == 0;
			break;
		case 4: // byte
			originalEqual = ((Byte) originalValue).compareTo((Byte) newValue) == 0;
			break;
		case 5: // boolean
			originalEqual = ((Boolean) originalValue)
					.compareTo((Boolean) newValue) == 0;
			break;
		case 6: // raw (as byte[])
			originalEqual = Arrays.equals((byte[]) originalValue,
					(byte[]) newValue);
			break;
		}

		if (!checkChange) {
			if (originalEqual) {
				onValueRefreshed();
				return 0;
			}
			setCheckingChange(true);
			// Manager::Get()->RefreshValue( GetID() );
			return 1;
		} else {
			boolean checkEqual = false;
			switch (type) {
			case 1: // string
				checkEqual = ((String) checkValue).compareTo((String) newValue) == 0;
				break;
			case 2: // short
				checkEqual = ((Short) checkValue).compareTo((Short) newValue) == 0;
				break;
			case 3: // int
				checkEqual = ((Integer) checkValue)
						.compareTo((Integer) newValue) == 0;
				break;
			case 4: // byte
				checkEqual = ((Byte) checkValue).compareTo((Byte) newValue) == 0;
				break;
			case 5: // boolean
				checkEqual = ((Boolean) checkValue)
						.compareTo((Boolean) newValue) == 0;
				break;
			case 6: // raw (as byte[])
				checkEqual = Arrays.equals((byte[]) checkValue,
						(byte[]) newValue);
				break;
			}

			if (checkEqual) {
				setCheckingChange(false);
				onValueChanged();
				return 2;
			}

			if (originalEqual) {
				setCheckingChange(false);
				onValueRefreshed();
				return 0;
			}

			setCheckingChange(true);
			// Manager::Get()->RefreshValue( GetID() );
			return 1;
		}
	}

}
