package com.azwave.androidzwave.zwave;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem;
import com.azwave.androidzwave.zwave.items.QueueManager;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;
import com.azwave.androidzwave.zwave.items.QueueItem.QueueCommand;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.NodeManager;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;
import com.azwave.androidzwave.zwave.services.IOService;
import com.azwave.androidzwave.zwave.services.IOServiceListener;
import com.azwave.androidzwave.zwave.utils.HexDump;
import com.azwave.androidzwave.zwave.utils.Log;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.utils.XMLManager;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

public class Driver implements IOServiceListener {

	// -----------------------------------------------------------------------------------------
	// Driver Component Variable
	// -----------------------------------------------------------------------------------------
	private Log log;
	private NodeManager nodeManager;
	private XMLManager xmlManager;
	private QueueManager queueManager;
	private UsbSerialDriver serialDriver;
	private IOService ioService;
	private ExecutorService ioExecService;

	public Driver(QueueManager queueManager, NodeManager nodeManager,
			XMLManager xmlmanager, UsbSerialDriver serialDriver, Log log) {
		this.log = log;
		this.xmlManager = xmlmanager;
		this.serialDriver = serialDriver;
		this.queueManager = queueManager;
		this.nodeManager = nodeManager;

		this.ioExecService = Executors.newSingleThreadExecutor();
		this.ioService = new IOService(serialDriver, this, queueManager, log);
	}

	public void start() throws IOException {
		ioExecService.submit(ioService);

		writeMsg(Msg.createNAK());
		nodeManager.init();
	}

	public void close() {
		ioService.terminate();
		ioExecService.shutdown();
	}

	public NodeManager getListNode() {
		return nodeManager;
	}

	@Override
	public void onReadData(byte[] data) throws IOException {
		if (data == null || data.length == 0) {
			return;
		}
		log.add("R: " + HexDump.dumpHexString(data));

		if (data.length == 1) {
			switch (data[0]) {
			case Defs.ACK:
				queueManager.setWaitingForACK(false);
				if (queueManager.getExpectedCallbackId() == 0
						&& queueManager.getExpectedReply() == 0) {
					queueManager.removeCurrentMsg();
				}
				return;
			case Defs.NAK:
				writeMsg(Msg.createNAK());
				return;
			case Defs.CAN:
				Msg temp = queueManager.getCurrentMsg();
				if (temp != null) {
					temp.setMaxSendAttempts((byte) (temp.getMaxSendAttempts() + 1));
				}
				writeMsg(Msg.createCAN());
				return;
			}
		}

		byte[] datatemp;
		if (data.length > 2 && data[0] == Defs.ACK && data[1] == Defs.SOF) {
			queueManager.setWaitingForACK(false);
			if (queueManager.getExpectedCallbackId() == 0
					&& queueManager.getExpectedReply() == 0) {
				queueManager.removeCurrentMsg();
			}
			datatemp = Arrays.copyOfRange(data, 1, data.length);
		} else {
			datatemp = data;
		}

		if (datatemp.length > 1 && datatemp[0] == Defs.SOF) {
			byte nodeId = SafeCast.nodeIdFromStream(datatemp);
			if (nodeId == 0) {
				SafeCast.nodeIdFromMsg(queueManager.getCurrentMsg());
			}

			byte checksum = (byte) 0xFF;
			for (int i = 1; i < (datatemp.length - 1); ++i) {
				checksum ^= datatemp[i];
			}

			if (checksum == datatemp[datatemp.length - 1]) {
				writeMsg(Msg.createACK());
				processStream(Arrays.copyOfRange(datatemp, 2, datatemp.length));
			} else {
				writeMsg(Msg.createNAK());
			}
		} else {
			writeMsg(Msg.createNAK());
		}
	}

