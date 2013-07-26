package com.azwave.androidzwave.zwave.nodes;

//-----------------------------------------------------------------------------
//Copyright (c) 2009 Mal Lansell <xpl@lansell.org>
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
import java.util.Map.Entry;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.commandclass.Association;
import com.azwave.androidzwave.zwave.commandclass.Basic;
import com.azwave.androidzwave.zwave.commandclass.CommandClass;
import com.azwave.androidzwave.zwave.commandclass.CommandClassManager;
import com.azwave.androidzwave.zwave.commandclass.Configuration;
import com.azwave.androidzwave.zwave.commandclass.ManufacturerSpecific;
import com.azwave.androidzwave.zwave.commandclass.MultiInstance;
import com.azwave.androidzwave.zwave.commandclass.NoOperation;
import com.azwave.androidzwave.zwave.commandclass.Version;
import com.azwave.androidzwave.zwave.commandclass.WakeUp;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueManager;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.utils.Log;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.utils.XMLManager;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueBool;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueInt;
import com.azwave.androidzwave.zwave.values.ValueList;
import com.azwave.androidzwave.zwave.values.ValueManager;
import com.azwave.androidzwave.zwave.values.ValueShort;
import com.azwave.androidzwave.zwave.xml.DeviceClass;
import com.azwave.androidzwave.zwave.xml.GenericDeviceClass;
import com.hoho.android.usbserial.util.HexDump;

public class Node {

	// -----------------------------------------------------------------------------------------
	// Class Info Variable
	// -----------------------------------------------------------------------------------------
	private int homeId;
	private byte nodeId;
	private byte primaryId = 1;
	private byte[] neighbors = new byte[Defs.NUM_NODE_BITFIELD_BYTES];
	private byte[] routeNodes = new byte[5];

	private Log log;
	private Controller primaryController;

	private XMLManager xml;
	private QueueManager queue;
	private CommandClassManager ccManager;
	private ValueManager values;
	private NodeListener listener;

	// -----------------------------------------------------------------------------------------
	// Protocol Info Variable
	// -----------------------------------------------------------------------------------------
	private static final byte SECURITY_FLAG_SECURITY = (byte) 0x01;
	private static final byte SECURITY_FLAG_CONTROLLER = (byte) 0x02;
	private static final byte SECURITY_FLAG_SPECIFIC_DEVICE = (byte) 0x04;
	private static final byte SECURITY_FLAG_ROUTING_SLAVE = (byte) 0x08;
	private static final byte SECURITY_FLAG_BEAM_CAPABILITY = (byte) 0x10;
	private static final byte SECURITY_FLAG_SENSOR_250MS = (byte) 0x20;
	private static final byte SECURITY_FLAG_SENSOR_1000MS = (byte) 0x40;
	private static final byte SECURITY_FLAG_OPTIONAL_FUNC = (byte) 0x80;

	private boolean listeningDevice = true;
	private boolean frequentListeningDevice = false;
	private boolean beamingDevice = false;
	private boolean routingDevice = false;
	private boolean securityDevice = false;

	private int maxBaudRate = 0;
	private byte version = 0;

	private boolean nodeAlive = true;
	private boolean protocolInfoReceived = false;

	private byte basicId = 0;
	private byte genericId = 0;
	private byte specificId = 0;
	private byte basicMapping = 0;
	private String deviceType = "";

	private GenericDeviceClass genericDeviceClass;
	private DeviceClass deviceClass;

	private int error = 0;

	// -----------------------------------------------------------------------------------------
	// Device Naming Info Variable
	// -----------------------------------------------------------------------------------------
	private String manufacturerName = "";
	private String productName = "";
	private String nodeName = "";
	private String location = "";

	private String manufacturerId = "";
	private String productType = "";
	private String productId = "";

	private boolean manufacturerReceived = false;

	// -----------------------------------------------------------------------------------------
	// Node Info Variable
	// -----------------------------------------------------------------------------------------
	private boolean nodeInfoReceived = false;
	private boolean nodeInfoSupported = true;

