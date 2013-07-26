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

import java.util.concurrent.ConcurrentHashMap;

import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.HexDump;
import com.azwave.androidzwave.zwave.utils.SafeCast;

public class CommandClassManager {

	private ConcurrentHashMap<Byte, CommandClass> commandClassMap;
	private Node node;

	public CommandClassManager(Node node) {
		this.commandClassMap = new ConcurrentHashMap<Byte, CommandClass>();
		this.node = node;
	}

	// -----------------------------------------------------------------------------------------
	// C.R.U.D. Section
	// -----------------------------------------------------------------------------------------
	public CommandClass addCommandClass(byte commandClassId) {
		if (commandClassMap.containsKey(commandClassId)
				| !isSupported(commandClassId)) {
			return null;
		}

		node.getLog().add(
				String.format("Node %d - Add Command Class : %s",
						SafeCast.toInt(node.getNodeId()),
						getString(commandClassId)));

		CommandClass cc = CommandClassManager.create(node, commandClassId);
		commandClassMap.put(commandClassId, cc);
		return cc;
	}

	public boolean addCommandClasses(byte[] commandClassesId) {
		if (commandClassesId == null || commandClassesId.length == 0) {
			return false;
		}

		boolean aftermark = false;
		for (byte commandClassId : commandClassesId) {
			if (commandClassId == (byte) 0xEF) {
				aftermark = true;
			} else if (isSupported(commandClassId)) {
				CommandClass cc = addCommandClass(commandClassId);
				if (cc != null) {
					if (aftermark) {
						cc.setAfterMark();
					}
					cc.setInstance((byte) 1);
				}
			}
		}

		return true;
	}

	public CommandClass getCommandClass(byte commandClassId) {
		return commandClassMap.get(commandClassId);
	}

	public ConcurrentHashMap<Byte, CommandClass> getCommandClassMap() {
		return commandClassMap;
	}

	public CommandClass removeCommandClass(byte commandClassId) {
		if (!commandClassMap.containsKey(commandClassId)) {
			return null;
		}
		return commandClassMap.remove(commandClassId);
	}

	// -----------------------------------------------------------------------------------------
	// Static Methods Section
	// -----------------------------------------------------------------------------------------

	// -----------------------------------------------------------------------------------------
	// Static Methods Section
	// -----------------------------------------------------------------------------------------
	public static boolean isSupported(byte commandClassId) {
		return getString(commandClassId).length() == 0 ? false : true;
	}

	public static String getString(byte commandClassId) {
		switch (commandClassId) {
		case Alarm.COMMAND_CLASS_ID:
			return Alarm.COMMAND_CLASS_NAME;
		case ApplicationStatus.COMMAND_CLASS_ID:
			return ApplicationStatus.COMMAND_CLASS_NAME;
		case Association.COMMAND_CLASS_ID:
			return Association.COMMAND_CLASS_NAME;
		case AssociationCommandConfig.COMMAND_CLASS_ID:
			return AssociationCommandConfig.COMMAND_CLASS_NAME;
		case Basic.COMMAND_CLASS_ID:
			return Basic.COMMAND_CLASS_NAME;
		case Battery.COMMAND_CLASS_ID:
			return Battery.COMMAND_CLASS_NAME;
		case Clock.COMMAND_CLASS_ID:
			return Clock.COMMAND_CLASS_NAME;
		case Configuration.COMMAND_CLASS_ID:
			return Configuration.COMMAND_CLASS_NAME;
		case ControllerReplication.COMMAND_CLASS_ID:
			return ControllerReplication.COMMAND_CLASS_NAME;
		case CRC16Encap.COMMAND_CLASS_ID:
			return CRC16Encap.COMMAND_CLASS_NAME;
		case EnergyProduction.COMMAND_CLASS_ID:
			return EnergyProduction.COMMAND_CLASS_NAME;
		case Hail.COMMAND_CLASS_ID:
			return Hail.COMMAND_CLASS_NAME;
		case Indicator.COMMAND_CLASS_ID:
			return Indicator.COMMAND_CLASS_NAME;
		case Language.COMMAND_CLASS_ID:
			return Language.COMMAND_CLASS_NAME;
		case Lock.COMMAND_CLASS_ID:
			return Lock.COMMAND_CLASS_NAME;
		case ManufacturerSpecific.COMMAND_CLASS_ID:
			return ManufacturerSpecific.COMMAND_CLASS_NAME;
		case MeterPulse.COMMAND_CLASS_ID:
			return MeterPulse.COMMAND_CLASS_NAME;
		case MultiCommand.COMMAND_CLASS_ID:
			return MultiCommand.COMMAND_CLASS_NAME;
		case MultiInstance.COMMAND_CLASS_ID:
			return MultiInstance.COMMAND_CLASS_NAME;
		case MultiInstanceAssociation.COMMAND_CLASS_ID:
			return MultiInstanceAssociation.COMMAND_CLASS_NAME;
		case NoOperation.COMMAND_CLASS_ID:
			return NoOperation.COMMAND_CLASS_NAME;
		case PowerLevel.COMMAND_CLASS_ID:
			return PowerLevel.COMMAND_CLASS_NAME;
		case Proprietary.COMMAND_CLASS_ID:
			return Proprietary.COMMAND_CLASS_NAME;
		case Protection.COMMAND_CLASS_ID:
			return Protection.COMMAND_CLASS_NAME;
		case SceneActivation.COMMAND_CLASS_ID:
			return SceneActivation.COMMAND_CLASS_NAME;
		case SensorAlarm.COMMAND_CLASS_ID:
			return SensorAlarm.COMMAND_CLASS_NAME;
		case SensorBinary.COMMAND_CLASS_ID:
			return SensorBinary.COMMAND_CLASS_NAME;
		case SensorMultiLevel.COMMAND_CLASS_ID:
			return SensorMultiLevel.COMMAND_CLASS_NAME;
		case SwitchAll.COMMAND_CLASS_ID:
			return SwitchAll.COMMAND_CLASS_NAME;
		case SwitchBinary.COMMAND_CLASS_ID:
			return SwitchBinary.COMMAND_CLASS_NAME;
		case SwitchMultiLevel.COMMAND_CLASS_ID:
			return SwitchMultiLevel.COMMAND_CLASS_NAME;
		case SwitchToggleBinary.COMMAND_CLASS_ID:
			return SwitchToggleBinary.COMMAND_CLASS_NAME;
		case SwitchToggleMultiLevel.COMMAND_CLASS_ID:
			return SwitchToggleMultiLevel.COMMAND_CLASS_NAME;
		case ThermostatFanState.COMMAND_CLASS_ID:
			return ThermostatFanState.COMMAND_CLASS_NAME;
		case ThermostatOperatingState.COMMAND_CLASS_ID:
			return ThermostatOperatingState.COMMAND_CLASS_NAME;
		case Version.COMMAND_CLASS_ID:
			return Version.COMMAND_CLASS_NAME;
		case WakeUp.COMMAND_CLASS_ID:
			return WakeUp.COMMAND_CLASS_NAME;
		}

		return "";
	}