	@Override
	public void onWriteData() throws IOException {
		QueueItem item = queueManager.peek();
		if (item == null) {
			return;
		}

		if (item.getCommand() == QueueCommand.SendMessage) {
			queueManager.setCurrentMsg(item.getMsg());
			queueManager.poll();
			writeMsg();
		}

		if (item.getCommand() == QueueCommand.QueryStageComplete) {
			queueManager.setCurrentMsg(null);
			QueryStage stage = item.getQueryStage();
			queueManager.poll();
			Node node = nodeManager.getNode(item.getNodeId());
			if (node != null) {
				if (stage != QueryStage.Complete) {
					if (!item.isRetry()) {
						log.add(String.format(
								"W: [Node %d] Query Stage Complete --- %s",
								SafeCast.toInt(node.getNodeId()), stage.name()));
						node.queryStageComplete(stage);
					}
					node.advanceQueries();
				} else {
					log.add(String.format(
							"W: [Node %d] Query Stage Complete --- %s",
							SafeCast.toInt(node.getNodeId()), stage.name()));
					nodeManager.checkCompletedNodeQueries();
				}
			}
		}

		if (item.getCommand() == QueueCommand.Controller) {
			queueManager.setCurrentControllerCmd(item.getCci());
			if (item.getCci().isControllerCommandDone()) {
				queueManager.poll();
				if (item.getCci().getControllerCallback() != null) {
					item.getCci()
							.getControllerCallback()
							.onAction(
									item.getCci().getControllerState(),
									item.getCci().getControllerReturnError(),
									item.getCci()
											.getControllerCallbackContext());
				}
				queueManager.setCurrentControllerCmd(null);
			} else if (item.getCci().getControllerState() == ControllerState.Normal) {
				nodeManager.getPrimaryController().doControllerCommand();
			} else if (item.getCci().isControllerStateChanged()) {
				if (item.getCci().getControllerCallback() != null) {
					item.getCci()
							.getControllerCallback()
							.onAction(
									item.getCci().getControllerState(),
									item.getCci().getControllerReturnError(),
									item.getCci()
											.getControllerCallbackContext());
					item.getCci().setControllerStateChanged(false);
				}
			}
		}
	}

	public void writeMsg() throws IOException {
		Msg msg = queueManager.getCurrentMsg();
		if (msg == null) {
			queueManager.removeExpectedAndACK();
			return;
		}

		msg.toArray();

		byte attemp = msg.getSendAttempts();
		byte nodeId = SafeCast.nodeIdFromMsg(msg);
		Node node = nodeManager.getNode(nodeId);

		if (attemp >= msg.getMaxSendAttempts()
				|| (node != null && !node.isNodeAlive() && !msg.isNoOperation())) {
			log.add("Msg dropped because node is dead or max attemp reached");
			queueManager.removeCurrentMsg();
			return;
		}

		if (attemp != 0) {
			msg.updateCallBackId();
		}

		msg.setSendAttempts(++attemp);
		queueManager.setExpectedCallbackId(msg.getCallbackId());
		queueManager.setExpectedCommandClassId(msg.getExpectedCommandClass());
		queueManager.setExpectedReply(msg.getExpectedReply());
		queueManager.setExpectedNodeId(SafeCast.nodeIdFromMsg(msg));
		queueManager.setWaitingForACK(true);

		writeMsg(msg);
	}

	public void writeMsg(Msg msg) throws IOException {
		serialDriver.write(msg.toArray(), Defs.BYTE_TIMEOUT);
		log.add(String.format("W: [Node %d] %s",
				SafeCast.toInt(SafeCast.nodeIdFromMsg(msg)), msg.toString()));
	}