	// -----------------------------------------------------------------------------------------
	// Query Info Variable
	// -----------------------------------------------------------------------------------------
	public enum QueryStage {
		ProtocolInfo, Probe1, WakeUp, ManSpec1, NodeInfo, ManSpec2, Version, Instances, Static, Probe2, Associations, Neighbors, Session, Dynamic, Config, Complete, None
	}

	private QueryStage queryStage = QueryStage.None;
	private boolean queryPending = false;
	private boolean queryConfiguration = false;
	private byte queryRetries = 0;

	// -----------------------------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------------------------
	public Node(int homeId, byte nodeId, Controller primaryController,
			QueueManager queue, XMLManager xml, Log log) {
		this.homeId = homeId;
		this.nodeId = nodeId;

		this.log = log;
		this.xml = xml;
		this.queue = queue;
		this.primaryController = primaryController;

		this.values = new ValueManager(this);
		this.ccManager = new CommandClassManager(this);

		if (primaryController != null) {
			primaryId = primaryController.getNodeId();
			ccManager.addCommandClass(NoOperation.COMMAND_CLASS_ID);
		}

		Arrays.fill(neighbors, (byte) 0);
		Arrays.fill(routeNodes, (byte) 0);
	}

	// -----------------------------------------------------------------------------------------
	// Class Info Methods
	// -----------------------------------------------------------------------------------------
	public int getHomeId() {
		return homeId;
	}

	public byte getNodeId() {
		return nodeId;
	}

	public byte getPrimaryNodeId() {
		return primaryId;
	}

	public Controller getPrimaryController() {
		return (Controller) (primaryController == null ? this
				: primaryController);
	}

	public QueueManager getQueueManager() {
		return queue;
	}

	public Log getLog() {
		return log;
	}

	public XMLManager getXMLManager() {
		return xml;
	}

	public CommandClassManager getCommandClassManager() {
		return ccManager;
	}

	public ValueManager getValueManager() {
		return values;
	}

	public void setNodeListener(NodeListener listener) {
		this.listener = listener;
	}

	public NodeListener getNodeListener() {
		return listener;
	}

	// -----------------------------------------------------------------------------------------
	// Protocol Info Methods
	// -----------------------------------------------------------------------------------------
	public void updateProtocolInfo(byte[] data) {
		if (data[4] == 0) {
			setNodeAlive(false);
		} else if (!protocolInfoReceived) {
			listeningDevice = ((data[0] & 0x80) != 0);
			routingDevice = ((data[0] & 0x40) != 0);
			frequentListeningDevice = ((data[1] & (SECURITY_FLAG_SENSOR_250MS | SECURITY_FLAG_SENSOR_1000MS)) != 0);
			beamingDevice = ((data[1] & SECURITY_FLAG_BEAM_CAPABILITY) != 0);
			securityDevice = ((data[1] & SECURITY_FLAG_SECURITY) != 0);

			version = (byte) ((data[0] & (byte) 0x07) + 1);
			maxBaudRate = (data[0] & 0x38) == 0x10 ? 40000 : 9600;

			/* Logs */
			log.add(String.format("FUNC_ID_ZW_GET_NODE_PROTOCOL_INFO -- Protocol Info: Node %d",
					SafeCast.toInt(nodeId)));
			log.add(String.format("--- Is Listening	  : %b", listeningDevice));
			log.add(String.format("--- Is Routing  	  : %b", routingDevice));
			log.add(String.format("--- Is Frequent 	  : %b",
					frequentListeningDevice));
			log.add(String.format("--- Is Beaming 	  : %b", beamingDevice));
			log.add(String.format("--- Is Security 	  : %b", securityDevice));
			log.add(String.format("--- API Version	  : %d",
					SafeCast.toInt(version)));
			log.add(String.format("--- Max. Baud Rate : %d", maxBaudRate));
			log.add("=======================");
			log.add(String.format("--- Basic Device   : %d", SafeCast.toInt(data[3])));
			log.add(String.format("--- Generic Device : %d", SafeCast.toInt(data[4])));
			log.add(String.format("--- Specific Device: %d", SafeCast.toInt(data[5])));
			log.add("=======================");

			setDeviceClasses(data[3], data[4], data[5]);

			protocolInfoReceived = true;
		}
	}