	private static CommandClass create(Node node, byte commandClassId) {
		switch (commandClassId) {
		case Alarm.COMMAND_CLASS_ID:
			return new Alarm(node);
		case ApplicationStatus.COMMAND_CLASS_ID:
			return new ApplicationStatus(node);
		case Association.COMMAND_CLASS_ID:
			return new Association(node);
		case AssociationCommandConfig.COMMAND_CLASS_ID:
			return new AssociationCommandConfig(node);
		case Basic.COMMAND_CLASS_ID:
			return new Basic(node);
		case Battery.COMMAND_CLASS_ID:
			return new Battery(node);
		case Clock.COMMAND_CLASS_ID:
			return new Clock(node);
		case Configuration.COMMAND_CLASS_ID:
			return new Configuration(node);
		case ControllerReplication.COMMAND_CLASS_ID:
			return new ControllerReplication(node);
		case CRC16Encap.COMMAND_CLASS_ID:
			return new CRC16Encap(node);
		case EnergyProduction.COMMAND_CLASS_ID:
			return new EnergyProduction(node);
		case Hail.COMMAND_CLASS_ID:
			return new Hail(node);
		case Indicator.COMMAND_CLASS_ID:
			return new Indicator(node);
		case Language.COMMAND_CLASS_ID:
			return new Language(node);
		case Lock.COMMAND_CLASS_ID:
			return new Lock(node);
		case ManufacturerSpecific.COMMAND_CLASS_ID:
			return new ManufacturerSpecific(node);
		case MeterPulse.COMMAND_CLASS_ID:
			return new MeterPulse(node);
		case MultiCommand.COMMAND_CLASS_ID:
			return new MultiCommand(node);
		case MultiInstance.COMMAND_CLASS_ID:
			return new MultiInstance(node);
		case MultiInstanceAssociation.COMMAND_CLASS_ID:
			return new MultiInstanceAssociation(node);
		case NoOperation.COMMAND_CLASS_ID:
			return new NoOperation(node);
		case PowerLevel.COMMAND_CLASS_ID:
			return new PowerLevel(node);
		case Proprietary.COMMAND_CLASS_ID:
			return new Proprietary(node);
		case Protection.COMMAND_CLASS_ID:
			return new Protection(node);
		case SceneActivation.COMMAND_CLASS_ID:
			return new SceneActivation(node);
		case SensorAlarm.COMMAND_CLASS_ID:
			return new SensorAlarm(node);
		case SensorBinary.COMMAND_CLASS_ID:
			return new SensorBinary(node);
		case SensorMultiLevel.COMMAND_CLASS_ID:
			return new SensorMultiLevel(node);
		case SwitchAll.COMMAND_CLASS_ID:
			return new SwitchAll(node);
		case SwitchBinary.COMMAND_CLASS_ID:
			return new SwitchBinary(node);
		case SwitchMultiLevel.COMMAND_CLASS_ID:
			return new SwitchMultiLevel(node);
		case SwitchToggleBinary.COMMAND_CLASS_ID:
			return new SwitchToggleBinary(node);
		case SwitchToggleMultiLevel.COMMAND_CLASS_ID:
			return new SwitchToggleMultiLevel(node);
		case ThermostatFanState.COMMAND_CLASS_ID:
			return new ThermostatFanState(node);
		case ThermostatOperatingState.COMMAND_CLASS_ID:
			return new ThermostatOperatingState(node);
		case Version.COMMAND_CLASS_ID:
			return new Version(node);
		case WakeUp.COMMAND_CLASS_ID:
			return new WakeUp(node);
		}

		return null;
	}

}