	private void processStream(byte[] data) {
		boolean handleCallback = true;
		if (data[0] == Defs.RESPONSE) {
			switch (data[1]) {
			case Defs.FUNC_ID_SERIAL_API_GET_INIT_DATA:
				nodeManager.handleSerialAPIGetInitDataResponse(data);
				break;
			case Defs.FUNC_ID_ZW_GET_CONTROLLER_CAPABILITIES:
				nodeManager.handleGetControllerCapabilitiesResponse(data);
				break;
			case Defs.FUNC_ID_SERIAL_API_GET_CAPABILITIES:
				nodeManager.handleGetSerialAPICapabilitiesResponse(data);
				break;
			case Defs.FUNC_ID_SERIAL_API_SOFT_RESET:
				log.add("FUNC_ID_SERIAL_API_SOFT_RESET has done!");
				break;
			case Defs.FUNC_ID_ZW_SEND_DATA:
				if (data[2] != 0) {
					log.add("FUNC_ID_ZW_SEND_DATA delivered to Z-Wave Stack.");
				} else {
					log.add("FUNC_ID_ZW_SEND_DATA can't delivered to Z-Wave Stack.");
				}
				handleCallback = false;
				break;
			case Defs.FUNC_ID_ZW_GET_VERSION:
				nodeManager.handleGetVersionResponse(data);
				break;
			case Defs.FUNC_ID_ZW_GET_RANDOM:
				log.add("FUNC_ID_ZW_GET_RANDOM get random numbers: true");
				break;
			case Defs.FUNC_ID_ZW_MEMORY_GET_ID:
				nodeManager.handleMemoryGetIdResponse(data);
				break;
			case Defs.FUNC_ID_ZW_GET_NODE_PROTOCOL_INFO:
				Msg msg = queueManager.getCurrentMsg();
				if (msg != null) {
					Node node = nodeManager
							.getNode(SafeCast.nodeIdFromMsg(msg));
					if (node != null) {
						node.updateProtocolInfo(Arrays.copyOfRange(data, 2,
								data.length));
					}
				}
				break;
			case Defs.FUNC_ID_ZW_REPLICATION_SEND_DATA:
				break;
			case Defs.FUNC_ID_ZW_ASSIGN_RETURN_ROUTE:
				break;
			case Defs.FUNC_ID_ZW_DELETE_RETURN_ROUTE:
				break;
			case Defs.FUNC_ID_ZW_ENABLE_SUC:
				log.add("FUNC_ID_ZW_ENABLE_SUC has done!");
				break;
			case Defs.FUNC_ID_ZW_REQUEST_NETWORK_UPDATE:
				if (!nodeManager.handleNetworkUpdateResponse(data)) {
					queueManager.removeExpected();
					queueManager.setExpectedCallbackId(data[2]);
				}
				break;
			case Defs.FUNC_ID_ZW_SET_SUC_NODE_ID:
				log.add("FUNC_ID_ZW_SET_SUC_NODE_ID has done!");
				break;
			case Defs.FUNC_ID_ZW_GET_SUC_NODE_ID:
				nodeManager.handleGetSUCNodeIdResponse(data);
				break;
			case Defs.FUNC_ID_ZW_REQUEST_NODE_INFO:
				if (data[2] != 0) {
					log.add("FUNC_ID_ZW_REQUEST_NODE_INFO -- request success.");
				} else {
					log.add("FUNC_ID_ZW_REQUEST_NODE_INFO -- request failed.");
				}
				break;
			case Defs.FUNC_ID_ZW_REMOVE_FAILED_NODE_ID:
				if (!nodeManager.handleRemoveFailedNodeResponse(data)) {
					queueManager.removeExpected();
					queueManager.setExpectedCallbackId(data[2]);
				}
				break;
			case Defs.FUNC_ID_ZW_IS_FAILED_NODE_ID:
				break;
			case Defs.FUNC_ID_ZW_REPLACE_FAILED_NODE:
				break;
			case Defs.FUNC_ID_ZW_GET_ROUTING_INFO:
				nodeManager.handleGetRoutingInfoResponse(data);
				break;
			case Defs.FUNC_ID_ZW_R_F_POWER_LEVEL_SET:
				break;
			case Defs.FUNC_ID_ZW_READ_MEMORY:
				break;
			case Defs.FUNC_ID_SERIAL_API_SET_TIMEOUTS:
				break;
			case Defs.FUNC_ID_MEMORY_GET_BYTE:
				break;
			case Defs.FUNC_ID_ZW_GET_VIRTUAL_NODES:
				break;
			case Defs.FUNC_ID_ZW_SET_SLAVE_LEARN_MODE:
				break;
			case Defs.FUNC_ID_ZW_SEND_SLAVE_NODE_INFO:
				break;
			default:
				break;
			}
		} else if (data[0] == Defs.REQUEST) {
			switch (data[1]) {
			case Defs.FUNC_ID_APPLICATION_COMMAND_HANDLER:
				nodeManager.handleApplicationCommandHandlerRequest(data);
				break;
			case Defs.FUNC_ID_ZW_SEND_DATA:
				nodeManager.handleSendDataRequest(data, false);
				break;
			case Defs.FUNC_ID_ZW_REPLICATION_COMMAND_COMPLETE:
				break;
			case Defs.FUNC_ID_ZW_REPLICATION_SEND_DATA:
				break;
			case Defs.FUNC_ID_ZW_ASSIGN_RETURN_ROUTE:
				break;
			case Defs.FUNC_ID_ZW_DELETE_RETURN_ROUTE:
				break;
			case Defs.FUNC_ID_ZW_SEND_NODE_INFORMATION:
				break;
			case Defs.FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE:
			case Defs.FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE_OPTIONS:
				break;
			case Defs.FUNC_ID_ZW_APPLICATION_UPDATE:
				handleCallback = !nodeManager
						.handleApplicationUpdateRequest(data);
				break;
			case Defs.FUNC_ID_ZW_ADD_NODE_TO_NETWORK:
				nodeManager.handleAddNodeToNetworkRequest(data);
				break;
			case Defs.FUNC_ID_ZW_REMOVE_NODE_FROM_NETWORK:
				nodeManager.handleRemoveNodeFromNetworkRequest(data);
				break;
			case Defs.FUNC_ID_ZW_CREATE_NEW_PRIMARY:
				nodeManager.handleCreateNewPrimaryRequest(data);
				break;
			case Defs.FUNC_ID_ZW_CONTROLLER_CHANGE:
				nodeManager.handleControllerChangeRequest(data);
				break;
			case Defs.FUNC_ID_ZW_SET_LEARN_MODE:
				break;
			case Defs.FUNC_ID_ZW_REQUEST_NETWORK_UPDATE:
				break;
			case Defs.FUNC_ID_ZW_REMOVE_FAILED_NODE_ID:
				break;
			case Defs.FUNC_ID_ZW_REPLACE_FAILED_NODE:
				break;
			case Defs.FUNC_ID_ZW_SET_SLAVE_LEARN_MODE:
				break;
			case Defs.FUNC_ID_ZW_SEND_SLAVE_NODE_INFO:
				break;
			case Defs.FUNC_ID_APPLICATION_SLAVE_COMMAND_HANDLER:
				break;
			case Defs.FUNC_ID_PROMISCUOUS_APPLICATION_COMMAND_HANDLER:
				break;
			case Defs.FUNC_ID_ZW_SET_DEFAULT:
				break;
			default:
				break;
			}
		}

		if (handleCallback
				&& (queueManager.getExpectedCallbackId() != 0 || queueManager
						.getExpectedReply() != 0)) {
			if (queueManager.getExpectedCallbackId() != 0
					&& queueManager.getExpectedCallbackId() == data[2]) {
				queueManager.setExpectedCallbackId((byte) 0);
			}

			if (queueManager.getExpectedReply() != 0
					&& queueManager.getExpectedReply() == data[1]) {
				if (queueManager.getExpectedCommandClassId() != 0
						&& queueManager.getExpectedReply() == Defs.FUNC_ID_APPLICATION_COMMAND_HANDLER) {
					if (queueManager.getExpectedCallbackId() == 0
							&& queueManager.getExpectedCommandClassId() == data[5]
							&& queueManager.getExpectedNodeId() == data[3]) {
						queueManager.removeExpectedAndACK();
					}
				} else if (queueManager.isExpectedReply(data[3])) {
					queueManager.setExpectedReply((byte) 0);
					queueManager.setExpectedNodeId((byte) 0);
				}
			}

			if (!(queueManager.getExpectedCallbackId() != 0 || queueManager
					.getExpectedReply() != 0)) {
				queueManager.removeCurrentMsg();
			}
		}
	}

}
