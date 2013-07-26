package com.azwave.androidzwave.zwave.commandclass;

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

import java.util.Arrays;

import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueDecimal;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;

public class SensorMultiLevel extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x31;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_SENSOR_MULTILEVEL";

	public static final byte SENSOR_MULTI_LEVEL_CMD_GET = (byte) 0x04;
	public static final byte SENSOR_MULTI_LEVEL_CMD_REPORT = (byte) 0x05;

	public enum SensorType {
		None, Temperature, General, Luminance, Power, RelativeHumidity, Velocity, Direction, AtmosphericPressure, BarometricPressure, SolarRadiation, DewPoint, RainRate, TideLevel, Weight, Voltage, Current, CO2, AirFlow, TankCapacity, Distance
	}

	private static final String sensorTypeNames[] = { "Undefined",
			"Temperature", "General", "Luminance", "Power",
			"Relative Humidity", "Velocity", "Direction",
			"Atmospheric Pressure", "Barometric Pressure", "Solar Radiation",
			"Dew Point", "Rain Rate", "Tide Level", "Weight", "Voltage",
			"Current", "CO2 Level", "Air Flow", "Tank Capacity", "Distance" };

	private static final String tankCapcityUnits[] = { "l", "cbm", "gal" };

	private static final String distanceUnits[] = { "m", "cm", "ft" };

	public SensorMultiLevel(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	public boolean requestValue(int requestFlags, byte dummy1, byte instance,
			QueuePriority queue) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				SENSOR_MULTI_LEVEL_CMD_GET,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public void createVars(byte instance) {
		// do in handle message
	}

	public byte getMaxVersion() {
		return 1;
	}

	@Override
	public byte getCommandClassId() {
		return COMMAND_CLASS_ID;
	}

	@Override
	public String getCommandClassName() {
		return COMMAND_CLASS_NAME;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (SENSOR_MULTI_LEVEL_CMD_REPORT == data[0]) {
			byte[] scale = null;
			byte[] precision = { 0 };
			byte sensorType = data[1];
			String str = extractValue(Arrays.copyOfRange(data, 2, data.length),
					scale, precision);

			if (node != null) {
				String units = "";
				switch (SensorType.values()[sensorType]) {
				case Temperature:
					units = scale != null ? "F" : "C";
					break;
				case General:
					units = scale != null ? "" : "%";
					break;
				case Luminance:
					units = scale != null ? "lux" : "%";
					break;
				case Power:
					units = scale != null ? "BTU/h" : "W";
					break;
				case RelativeHumidity:
					units = "%";
					break;
				case Velocity:
					units = scale != null ? "mph" : "m/s";
					break;
				case Direction:
					units = "";
					break;
				case AtmosphericPressure:
					units = scale != null ? "inHg" : "kPa";
					break;
				case BarometricPressure:
					units = scale != null ? "inHg" : "kPa";
					break;
				case SolarRadiation:
					units = "W/m2";
					break;
				case DewPoint:
					units = scale != null ? "in/h" : "mm/h";
					break;
				case RainRate:
					units = scale != null ? "F" : "C";
					break;
				case TideLevel:
					units = scale != null ? "ft" : "m";
					break;
				case Weight:
					units = scale != null ? "lb" : "kg";
					break;
				case Voltage:
					units = scale != null ? "mV" : "V";
					break;
				case Current:
					units = scale != null ? "mA" : "A";
					break;
				case CO2:
					units = "ppm";
					break;
				case AirFlow:
					units = scale != null ? "cfm" : "m3/h";
					break;
				case TankCapacity:
					units = tankCapcityUnits[scale != null && scale.length == 0 ? scale[0]
							: 0];
					break;
				case Distance:
					units = distanceUnits[scale != null && scale.length == 0 ? scale[0]
							: 0];
					break;
				default:
					break;
				}

				ValueDecimal value = (ValueDecimal) getValue((byte) instance,
						sensorType);
				if (value == null) {
					node.getValueManager().createValueDecimal(ValueGenre.USER,
							getCommandClassId(), (byte) instance, sensorType,
							sensorTypeNames[sensorType], units, true, false,
							"0.0", (byte) 0);
				} else {
					value.setUnits(units);
				}

				if (value.getPrecision() != precision[0]) {
					value.setPrecision(precision[0]);
				}
				value.onValueRefreshed(str);
				return true;
			}
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setValue(Value value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

}