	private void setDeviceClasses(byte basic, byte generic, byte specific) {
		basicId = basic;
		genericId = generic;
		specificId = specific;
		deviceType = xml.getBasicDeviceClasses().containsKey(basic) ? xml
				.getBasicDeviceClasses().get(basic) : "";

		if (xml.getGenericDeviceClasses().containsKey(generic)) {
			genericDeviceClass = xml.getGenericDeviceClasses().get(generic);
			deviceType = genericDeviceClass.getLabel();

			ccManager.addCommandClasses(genericDeviceClass
					.getMandatoryCommandClasses());
			basicMapping = genericDeviceClass.getBasicMapping();

			if (genericDeviceClass.getSpecificDeviceClasses().containsKey(
					specific)) {
				deviceClass = genericDeviceClass.getDeviceClass(specific);
				deviceType = deviceClass.getLabel();

				ccManager.addCommandClasses(deviceClass
						.getMandatoryCommandClasses());
				if (deviceClass.getBasicMapping() != 0) {
					basicMapping = deviceClass.getBasicMapping();
				}
			}

			if (!listeningDevice) {
				WakeUp wu = (WakeUp) ccManager
						.getCommandClass(WakeUp.COMMAND_CLASS_ID);
				if (wu != null) {
					wu.setInstance((byte) 1);
				}
			}

			Basic cc = (Basic) ccManager
					.getCommandClass(Basic.COMMAND_CLASS_ID);
			if (cc != null) {
				cc.setMapping(basicMapping);
			}
		}
	}

	public boolean isController() {
		return (basicId == 0x01 || basicId == 0x02)
				&& (genericId == 0x01 || genericId == 0x02);
	}

	public boolean isListeningDevice() {
		return listeningDevice;
	}

	public boolean isFrequentListeningDevice() {
		return frequentListeningDevice;
	}

	public boolean isBeamingDevice() {
		return beamingDevice;
	}

	public boolean isRoutingDevice() {
		return routingDevice;
	}

	public boolean isSecurityDevice() {
		return securityDevice;
	}

	public boolean isNodeAlive() {
		return nodeAlive;
	}

	public boolean isProtocolInfoReceived() {
		return protocolInfoReceived;
	}

	public boolean isNodeInfoSupported() {
		return nodeInfoSupported;
	}

	public int getMaxBaudRate() {
		return maxBaudRate;
	}

	public byte getVersion() {
		return version;
	}

	public byte getSecurity() {
		return (byte) (isSecurityDevice() ? 0x01 : 0x00);
	}

	public byte getBasicDeviceClassID() {
		return basicId;
	}

	public byte getGenericDeviceClassID() {
		return genericId;
	}

