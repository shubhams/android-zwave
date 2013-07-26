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

import com.azwave.androidzwave.zwave.nodes.Node;

public class ValueButton extends Value {
	private boolean pressed = false;

	public ValueButton() {
	}

	public ValueButton(ValueId id, String label, byte pollIntensity) {
		super(id, label, "", false, true, true, pollIntensity);
	}

	public boolean isPressed() {
		return pressed;
	}

	public boolean pressButton() {
		pressed = true;
		boolean res = super.set();
		return res;
	}

	public boolean releaseButton() {
		pressed = false;
		boolean res = super.set();

		Node node = getId().getNode();
		if (node != null) {
			node.requestDynamicValues();
		}

		return res;
	}
}
