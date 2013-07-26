package com.azwave.androidzwave.zwave.xml;

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

public class DeviceClass {

	protected byte[] mandatoryCommandClasses;
	protected byte basicMapping;
	protected String basicLabel;

	public DeviceClass(byte[] mandatory, byte mapping, String label) {
		mandatoryCommandClasses = mandatory;
		basicMapping = mapping;
		basicLabel = label;
	}

	public byte[] getMandatoryCommandClasses() {
		return mandatoryCommandClasses;
	}

	public byte getBasicMapping() {
		return basicMapping;
	}

	public String getLabel() {
		return basicLabel;
	}

}
