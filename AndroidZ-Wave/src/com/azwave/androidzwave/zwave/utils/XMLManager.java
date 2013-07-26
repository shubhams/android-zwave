package com.azwave.androidzwave.zwave.utils;

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
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.azwave.androidzwave.zwave.commandclass.CommandClass;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueBool;
import com.azwave.androidzwave.zwave.values.ValueButton;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueDecimal;
import com.azwave.androidzwave.zwave.values.ValueId;
import com.azwave.androidzwave.zwave.values.ValueInt;
import com.azwave.androidzwave.zwave.values.ValueList;
import com.azwave.androidzwave.zwave.values.ValueRaw;
import com.azwave.androidzwave.zwave.values.ValueSchedule;
import com.azwave.androidzwave.zwave.values.ValueShort;
import com.azwave.androidzwave.zwave.values.ValueString;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueId.ValueType;
import com.azwave.androidzwave.zwave.xml.DeviceClass;
import com.azwave.androidzwave.zwave.xml.GenericDeviceClass;
import com.azwave.androidzwave.zwave.xml.Product;

public abstract class XMLManager {

	protected DocumentBuilderFactory dbf;
	protected DocumentBuilder db;
	protected Log log;

	protected HashMap<Byte, String> basicDeviceClasses;
	protected HashMap<Byte, GenericDeviceClass> genericDeviceClasses;

	protected HashMap<Short, String> manufacturerMap;
	protected HashMap<Long, Product> productMap;

	public XMLManager(Log log) {
		this.log = log;
		this.dbf = DocumentBuilderFactory.newInstance();

		basicDeviceClasses = new HashMap<Byte, String>();
		genericDeviceClasses = new HashMap<Byte, GenericDeviceClass>();

		manufacturerMap = new HashMap<Short, String>();
		productMap = new HashMap<Long, Product>();
	}

	protected abstract Document readAsset(String filename) throws Exception;

	public abstract void readDeviceClasses();

	public abstract void readManufacturerSpecific();

	public HashMap<Byte, String> getBasicDeviceClasses() {
		return basicDeviceClasses;
	}

	public HashMap<Byte, GenericDeviceClass> getGenericDeviceClasses() {
		return genericDeviceClasses;
	}

	public HashMap<Short, String> getManufacturerMap() {
		return manufacturerMap;
	}

	public HashMap<Long, Product> getProductMap() {
		return productMap;
	}

