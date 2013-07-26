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

public class ValueSchedule extends Value {

	private SwitchPoint[] switchPoints = new SwitchPoint[9];
	private byte numSwitchPoints = 0;

	public ValueSchedule() {
	}

	public ValueSchedule(ValueId id, String label, String units,
			boolean readOnly, boolean writeOnly, byte pollIntensity) {
		super(id, label, units, readOnly, writeOnly, false, pollIntensity);
	}

	public boolean set() {
		return super.set();
	}

	public void onValueRefreshed() {
		super.onValueRefreshed();
	}

	public boolean setSwitchPoint(byte hours, byte minutes, byte setback) {
		byte insertAt = 0;
		for (int i = 0; i < SafeCast.toInt(numSwitchPoints); ++i) {
			if (switchPoints[i].hours == hours) {
				if (switchPoints[i].minutes == minutes) {
					switchPoints[i].setback = setback;
					return true;
				}
				if (switchPoints[i].minutes > minutes) {
					break;
				}
			} else if (switchPoints[i].hours > hours) {
				break;
			}
			++insertAt;
		}

		if (numSwitchPoints >= 9) {
			return false;
		}

		for (int i = numSwitchPoints; i > SafeCast.toInt(insertAt); --i) {
			switchPoints[i].hours = switchPoints[i - 1].hours;
			switchPoints[i].minutes = switchPoints[i - 1].minutes;
			switchPoints[i].setback = switchPoints[i - 1].setback;
		}

		// Insert the new switch point
		switchPoints[SafeCast.toInt(insertAt)].hours = hours;
		switchPoints[SafeCast.toInt(insertAt)].minutes = minutes;
		switchPoints[SafeCast.toInt(insertAt)].setback = setback;

		++numSwitchPoints;
		return true;
	}

	public boolean removeSwitchPoint(byte idx) {
		if (SafeCast.toInt(idx) >= SafeCast.toInt(numSwitchPoints)) {
			return false;
		}

		for (int i = SafeCast.toInt(idx); i < (SafeCast.toInt(numSwitchPoints) - 1); ++i) {
			switchPoints[i].hours = switchPoints[i + 1].hours;
			switchPoints[i].minutes = switchPoints[i + 1].minutes;
			switchPoints[i].setback = switchPoints[i + 1].setback;
		}

		--numSwitchPoints;
		return true;
	}

	public boolean getSwitchPoint(byte idx, Byte hours, Byte minutes,
			Byte setback) {
		if (SafeCast.toInt(idx) >= SafeCast.toInt(numSwitchPoints)) {
			return false;
		}

		if (hours != null) {
			hours = switchPoints[idx].hours;
		}
		if (minutes != null) {
			minutes = switchPoints[idx].minutes;
		}
		if (setback != null) {
			setback = switchPoints[idx].setback;
		}

		return true;
	}

	public boolean findSwitchPoint(byte hours, byte minutes, Byte idx) {
		for (int i = 0; i < SafeCast.toInt(numSwitchPoints); ++i) {
			if (switchPoints[i].hours == hours) {
				if (switchPoints[i].minutes == minutes) {
					if (idx != null) {
						idx = (byte) i;
					}
					return true;
				}
				if (switchPoints[i].minutes > minutes) {
					return false;
				}
			} else if (switchPoints[i].hours > hours) {
				return false;
			}
		}
		return false;
	}

	private class SwitchPoint {
		public byte hours = 0;
		public byte minutes = 0;
		public byte setback = 0;
	}
}
