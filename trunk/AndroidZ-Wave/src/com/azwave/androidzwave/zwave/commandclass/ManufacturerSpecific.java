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

import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.xml.Product;

public class ManufacturerSpecific extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x72;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_MANUFACTURER_SPECIFIC";

	public static final byte MAN_SPEC_CMD_GET = 0x04;
	public static final byte MAN_SPEC_CMD_REPORT = 0x05;

	public ManufacturerSpecific(Node node) {
		super(node);
	}

	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_STATIC) != 0
				&& hasStaticRequest(STATIC_REQUEST_VALUES)) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}

	public boolean requestValue(int requestFlags, byte dummy1, byte instance,
			QueuePriority queue) {
		if (instance != 1) {
			return false;
		}
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID,
				MAN_SPEC_CMD_GET, node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public static String setProductDetails(Node node, short manufacturerId,
			short productType, short productId) {
		String manufacturerName = String.format("Unknown: id=%04x",
				manufacturerId);
		String productName = String.format("Unknown: type=%04x, id=%04x",
				productType, productId);
		String configName = "";

		if (node.getXMLManager().getManufacturerMap()
				.containsKey(manufacturerId)) {
			manufacturerName = node.getXMLManager().getManufacturerMap()
					.get(manufacturerId);

			long key = Product.getKey(manufacturerId, productType, productId);
			if (node.getXMLManager().getProductMap().containsKey(key)) {
				productName = node.getXMLManager().getProductMap().get(key)
						.getProductName();
				configName = node.getXMLManager().getProductMap().get(key)
						.getConfigPath();
			}
		}

		if (node.getManufacturerName().length() == 0) {
			node.setManufacturerName(manufacturerName);
		}
		if (node.getProductName().length() == 0) {
			node.setProductName(productName);
		}

		node.setManufacturerId(String.format("%04x", manufacturerId));
		node.setProductType(String.format("%04x", productType));
		node.setProductId(String.format("%04x", productId));

		return configName;
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
		if (MAN_SPEC_CMD_REPORT == data[0]) {
			short manufacturerId = (short) (SafeCast.toShort(data[1]) << 8 | SafeCast
					.toShort(data[2]));
			short productType = (short) (SafeCast.toShort(data[3]) << 8 | SafeCast
					.toShort(data[4]));
			short productId = (short) (SafeCast.toShort(data[5]) << 8 | SafeCast
					.toShort(data[6]));

			if (node != null) {
				String configPath = setProductDetails(node, manufacturerId,
						productType, productId);
				if (configPath.length() > 0) {
					node.getXMLManager().readProduct(node, configPath);
				}
				clearStaticRequest(STATIC_REQUEST_VALUES);
				node.setManufacturerReceived(true);
			}
			return true;
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
	public byte getMaxVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void createVars(byte mInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createVars(byte mInstance, byte index) {
		// TODO Auto-generated method stub

	}

}
