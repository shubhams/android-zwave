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

public class EnergyProduction extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x90;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_ENERGY_PRODUCTION";

	public static final byte ENERGY_PROD_CMD_GET = (byte) 0x02;
	public static final byte ENERGY_PROD_CMD_REPORT = (byte) 0x03;

	public enum EnergyProductionIndex {
		Instant, Total, Today, Time
	}

	private static final String energyParameterNames[] = {
			"Instant energy production", "Total energy production",
			"Energy production today", "Total production time" };

	public EnergyProduction(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte getCommandClassId() {
		// TODO Auto-generated method stub
		return COMMAND_CLASS_ID;
	}

	@Override
	public String getCommandClassName() {
		// TODO Auto-generated method stub
		return COMMAND_CLASS_NAME;
	}

	@Override
	public byte getMaxVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (ENERGY_PROD_CMD_REPORT == data[0]) {
			byte[] scale = null;
			byte[] precision = { 0 };
			String str = extractValue(Arrays.copyOfRange(data, 2, data.length),
					scale, precision);

			ValueDecimal value = (ValueDecimal) getValue((byte) instance,
					data[1]);
			if (value != null) {
				value.onValueRefreshed(str);
				if (value.getPrecision() != precision[0]) {
					value.setPrecision(precision[0]);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		boolean request = false;
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
			request |= requestValue(requestFlags,
					(byte) EnergyProductionIndex.Instant.ordinal(), instance,
					queue);
			request |= requestValue(requestFlags,
					(byte) EnergyProductionIndex.Total.ordinal(), instance,
					queue);
			request |= requestValue(requestFlags,
					(byte) EnergyProductionIndex.Today.ordinal(), instance,
					queue);
			request |= requestValue(requestFlags,
					(byte) EnergyProductionIndex.Time.ordinal(), instance,
					queue);
		}
		return request;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 3, COMMAND_CLASS_ID,
				ENERGY_PROD_CMD_GET, index,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance) {
		if (node != null) {
			node.getValueManager().createValueDecimal(
					ValueGenre.USER,
					getCommandClassId(),
					(byte) mInstance,
					(byte) EnergyProductionIndex.Instant.ordinal(),
					energyParameterNames[EnergyProductionIndex.Instant
							.ordinal()], "W", true, false, "0.0", (byte) 0);
			node.getValueManager()
					.createValueDecimal(
							ValueGenre.USER,
							getCommandClassId(),
							(byte) mInstance,
							(byte) EnergyProductionIndex.Total.ordinal(),
							energyParameterNames[EnergyProductionIndex.Total
									.ordinal()], "kWh", true, false, "0.0",
							(byte) 0);
			node.getValueManager()
					.createValueDecimal(
							ValueGenre.USER,
							getCommandClassId(),
							(byte) mInstance,
							(byte) EnergyProductionIndex.Today.ordinal(),
							energyParameterNames[EnergyProductionIndex.Today
									.ordinal()], "kWh", true, false, "0.0",
							(byte) 0);
			node.getValueManager().createValueDecimal(ValueGenre.USER,
					getCommandClassId(), (byte) mInstance,
					(byte) EnergyProductionIndex.Time.ordinal(),
					energyParameterNames[EnergyProductionIndex.Time.ordinal()],
					"", true, false, "0.0", (byte) 0);
		}
	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setValue(Value value) {
		// TODO Auto-generated method stub
		return false;
	}

}