	public byte getSpecificDeviceClassID() {
		return specificId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setNodeInfoSupported(boolean supported) {
		this.nodeInfoSupported = supported;
	}

	public void setNodeAlive(boolean alive) {
		nodeAlive = alive;
		if (alive) {
			error = 0;
			if (queryStage != QueryStage.Complete) {
				queryRetries = 0;
				advanceQueries();
			}
		} else {
			if (queryStage != QueryStage.Complete) {
				queue.sendQueryStageComplete(nodeId, QueryStage.Complete);
			}
		}
		if (listener != null) {
			listener.onNodeAliveListener(alive);
		}
	}

	public int incErrorCount() {
		return ++error;
	}

	// -----------------------------------------------------------------------------------------
	// Device Naming Info Methods
	// -----------------------------------------------------------------------------------------
	public String getManufacturerName() {
		return manufacturerName;
	}

	public String getProductName() {
		return productName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getLocation() {
		return location;
	}

	public String getManufacturerId() {
		return manufacturerId;
	}

	public String getProductType() {
		return productType;
	}

	public String getProductId() {
		return productId;
	}

	public boolean getManufacturerReceived() {
		return manufacturerReceived;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setManufacturerId(String manufacturerId) {
		this.manufacturerId = manufacturerId;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public void setManufacturerReceived(boolean received) {
		this.manufacturerReceived = received;
	}

	// -----------------------------------------------------------------------------------------
	// Node Info Methods
	// -----------------------------------------------------------------------------------------
	public void updateNodeInfo(byte[] data, byte length) {
		if (!nodeInfoReceived) {
			boolean afterMark = false;

			for (int i = 0; i < length; i++) {
				if (data[i] == (byte) 0xEF) {
					afterMark = true;
				} else {
					if (CommandClassManager.isSupported(data[i])) {
						CommandClass cc = ccManager.addCommandClass(data[i]);
						if (cc != null) {
							if (afterMark) {
								cc.setAfterMark();
							}
							cc.setInstance((byte) 1);
						}
					}
				}
			}

			setStaticRequest();
			nodeInfoReceived = true;
		} else {
			setQueryStage(QueryStage.Dynamic);
		}
	}

	private void setStaticRequest() {
		byte request = 0;

		if (ccManager.getCommandClass(MultiInstance.COMMAND_CLASS_ID) != null) {
			request |= CommandClass.STATIC_REQUEST_INSTANCES;
		}

		if (ccManager.getCommandClass(Version.COMMAND_CLASS_ID) != null) {
			request |= CommandClass.STATIC_REQUEST_INSTANCES;
		}

		if (request != 0) {
			for (Entry<Byte, CommandClass> entry : ccManager
					.getCommandClassMap().entrySet()) {
				entry.getValue().setStaticRequest(request);
			}
			setQueryStage(QueryStage.ManSpec2);
		}
	}

	public void applicationCommandHandler(byte[] data) {
		CommandClass cc = ccManager.getCommandClass(data[5]);
		if (cc != null) {
			// log.add("APP CMD HANDLER: " + HexDump.dumpHexString(data) +
			// " ---" + HexDump.dumpHexString(Arrays.copyOfRange(data, 6,
			// data.length)));
			cc.handleMsg(Arrays.copyOfRange(data, 6, data.length), data[4]);
		} else {
		}
	}

	// -----------------------------------------------------------------------------------------
	// Neighbors Methods
	// -----------------------------------------------------------------------------------------
	public int getNeighbors(byte[] temp) {
		int numNeighbors = 0;
		if (queryStage.ordinal() < QueryStage.Session.ordinal()) {
			temp = null;
			return 0;
		}

		for (int i = 0; i < SafeCast.toInt(Defs.NUM_NODE_BITFIELD_BYTES); i++) {
			for (char mask = 0x80; mask != 0; mask >>= 1) {
				if ((neighbors[i] & mask) != 0) {
					numNeighbors++;
				}
			}
		}

		if (numNeighbors == 0) {
			temp = null;
			return 0;
		}

		byte[] temp2 = new byte[numNeighbors];
		int index = 0;
		for (int i = 0; i < SafeCast.toInt(Defs.NUM_NODE_BITFIELD_BYTES); i++) {
			for (int j = 0; j < 8; j++) {
				if ((neighbors[i] & (0x01 << j)) != 0) {
					temp2[index++] = (byte) ((i << 3) + j + 1);
				}
			}
		}

		temp = temp2;
		return numNeighbors;
	}

	public byte[] getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(byte[] data) {
		this.neighbors = data;
	}

	// -----------------------------------------------------------------------------------------
	// Query Info Methods
	// -----------------------------------------------------------------------------------------
	public void advanceQueries() {
		boolean addQSC = false;
		Msg msg = null;

		while (nodeAlive && !queryPending) {
			log.add(String.format("QueryStage: [Node %d] %s",
					SafeCast.toInt(nodeId), queryStage.name()));
			switch (queryStage) {
			case None:
				queryStage = QueryStage.ProtocolInfo;
				queryRetries = 0;
				break;
			case ProtocolInfo:
				if (!protocolInfoReceived) {
					msg = new Msg(nodeId, Defs.REQUEST,
							Defs.FUNC_ID_ZW_GET_NODE_PROTOCOL_INFO, false);
					msg.append(nodeId);
					queue.sendMsg(msg, QueuePriority.Query);
					queryPending = true;
					addQSC = true;
				} else {
					queryStage = QueryStage.Probe1;
					queryRetries = 0;
				}
				break;
			case Probe1:
				NoOperation noop1 = (NoOperation) ccManager
						.getCommandClass(NoOperation.COMMAND_CLASS_ID);
				if (nodeId != primaryId && noop1 != null) {
					noop1.set(true);
					queryPending = true;
					addQSC = true;
				} else {
					queryStage = QueryStage.WakeUp;
					queryRetries = 0;
				}
				break;
			case WakeUp:
				WakeUp wu = (WakeUp) ccManager
						.getCommandClass(WakeUp.COMMAND_CLASS_ID);
				if (wu != null && !isController()
						&& isFrequentListeningDevice()) {
					wu.init();
					queryPending = true;
					addQSC = true;
				} else {
					queryStage = QueryStage.ManSpec1;
					queryRetries = 0;
				}
				break;
			case ManSpec1:
				if (nodeId == primaryId) {
					Controller cont = (Controller) this;
					String configPath = ManufacturerSpecific.setProductDetails(
							cont, cont.getManufacturerShortId(),
							cont.getProductShortType(),
							cont.getProductShortId());
					if (configPath.length() > 0) {
						xml.readProduct(this, configPath);
					}
					queryStage = QueryStage.NodeInfo;
					queryRetries = 0;
				} else {
					ManufacturerSpecific ms = (ManufacturerSpecific) ccManager
							.getCommandClass(ManufacturerSpecific.COMMAND_CLASS_ID);
					if (ms != null) {
						queryPending = ms.requestState(
								CommandClass.REQUEST_FLAG_STATIC, (byte) 1,
								QueuePriority.Query);
						addQSC = queryPending;
					}
					if (!queryPending) {
						queryStage = QueryStage.NodeInfo;
						queryRetries = 0;
					}
				}
				break;
			case NodeInfo:
				if (!nodeInfoReceived && nodeInfoSupported) {
					msg = new Msg(nodeId, Defs.REQUEST,
							Defs.FUNC_ID_ZW_REQUEST_NODE_INFO, false, true,
							Defs.FUNC_ID_ZW_APPLICATION_UPDATE);
					msg.append(nodeId);
					queue.sendMsg(msg, QueuePriority.Query);
					queryPending = true;
					addQSC = true;
				} else {
					queryStage = QueryStage.ManSpec2;
					queryRetries = 0;
				}
				break;
			case ManSpec2:
				if (!manufacturerReceived) {
					ManufacturerSpecific ms = (ManufacturerSpecific) ccManager
							.getCommandClass(ManufacturerSpecific.COMMAND_CLASS_ID);
					if (ms != null) {
						queryPending = ms.requestState(
								CommandClass.REQUEST_FLAG_STATIC, (byte) 1,
								QueuePriority.Query);
						addQSC = queryPending;
					}
					if (!queryPending) {
						queryStage = QueryStage.Version;
						queryRetries = 0;
					}
				} else {
					ManufacturerSpecific ms = (ManufacturerSpecific) ccManager
							.getCommandClass(ManufacturerSpecific.COMMAND_CLASS_ID);
					if (ms != null) {
						xml.reloadConfigManufacturer(this);
					}
					queryStage = QueryStage.Version;
					queryRetries = 0;
				}
				break;
			case Version:
				if (requestAllVersion()) {
					queryPending = true;
					addQSC = true;
				}
				if (!queryPending) {
					queryStage = QueryStage.Instances;
					queryRetries = 0;
				}
				break;
			case Instances:
				MultiInstance micc = (MultiInstance) ccManager
						.getCommandClass(MultiInstance.COMMAND_CLASS_ID);
				if (micc != null) {
					queryPending = micc.requestInstances();
					addQSC = queryPending;
				}
				if (!queryPending) {
					queryStage = QueryStage.Static;
					queryRetries = 0;
				}
				break;
			case Static:
				queryPending = requestStaticValues();
				addQSC = queryPending;
				if (!queryPending) {
					queryStage = QueryStage.Associations;
					queryRetries = 0;
				}
				break;
			case Probe2:
				NoOperation noop2 = (NoOperation) ccManager
						.getCommandClass(NoOperation.COMMAND_CLASS_ID);
				if (nodeId != primaryId && noop2 != null) {
					noop2.set(true);
					queryPending = true;
					addQSC = true;
				} else {
					queryStage = QueryStage.Associations;
					queryRetries = 0;
				}
				break;
			case Associations:
				Association acc = (Association) ccManager
						.getCommandClass(Association.COMMAND_CLASS_ID);
				if (acc != null) {
					acc.requestAllGroup(0);
					queryPending = true;
					addQSC = true;
				} else {
					queryStage = QueryStage.Neighbors;
					queryRetries = 0;
				}
				break;
			case Neighbors:
				if (nodeId == primaryId) {
					((Controller) this).requestNodeNeighbors(nodeId, 0);
				} else {
					primaryController.requestNodeNeighbors(nodeId, 0);
				}
				queryPending = true;
				addQSC = true;
				break;
			case Session:
				queryPending = requestSessionValues();
				addQSC = queryPending;
				if (!queryPending) {
					queryStage = QueryStage.Dynamic;
					queryRetries = 0;
				}
				break;
			case Dynamic:
				queryPending = requestDynamicValues();
				addQSC = queryPending;
				if (!queryPending) {
					queryStage = QueryStage.Config;
					queryRetries = 0;
				}
				break;
			case Config:
				if (queryConfiguration) {
					if (requestAllConfiguration(0)) {
						queryPending = true;
						addQSC = true;
					}
					queryConfiguration = false;
				}
				if (!queryPending) {
					queryStage = QueryStage.Complete;
					queryRetries = 0;
				}
				break;
			case Complete:
				queue.sendQueryStageComplete(nodeId, queryStage);
				return;
			default:
				break;
			}
		}

		if (addQSC && nodeAlive) {
			queue.sendQueryStageComplete(nodeId, queryStage);
		}
	}

	public QueryStage getQueryStage() {
		return queryStage;
	}

	public void setQueryStage(QueryStage stage) {
		setQueryStage(stage, true);
	}

	public void setQueryStage(QueryStage stage, boolean advance) {
		if (stage.ordinal() < queryStage.ordinal()) {
			queryStage = stage;
			queryPending = false;
			if (QueryStage.Config == stage) {
				queryConfiguration = true;
			}
		}
		if (advance) {
			advanceQueries();
		}
	}

	public void queryStageRetry(QueryStage stage) {
		queryStageRetry(stage, (byte) 0);
	}

	public void queryStageRetry(QueryStage stage, byte maxAttempts) {
		if (stage != queryStage) {
			return;
		}
		queryPending = false;
		if (maxAttempts != 0 && (++queryRetries >= maxAttempts)) {
			queryRetries = 0;
			if (queryStage != QueryStage.Probe1
					&& queryStage != QueryStage.Probe2) {
				queryStage = QueryStage.values()[queryStage.ordinal() + 1];
			}
		}
		queue.retryQueryStageComplete(nodeId, queryStage);
	}

	public void queryStageComplete(QueryStage stage) {
		if (stage != queryStage) {
			return;
		}
		if (queryStage != QueryStage.Complete) {
			queryPending = false;
			queryStage = QueryStage.values()[queryStage.ordinal() + 1];
			if (queryStage == QueryStage.Probe2) {
				queryStage = QueryStage.values()[queryStage.ordinal() + 1];
			}
			queryRetries = 0;
		}
	}

	public boolean allQueriesCompleted() {
		return QueryStage.Complete == queryStage;
	}

	// -----------------------------------------------------------------------------------------
	// Actions Methods
	// -----------------------------------------------------------------------------------------
	public void setOn() {
		Basic cc = (Basic) ccManager.getCommandClass(Basic.COMMAND_CLASS_ID);
		if (cc != null) {
			cc.set((byte) 255);
		}
	}

	public void setOff() {
		Basic cc = (Basic) ccManager.getCommandClass(Basic.COMMAND_CLASS_ID);
		if (cc != null) {
			cc.set((byte) 0);
		}
	}

	public void setLevel(byte level) {
		int lvl = SafeCast.toInt(level);
		byte adjustlevel = (byte) (lvl > 99 && lvl < 256 ? 99 : lvl);
		Basic cc = (Basic) ccManager.getCommandClass(Basic.COMMAND_CLASS_ID);
		if (cc != null) {
			cc.set(adjustlevel);
		}
	}

	// -----------------------------------------------------------------------------------------
	// Request Values Methods
	// -----------------------------------------------------------------------------------------
	public boolean requestDynamicValues() {
		boolean res = false;
		for (Entry<Byte, CommandClass> entry : ccManager.getCommandClassMap()
				.entrySet()) {
			CommandClass cc = entry.getValue();
			if (!cc.isAfterMark()) {
				res |= cc.requestStateForAllInstances(
						CommandClass.REQUEST_FLAG_DYNAMIC, QueuePriority.Send);
			}
		}
		return res;
	}

	public boolean requestSessionValues() {
		boolean res = false;
		for (Entry<Byte, CommandClass> entry : ccManager.getCommandClassMap()
				.entrySet()) {
			CommandClass cc = entry.getValue();
			if (!cc.isAfterMark()) {
				res |= cc.requestStateForAllInstances(
						CommandClass.REQUEST_FLAG_SESSION, QueuePriority.Query);
			}
		}
		return res;
	}

	public boolean requestStaticValues() {
		boolean res = false;
		for (Entry<Byte, CommandClass> entry : ccManager.getCommandClassMap()
				.entrySet()) {
			CommandClass cc = entry.getValue();
			if (!cc.isAfterMark()) {
				res |= cc.requestStateForAllInstances(
						CommandClass.REQUEST_FLAG_STATIC, QueuePriority.Query);
			}
		}
		return res;
	}

	public boolean requestAllVersion() {
		boolean res = false;
		Version vcc = (Version) ccManager
				.getCommandClass(Version.COMMAND_CLASS_ID);
		if (vcc != null) {
			for (Entry<Byte, CommandClass> entry : ccManager
					.getCommandClassMap().entrySet()) {
				CommandClass cc = entry.getValue();
				if (cc.getMaxVersion() > 1) {
					res |= vcc.requestCommandClassVersion(cc);
				}
			}
		}
		return res;
	}

	public boolean requestAllConfiguration(int requestFlags) {
		boolean res = false;
		Configuration cc = (Configuration) ccManager
				.getCommandClass(Configuration.COMMAND_CLASS_ID);
		if (cc != null) {
			for (Entry<Integer, Value> entry : values.entrySet()) {
				Value val = entry.getValue();
				if (val.getId().getCommandClassId() == Configuration.COMMAND_CLASS_ID
						&& !val.isWriteOnly()) {
					res |= cc.requestValue(requestFlags,
							val.getId().getIndex(), (byte) 1,
							QueuePriority.Send);
				}
			}
		}
		return res;
	}

	public void requestConfigurationParam(byte param) {
		Configuration cc = (Configuration) ccManager
				.getCommandClass(Configuration.COMMAND_CLASS_ID);
		if (cc != null) {
			cc.requestValue(0, param, (byte) 1, QueuePriority.Send);
		}
	}

	public boolean setConfigurationParam(byte param, int value, byte size) {
		Configuration cc = (Configuration) ccManager
				.getCommandClass(Configuration.COMMAND_CLASS_ID);
		if (cc != null) {
			Value val = cc.getValue((byte) 1, param);
			if (val != null) {
				switch (val.getId().getType()) {
				case BOOL:
					ValueBool valueBool = (ValueBool) val;
					valueBool.set(value != 0);
					break;
				case BYTE:
					ValueByte valueByte = (ValueByte) val;
					valueByte.set((byte) value);
					break;
				case SHORT:
					ValueShort valueShort = (ValueShort) val;
					valueShort.set((short) value);
					break;
				case INT:
					ValueInt valueInt = (ValueInt) val;
					valueInt.set(value);
					break;
				case LIST:
					ValueList valueList = (ValueList) val;
					valueList.setByValue(value);
					break;
				default:
					break;
				}
				return true;
			}

			cc.set(param, value, size);
			return true;
		}
		return false;
	}
}