	public void readDeviceClasses(String location) {
		NodeList nodeList, nodeListSpec;
		Node node, nodeSpec;
		Element elem, elemSpec;

		byte key, mapping;
		byte keyspec, mappingspec;

		String label, cc, basic;
		String labelspec, ccspec, basicspec;

		byte[] mandatory, mandatoryspec;
		String[] strTemp, strTempSpec;

		try {
			Document doc = readAsset(location);
			doc.getDocumentElement().normalize();

			nodeList = doc.getElementsByTagName("Basic");
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				elem = (Element) node;

				label = elem.getAttribute("label");
				key = Integer.decode(elem.getAttribute("key")).byteValue();

				if (label.length() > 0) {
					basicDeviceClasses.put(key, label);
				}
			}

			nodeList = doc.getElementsByTagName("Generic");
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				elem = (Element) node;

				key = Integer.decode(elem.getAttribute("key")).byteValue();
				label = elem.getAttribute("label");
				cc = elem.getAttribute("command_classes");

				if (cc.length() > 0) {
					strTemp = cc.split(",");
					mandatory = new byte[strTemp.length];

					for (int j = 0; j < strTemp.length; j++) {
						mandatory[j] = Integer.decode(strTemp[j]).byteValue();
					}
				} else {
					mandatory = null;
				}

				basic = elem.getAttribute("basic");
				mapping = basic.length() > 0 ? Integer.decode(basic)
						.byteValue() : 0;

				HashMap<Byte, DeviceClass> specificDeviceClasses = new HashMap<Byte, DeviceClass>();
				nodeListSpec = elem.getElementsByTagName("Specific");
				for (int j = 0; j < nodeListSpec.getLength(); j++) {
					nodeSpec = nodeListSpec.item(j);
					elemSpec = (Element) nodeSpec;

					keyspec = Integer.decode(elemSpec.getAttribute("key"))
							.byteValue();
					labelspec = elemSpec.getAttribute("label");
					ccspec = elemSpec.getAttribute("command_classes");

					if (ccspec.length() > 0) {
						strTempSpec = ccspec.split(",");
						mandatoryspec = new byte[strTempSpec.length];

						for (int k = 0; k < strTempSpec.length; k++) {
							mandatoryspec[k] = Integer.decode(strTempSpec[k])
									.byteValue();
						}
					} else {
						mandatoryspec = null;
					}

					basicspec = elemSpec.getAttribute("basic");
					mappingspec = basicspec.length() > 0 ? Integer.decode(
							basicspec).byteValue() : 0;

					specificDeviceClasses.put(keyspec, new DeviceClass(
							mandatoryspec, mappingspec, labelspec));
				}

				genericDeviceClasses.put(key, new GenericDeviceClass(mandatory,
						mapping, label, specificDeviceClasses));
			}
			log.add(String.format(
					"Reading 'device_classes.xml' (basics: %d, generics: %d)",
					basicDeviceClasses.size(), genericDeviceClasses.size()));
		} catch (Exception e) {
			log.add(String.format(
					"Error while reading 'device_classes.xml': %s",
					e.toString()));
		}
	}

	public void readManufacturerSpecific(String location) {
		NodeList nodeList, nodeListProduct;
		Node node, nodeProduct;
		Element elem, elemProduct;

		short id, idProduct, typeProduct;
		String name, nameProduct, configProduct;

		try {
			Document doc = readAsset(location);
			doc.getDocumentElement().normalize();

			nodeList = doc.getElementsByTagName("Manufacturer");
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				elem = (Element) node;

				id = Integer.decode("0x" + elem.getAttribute("id"))
						.shortValue();
				name = elem.getAttribute("name");
				if (name.length() > 0) {
					manufacturerMap.put(id, name);
				}

				nodeListProduct = elem.getElementsByTagName("Product");
				for (int j = 0; j < nodeListProduct.getLength(); j++) {
					nodeProduct = nodeListProduct.item(j);
					elemProduct = (Element) nodeProduct;

					typeProduct = Integer.decode(
							"0x" + elemProduct.getAttribute("type"))
							.shortValue();
					idProduct = Integer.decode(
							"0x" + elemProduct.getAttribute("id")).shortValue();
					nameProduct = elemProduct.getAttribute("name");
					configProduct = elemProduct.getAttribute("config");

					Product product = new Product(id, typeProduct, idProduct,
							nameProduct, configProduct);
					if (!productMap.containsKey(product.getKey())) {
						productMap.put(product.getKey(), product);
					}
				}
			}

			log.add(String
					.format("Reading 'manufacturer_specific.xml' (manufacturers: %d, products: %d)",
							manufacturerMap.size(), productMap.size()));

		} catch (Exception e) {
			log.add(String.format(
					"Error while reading 'manufacturer_specific.xml': %s",
					e.toString()));
		}
	}

	public boolean readProduct(com.azwave.androidzwave.zwave.nodes.Node node,
			String location) {
		if (location == null || location.length() == 0) {
			return false;
		}

		try {
			Document doc = readAsset(location);
			doc.getDocumentElement().normalize();

			if (node.getQueryStage() == QueryStage.ManSpec1) {
				readDeviceProtocol(node, doc);
			} else {
				if (!node.getManufacturerReceived()) {
					readDeviceProtocol(node, doc);
				}
				readCommandClasses(node, doc);
			}
		} catch (Exception x) {
			return false;
		}

		return true;
	}

	private void readDeviceProtocol(
			com.azwave.androidzwave.zwave.nodes.Node node, Document doc)
			throws Exception {
		NodeList xmlnodes = doc.getElementsByTagName("Protocol");
		for (int i = 0; i < xmlnodes.getLength(); i++) {
			Node xmlnode = xmlnodes.item(i);
			Element xmlelem = (Element) xmlnode;

			String nodeInfo = xmlelem.getAttribute("nodeinfosupported");
			if (nodeInfo != null && nodeInfo.length() > 0) {
				node.setNodeInfoSupported(nodeInfo.compareToIgnoreCase("true") == 0);
			}

			NodeList xmlnodes2 = xmlelem.getElementsByTagName("APIcall");
			for (int j = 0; j < xmlnodes2.getLength(); j++) {
				Node xmlnode2 = xmlnodes2.item(j);
				Element xmlelem2 = (Element) xmlnode2;

				String funcstr = xmlelem2.getAttribute("function");
				if (funcstr != null && funcstr.length() > 0) {
					byte func = Integer.decode(funcstr).byteValue();
					String present = xmlelem2.getAttribute("present");
					node.getPrimaryController().setAPICall(func,
							present.compareToIgnoreCase("true") == 0);
				}
			}
		}
	}

	private void readCommandClasses(
			com.azwave.androidzwave.zwave.nodes.Node node, Document doc)
			throws Exception {
		NodeList xmlnodes = doc.getElementsByTagName("CommandClass");
		for (int i = 0; i < xmlnodes.getLength(); i++) {
			Node xmlnode = xmlnodes.item(i);
			Element xmlelem = (Element) xmlnode;

			String idstr = xmlelem.getAttribute("id");
			if (idstr != null && idstr.length() > 0) {
				byte id = Integer.decode(idstr).byteValue();
				String action = xmlelem.getAttribute("action");
				boolean remove = action != null && action.length() > 0
						&& action.compareToIgnoreCase("remove") == 0;

				CommandClass cc = node.getCommandClassManager()
						.getCommandClass(id);
				if (remove) {
					node.getCommandClassManager().removeCommandClass(id);
				} else {
					if (cc == null) {
						cc = node.getCommandClassManager().addCommandClass(id);
					}
					if (cc != null) {
						readCommandClass(cc, xmlnode);
					}
				}
			}
		}
	}

	private void readCommandClass(CommandClass cc, Node xmlnode)
			throws Exception {
		Element xmlelem = (Element) xmlnode, elem;
		String temp;
		NodeList xmlnodes;

		Node node;
		byte instances = (byte) 1;

		temp = xmlelem.getAttribute("version");
		if (temp != null && temp.length() > 0) {
			cc.setVersion(Integer.decode(temp).byteValue());
		}

		temp = xmlelem.getAttribute("instances");
		if (temp != null && temp.length() > 0) {
			instances = Integer.decode(temp).byteValue();
		}

		temp = xmlelem.getAttribute("request_flags");
		if (temp != null && temp.length() > 0) {
			cc.setStaticRequest(Integer.decode(temp).byteValue());
		}

		temp = xmlelem.getAttribute("override_precision");
		if (temp != null && temp.length() > 0) {
			cc.setOverridePrecision(Integer.decode(temp).byteValue());
		}

		temp = xmlelem.getAttribute("after_mark");
		cc.setAfterMark(temp != null && temp.length() > 0
				&& temp.compareToIgnoreCase("true") == 0);

		temp = xmlelem.getAttribute("create_vars");
		cc.setCreateVars(temp != null && temp.length() > 0
				&& temp.compareToIgnoreCase("true") == 0);
		if (!cc.isCreateVars()) {
			if (cc.getNode() != null) {
				cc.getNode().getValueManager()
						.removeCommandClassValues(cc.getCommandClassId());
			}
		}

		temp = xmlelem.getAttribute("getsupported");
		cc.setSupported(temp != null && temp.length() > 0
				&& temp.compareToIgnoreCase("true") == 0);
		cc.setInstances(instances);

		xmlnodes = xmlelem.getElementsByTagName("Instance");
		for (int i = 0; i < xmlnodes.getLength(); i++) {
			node = xmlnodes.item(i);
			elem = (Element) node;
			byte instance = 1;

			temp = xmlelem.getAttribute("index");
			if (temp != null && temp.length() > 0) {
				instance = Integer.decode(temp).byteValue();
				cc.setInstance(instance);
			}

			temp = xmlelem.getAttribute("endpoint");
			if (temp != null && temp.length() > 0) {
				cc.setEndPoint(instance, Integer.decode(temp).byteValue());
			}
		}

		xmlnodes = xmlelem.getElementsByTagName("Value");
		for (int i = 0; i < xmlnodes.getLength(); i++) {
			readValue(cc, xmlnodes.item(i));
		}
	}

	private boolean readValue(CommandClass cc, Node xmlnode) {
		Element xmlelem = (Element) xmlnode;
		Value value = null;
		ValueType type = Value.getValueType(xmlelem.getAttribute("type"));

		switch (type) {
		case BOOL:
			value = new ValueBool();
			break;
		case BYTE:
			value = new ValueByte();
			break;
		case DECIMAL:
			value = new ValueDecimal();
			break;
		case INT:
			value = new ValueInt();
			break;
		case LIST:
			value = new ValueList();
			break;
		case SCHEDULE:
			value = new ValueSchedule();
			break;
		case SHORT:
			value = new ValueShort();
			break;
		case STRING:
			value = new ValueString();
			break;
		case BUTTON:
			value = new ValueButton();
			break;
		case RAW:
			value = new ValueRaw();
			break;
		}

		if (value != null) {
			readValueDetail(value, cc, xmlnode);
			if (cc.getNode().getValueManager().addValue(value)) {
				return true;
			}
		}
		return false;
	}

	private void readValueDetail(Value value, CommandClass cc, Node xmlnode) {
		Element xmlelem = (Element) xmlnode;
		String temp;
		byte instance = 1, index = 0;

		ValueGenre genre = Value.getValueGenre(xmlelem.getAttribute("genre"));
		ValueType type = Value.getValueType(xmlelem.getAttribute("type"));

		temp = xmlelem.getAttribute("instance");
		if (temp != null && temp.length() > 0) {
			instance = Integer.decode(temp).byteValue();
		}

		temp = xmlelem.getAttribute("index");
		if (temp != null && temp.length() > 0) {
			index = Integer.decode(temp).byteValue();
		}

		value.setId(new ValueId(cc.getNode(), genre, cc.getCommandClassId(),
				instance, index, type));

		temp = xmlelem.getAttribute("label");
		if (temp != null && temp.length() > 0) {
			value.setLabel(temp);
		}

		temp = xmlelem.getAttribute("units");
		if (temp != null && temp.length() > 0) {
			value.setUnits(temp);
		}

		temp = xmlelem.getAttribute("read_only");
		value.setReadOnly(temp != null && temp.length() > 0
				&& temp.compareToIgnoreCase("true") == 0);

		temp = xmlelem.getAttribute("write_only");
		value.setWriteOnly(temp != null && temp.length() > 0
				&& temp.compareToIgnoreCase("true") == 0);

		temp = xmlelem.getAttribute("affects");
		if (temp != null && temp.length() > 0) {
			if (value.getAffectsLength() != 0) {
				value.setAffects(null);
			}
			value.setAffectsLength((byte) 0);
			if (temp.compareToIgnoreCase("all") == 0) {
				value.setAffectsAll(true);
			} else {
				String[] str_temp = temp.split(",");
				byte[] affect_temp = new byte[str_temp.length];
				for (int i = 0; i < str_temp.length; i++) {
					try {
						affect_temp[i] = (byte) Integer.parseInt(str_temp[i]);
					} catch (Exception x) {
						affect_temp = Arrays.copyOf(affect_temp, i + 1);
						break;
					}
				}
				value.setAffectsLength((byte) affect_temp.length);
				value.setAffects(affect_temp);
			}
		}

		temp = xmlelem.getAttribute("verify_changes");
		value.setVerifyChanges(temp != null && temp.length() > 0
				&& temp.compareToIgnoreCase("true") == 0);

		temp = xmlelem.getAttribute("min");
		if (temp != null && temp.length() > 0) {
			value.setMin(Integer.decode(temp));
		}

		temp = xmlelem.getAttribute("max");
		if (temp != null && temp.length() > 0) {
			value.setMax(Integer.decode(temp));
		}

		NodeList xmlnodes = xmlelem.getElementsByTagName("Help");
		for (int i = 0; i < xmlnodes.getLength(); i++) {
			value.setHelp(((Element) xmlnodes.item(i)).getTextContent());
		}
	}

	public void reloadConfigManufacturer(
			com.azwave.androidzwave.zwave.nodes.Node node) {
		if (node != null) {
			if (manufacturerMap.size() == 0 || productMap.size() == 0) {
				readManufacturerSpecific();
			}

			short manufacturerId = node.getManufacturerId().length() == 0 ? 0
					: Integer.decode(node.getManufacturerId()).shortValue();
			short productType = node.getProductType().length() == 0 ? 0
					: Integer.decode(node.getProductType()).shortValue();
			short productId = node.getProductId().length() == 0 ? 0 : Integer
					.decode(node.getProductId()).shortValue();

			if (manufacturerMap.containsKey(manufacturerId)) {
				long key = Product.getKey(manufacturerId, productType,
						productId);
				if (productMap.containsKey(key)) {
					String config = productMap.get(key).getConfigPath();
					if (config != null && config.length() > 0) {
						readProduct(node, config);
					}
				}
			}
		}
	}

}
