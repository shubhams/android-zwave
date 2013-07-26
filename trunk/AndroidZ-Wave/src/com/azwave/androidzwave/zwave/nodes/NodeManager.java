package com.azwave.androidzwave.zwave.nodes;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.commandclass.WakeUp;
import com.azwave.androidzwave.zwave.items.ControllerCmd;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem;
import com.azwave.androidzwave.zwave.items.QueueManager;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerError;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;
import com.azwave.androidzwave.zwave.items.QueueItem.QueueCommand;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;
import com.azwave.androidzwave.zwave.utils.Log;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.utils.XMLManager;

public class NodeManager {

	private static final String[] LIBRARY_TYPE_NAMES = { "Unknown",
			"Static Controller", "Controller", "Enhanced Slave", "Slave",
			"Installer", "Routing Slave", "Bridge Controller",
			"Device Under Test" };

	private int size = 0, homeId = 0;

	private String libraryVersion = "";
	private String libraryTypeName = "";
	private byte libraryType = 0;
	private byte sucNodeId = 0;

	private boolean initAllNodes = false;
	private boolean allNodesQueried = false;
	private boolean awakeNodesQueried = false;

	private Vector<Node> nodes;
	private Log logs;
	private XMLManager xml;
	private QueueManager queue;
	private NodeListener listener;

	private Controller primaryController;

	public NodeManager(QueueManager queue, XMLManager xmlmanager, Log log) {
		this.nodes = new Vector<Node>();
		nodes.setSize(Defs.MAX_TOTAL_NODES);

		this.logs = log;
		this.xml = xmlmanager;
		this.queue = queue;
	}

	// -----------------------------------------------------------------------------------------
	// Initialization Method
	// -----------------------------------------------------------------------------------------
	public void initAllNodes() {
		nodes.clear();
		nodes.setSize(Defs.MAX_TOTAL_NODES);

		init();
	}

	public NodeListener getListener() {
		return listener;
	}

	public void setListener(NodeListener listener) {
		this.listener = listener;
	}

	public void init() {
		queue.sendMsg(Msg.createZWaveGetVersion((byte) 0xFF),
				QueuePriority.Command);
		queue.sendMsg(Msg.createZWaveMemoryGetId((byte) 0xFF),
				QueuePriority.Command);
		queue.sendMsg(Msg.createZWaveGetControllerCapabilities((byte) 0xFF),
				QueuePriority.Command);
		queue.sendMsg(Msg.createZWaveSerialAPIGetCapabilities((byte) 0xFF),
				QueuePriority.Command);
		queue.sendMsg(Msg.createZWaveGetSUCNodeId((byte) 0xFF),
				QueuePriority.Command);
	}

	// -----------------------------------------------------------------------------------------
	// Node Management Method
	// -----------------------------------------------------------------------------------------
	public Node addNode(byte nodeId) {
		return addNode(homeId, nodeId);
	}

	public Node addNode(int homeId, byte nodeId) {
		if (!isNodeExist(nodeId)) {
			size++;
			logs.add(String.format("Adding node: %d", SafeCast.toInt(nodeId)));
		}

		Node node;
		if (nodesCount() == 1) {
			primaryController = new Controller(homeId, nodeId, queue, xml, logs);
			primaryController.setLibraryType(libraryType);
			primaryController.setLibraryTypeName(libraryTypeName);
			primaryController.setLibraryVersion(libraryVersion);
			primaryController.setNodeListener(listener);
			setNode(nodeId, primaryController);

			node = primaryController;
		} else {
			node = new Node(homeId, nodeId, primaryController, queue, xml, logs);
			setNode(nodeId, node);
			node.setNodeListener(listener);
			node.setQueryStage(QueryStage.ProtocolInfo);
		}

		if (listener != null) {
			listener.onNodeAddedToList();
		}
		return node;
	}

	public Node removeNode(byte nodeId) {
		if (isNodeExist(nodeId)) {
			size--;
			logs.add(String.format("Removing node: %d", SafeCast.toInt(nodeId)));
		}
		Node rem = getNode(nodeId);
		setNode(nodeId, null);
		if (listener != null) {
			listener.onNodeRemovedToList();
		}
		return rem;
	}

	public ArrayList<Node> toArrayList() {
		ArrayList<Node> arr_nodes = new ArrayList<Node>();
		for (int i = 0; i < Defs.MAX_TOTAL_NODES; i++) {
			if (nodes.get(i) != null) {
				arr_nodes.add(nodes.get(i));
			}
		}
		return arr_nodes;
	}

	public int nodesCount() {
		return size;
	}

	public boolean isNodeExist(byte nodeId) {
		return nodes.get(SafeCast.toInt(nodeId)) != null;
	}

	public Node[] toArray() {
		return nodes.toArray(new Node[Defs.MAX_TOTAL_NODES]);
	}

	// -----------------------------------------------------------------------------------------
	// Node (Alive) Management Method
	// -----------------------------------------------------------------------------------------
	public ArrayList<Node> toArrayListNodeAlive() {
		ArrayList<Node> arr_nodes = new ArrayList<Node>();
		for (int i = 0; i < Defs.MAX_TOTAL_NODES; i++) {
			if (nodes.get(i) != null && nodes.get(i).isNodeAlive()) {
				arr_nodes.add(nodes.get(i));
			}
		}
		return arr_nodes;
	}

	public boolean moveMsgToWakeUpQueue(byte targetNodeId, boolean move) {
		Node node = getNode(targetNodeId);
		if (node != null && !node.isListeningDevice()
				&& !node.isFrequentListeningDevice()
				&& primaryController != null
				&& targetNodeId != primaryController.getNodeId()) {
			WakeUp wu = (WakeUp) node.getCommandClassManager().getCommandClass(
					WakeUp.COMMAND_CLASS_ID);
			if (wu != null) {
				wu.setAwake(false);
				if (move) {
					if (queue.getCurrentControllerCmd() != null) {
						queue.removeCurrentMsg();
					}
					if (queue.getCurrentMsg() != null) {
						Msg msg = queue.getCurrentMsg();
						if (targetNodeId == SafeCast.nodeIdFromMsg(msg)) {
							if (!msg.isWakeUpNoMoreInformationCommand()
									&& !msg.isNoOperation()) {
								QueueItem item = new QueueItem();
								item.setCommand(QueueCommand.SendMessage);
								item.setMsg(msg);
								wu.queueItem(item);
							}
							queue.removeCurrentMsg();
						}
					}

					for (int i = 0; i < queue.size(); ++i) {
						Iterator<QueueItem> iter = queue.getQueue().iterator();
						while (iter.hasNext()) {
							boolean remove = false;
							QueueItem it = iter.next();
							if (it.getCommand() == QueueCommand.SendMessage
									&& targetNodeId == SafeCast
											.nodeIdFromMsg(it.getMsg())) {
								if (!it.getMsg()
										.isWakeUpNoMoreInformationCommand()
										&& !it.getMsg().isNoOperation()) {
									wu.queueItem(it);
								} else {
									it.setMsg(null);
								}
								remove = true;
							} else if (it.getCommand() == QueueCommand.QueryStageComplete
									&& targetNodeId == SafeCast
											.nodeIdFromMsg(it.getMsg())) {
								wu.queueItem(it);
								remove = true;
							} else if (it.getCommand() == QueueCommand.Controller
									&& targetNodeId == SafeCast
											.nodeIdFromMsg(it.getMsg())) {
								wu.queueItem(it);
								remove = true;
							}

							if (remove) {
								iter.remove();
							}
						}
					}

					if (queue.getCurrentControllerCmd() != null) {
						primaryController
								.updateControllerState(ControllerState.Sleeping);
						queue.sendControllerCommand(queue
								.getCurrentControllerCmd());
					}
					return true;
				}
			}
		}

		return false;
	}

	public int nodesAliveCount() {
		return toArrayListNodeAlive().size();
	}

	// -----------------------------------------------------------------------------------------
	// Protocol Info Variable
	// -----------------------------------------------------------------------------------------
	public void setHomeId(int homeId) {
		this.homeId = homeId;
	}

	public byte getPrimaryNodeId() {
		return primaryController.getNodeId();
	}

	public Controller getPrimaryController() {
		return primaryController;
	}

	public void setNode(byte nodeId, Node node) {
		nodes.set(SafeCast.toInt(nodeId), node);
	}

	public Node getNode(byte nodeId) {
		return nodes.get(SafeCast.toInt(nodeId));
	}

	public boolean isInitAllNodes() {
		return initAllNodes;
	}

	public void setInitAllNodes(boolean init) {
		this.initAllNodes = init;
	}

	public boolean isAllNodesQueried() {
		return allNodesQueried;
	}

	public void setAllNodesQueried(boolean all) {
		this.allNodesQueried = all;
	}

	public boolean isAwakeNodesQueried() {
		return awakeNodesQueried;
	}

	public void setAwakeNodesQueried(boolean awake) {
		this.awakeNodesQueried = awake;
	}

	// -----------------------------------------------------------------------------------------
	// Response Methods
	// -----------------------------------------------------------------------------------------
	public void handleGetVersionResponse(byte[] data) {
		StringBuilder str = new StringBuilder();

		for (int i = 2; i < data.length; i++) {
			if (data[i] != 0) {
				str.append((char) data[i]);
			} else {
				libraryType = data[i + 1];
				break;
			}
		}

		libraryVersion = str.toString();
		if (libraryType < 9) {
			libraryTypeName = LIBRARY_TYPE_NAMES[libraryType];
		}

		logs.add(String.format(
				"FUNC_ID_ZW_GET_VERSION : %s Library, Z-Wave %s",
				libraryTypeName, libraryVersion));
	}

	public void handleMemoryGetIdResponse(byte[] data) {
		homeId = SafeCast.toInt(data[2]) << 24 | SafeCast.toInt(data[3]) << 16
				| SafeCast.toInt(data[4]) << 8 | SafeCast.toInt(data[5]);
		addNode(homeId, data[6]);

		logs.add(String.format(
				"FUNC_ID_ZW_MEMORY_GET_ID : HomeId = 0x%08x, NodeId = %d",
				homeId, data[6]));
	}

	public void handleGetSUCNodeIdResponse(byte[] data) {
		sucNodeId = data[2];
		logs.add(String.format("FUNC_ID_ZW_GET_SUC_NODE_ID -- SUC Node Id: %d", SafeCast.toInt(sucNodeId)));
		if (sucNodeId == 0) {/* di OpenZ-Wave ada source tapi di comments */
		}
	}

	public void handleGetControllerCapabilitiesResponse(byte[] data) {
		primaryController.setControllerCapabilities(data[2]);
		String temp = "FUNC_ID_ZW_GET_CONTROLLER_CAPABILITIES -- ";
		if ((data[2] & Controller.CONTROLLER_CAPS_SIS) != 0) {
			temp += " SUC";
		} else {
			temp += " Not SUC";
		}
		logs.add(temp);
	}

	public void handleGetSerialAPICapabilitiesResponse(byte[] data) {
		primaryController.setSerialAPIVersion(new byte[] { data[2], data[3] });
		primaryController.setManufacturerShortId((short) (SafeCast
				.toShort(data[4]) << 8 | SafeCast.toShort(data[5])));
		primaryController.setProductShortType((short) (SafeCast
				.toShort(data[6]) << 8 | SafeCast.toShort(data[7])));
		primaryController
				.setProductShortId((short) (SafeCast.toShort(data[8]) << 8 | SafeCast
						.toShort(data[9])));
		primaryController.setApiMask(Arrays.copyOfRange(data, 10, 32));

		logs.add(String
				.format("FUNC_ID_SERIAL_API_GET_CAPABILITIES -- Serial API: v%d.%d,"
						+ " Man. Id = 0x%04x, Prod. Type = 0x%04x, Prod. Id = 0x%04x",
						primaryController.getSerialAPIVersion()[0],
						primaryController.getSerialAPIVersion()[1],
						primaryController.getManufacturerShortId(),
						primaryController.getProductShortType(),
						primaryController.getProductShortId()));

		Msg msg;
		if (primaryController.isBridgeController()) {
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_GET_VIRTUAL_NODES, false);
			queue.sendMsg(msg, QueuePriority.Command);
		} else if (primaryController
				.isAPICallSupported(Defs.FUNC_ID_ZW_GET_RANDOM)) {
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_GET_RANDOM, false);
			msg.append((byte) 32);
			queue.sendMsg(msg, QueuePriority.Command);
		}

		msg = new Msg((byte) 0xFF, Defs.REQUEST,
				Defs.FUNC_ID_SERIAL_API_GET_INIT_DATA, false);
		queue.sendMsg(msg, QueuePriority.Command);

		msg = new Msg((byte) 0xFF, Defs.REQUEST,
				Defs.FUNC_ID_SERIAL_API_APPL_NODE_INFORMATION, false, false);
		msg.appends(new byte[] { Defs.APPLICATION_NODEINFO_LISTENING, 0x02,
				0x01, 0x01, 0x2B });
		//msg.appends(new byte[] { Defs.APPLICATION_NODEINFO_LISTENING, 0x02,
		//		0x01, 0x00 });
		queue.sendMsg(msg, QueuePriority.Command);
	}

	public void handleSerialAPIGetInitDataResponse(byte[] data) {
		if (!initAllNodes) { /* load jika ada settings */
		} else { /* driver & setting hilang */
		}

		logs.add("FUNC_ID_SERIAL_API_GET_INIT_DATA");
		primaryController.setInitVersion(data[2]);
		primaryController.setInitCapabilities(data[3]);

		if (data[4] == Defs.NUM_NODE_BITFIELD_BYTES) {
			for (int i = 0; i < Defs.NUM_NODE_BITFIELD_BYTES; ++i) {
				for (int j = 0; j < 8; ++j) {
					byte nodeId = (byte) ((i * 8) + j + 1);
					if ((data[i + 5] & (0x01 << j)) != 0) {
						Node node = getNode(nodeId);
						if (node != null) {
							if (node.getNodeId() == primaryController
									.getNodeId()) {
								node.setQueryStage(QueryStage.ProtocolInfo);
							}
							if (!initAllNodes) {
								node.setQueryStage(QueryStage.Probe1);
							}
						} else {
							addNode(nodeId);
						}
					} else if (getNode(nodeId) != null) {
						removeNode(nodeId);
					}
				}
			}
		}

		initAllNodes = true;
	}

	public void handleGetRoutingInfoResponse(byte[] data) {
		Node node = getNode(SafeCast.nodeIdFromMsg(queue.getCurrentMsg()));
		if (node != null) {
			node.setNeighbors(Arrays.copyOfRange(data, 2, 29));
			logs.add(String.format("FUNC_ID_ZW_GET_ROUTING_INFO -- Neighbors of node %d are:",
					node.getNodeId()));
			boolean neighbors = false;
			for (int i = 0; i < 29; i++) {
				for (int j = 0; j < 8; j++) {
					if ((data[2 + i] & (0x01 << j)) != 0) {
						logs.add(String.format("--- Node %d", (i << 3) + j + 1));
						neighbors = true;
					}
				}
			}

			if (!neighbors) {
				logs.add(String.format("no neighbor on node %d",
						node.getNodeId()));
			}
		}
	}

	public boolean handleNetworkUpdateResponse(byte[] data) {
		boolean res = true;
		ControllerState state = ControllerState.InProgress;
		if (data[2] != 0) {
			logs.add("FUNC_ID_ZW_REQUEST_NETWORK_UPDATE --- in progress");
		} else {
			logs.add("FUNC_ID_ZW_REQUEST_NETWORK_UPDATE --- failed");
			state = ControllerState.Failed;
			res = false;
		}
		primaryController.updateControllerState(state);
		return res;
	}

	public boolean handleRemoveFailedNodeResponse(byte[] data) {
		boolean res = true;
		ControllerState state = ControllerState.InProgress;
		ControllerError error = ControllerError.None;

		if (data[2] != 0) {
			switch (data[2]) {
			case Defs.FAILED_NODE_NOT_FOUND:
				error = ControllerError.NotFound;
				break;
			case Defs.FAILED_NODE_REMOVE_PROCESS_BUSY:
				error = ControllerError.Busy;
				break;
			case Defs.FAILED_NODE_REMOVE_FAIL:
				error = ControllerError.Failed;
				break;
			case Defs.FAILED_NODE_NOT_PRIMARY_CONTROLLER:
				error = ControllerError.NotPrimary;
				break;
			default:
				break;
			}
			logs.add("FUNC_ID_ZW_REQUEST_NETWORK_UPDATE --- error");
			state = ControllerState.Failed;
			res = false;
		} else {
			logs.add("FUNC_ID_ZW_REQUEST_NETWORK_UPDATE --- in progress");
		}

		primaryController.updateControllerState(state, error);
		return res;
	}

	// -----------------------------------------------------------------------------------------
	// Request Methods
	// -----------------------------------------------------------------------------------------
	public void handleSendDataRequest(byte[] data, boolean replication) {
		byte nodeId = SafeCast.nodeIdFromMsg(queue.getCurrentMsg());
		if (data[2] != queue.getExpectedCallbackId()) {
			logs.add(String.format("Unexpected callback id: received %d != %d",
					SafeCast.toInt(data[2]),
					SafeCast.toInt(queue.getExpectedCallbackId())));
		} else {
			Node node = getNode(nodeId);
			if (node != null && queue.getCurrentMsg() != null) {
				if (data[3] != 0) {
					if (!handleErrorResponse(data[3], nodeId,
							replication ? "ZW_REPLICATION_END_DATA"
									: "ZW_SEND_DATA", !replication)) {
						if (queue.getCurrentMsg().isNoOperation()
								&& (node.getQueryStage() == QueryStage.Probe1 || node
										.getQueryStage() == QueryStage.Probe2)) {
							node.queryStageRetry(node.getQueryStage(), (byte) 3);
						}
					}
				} else {
					if (queue.getCurrentMsg()
							.isWakeUpNoMoreInformationCommand()) {
						WakeUp wu = (WakeUp) node.getCommandClassManager()
								.getCommandClass(WakeUp.COMMAND_CLASS_ID);
						if (wu != null) {
							wu.setAwake(false);
						}
					}
					if (!node.isNodeAlive()) {
						node.setNodeAlive(true);
					}
				}
			}
			queue.setExpectedCallbackId((byte) 0);
		}
	}

	public boolean handleApplicationUpdateRequest(byte[] data) {
		boolean messageRemoved = false;
		byte nodeId = data[3];
		Node node = getNode(nodeId);
		Node tnode = null;

		if (node != null && !node.isNodeAlive()) {
			node.setNodeAlive(true);
		}
		switch (data[2]) {
		case Defs.UPDATE_STATE_SUC_ID:
			logs.add(String.format("Update SUC Id node %d",
					SafeCast.toInt(nodeId)));
			sucNodeId = nodeId;
			break;
		case Defs.UPDATE_STATE_DELETE_DONE:
			logs.add(String.format("Remove node %d", SafeCast.toInt(nodeId)));
			removeNode(nodeId);
			break;
		case Defs.UPDATE_STATE_NEW_ID_ASSIGNED:
			logs.add(String.format("Add node %d", SafeCast.toInt(nodeId)));
			addNode(nodeId);
			break;
		case Defs.UPDATE_STATE_ROUTING_PENDING:
			logs.add(String.format("Routing pending node %d", nodeId));
			break;
		case Defs.UPDATE_STATE_NODE_INFO_REQ_FAILED:
			if (queue.getCurrentMsg() != null) {
				logs.add(String.format("Update failed node %d",
						SafeCast.nodeIdFromMsg(queue.getCurrentMsg())));
				tnode = getNode(SafeCast.nodeIdFromMsg(queue.getCurrentMsg()));
				if (tnode != null) {
					tnode.queryStageRetry(QueryStage.NodeInfo, (byte) 2);
					if (moveMsgToWakeUpQueue(tnode.getNodeId(), true)) {
						messageRemoved = true;
					}
				}
			}
			break;
		case Defs.UPDATE_STATE_NODE_INFO_REQ_DONE:
			logs.add(String.format("Update done node %d", nodeId));
			break;
		case Defs.UPDATE_STATE_NODE_INFO_RECEIVED:
			logs.add(String.format("Update info receive node %d", nodeId));
			if (node != null) {
				node.updateNodeInfo(Arrays.copyOfRange(data, 8, data.length),
						(byte) ((SafeCast.toInt(data[4]) - 3)));
			}
			break;
		}

		if (messageRemoved) {
			queue.removeExpectedAndACK();
		}
		return messageRemoved;
	}

	public void handleApplicationCommandHandlerRequest(byte[] data) {
		byte status = data[2];
		byte nodeId = data[3];
		byte classId = data[5];

		Node node = getNode(nodeId);
		if ((status & Defs.RECEIVE_STATUS_ROUTED_BUSY) != 0) {
		}
		if ((status & Defs.RECEIVE_STATUS_TYPE_BROAD) != 0) { /* receive 500ms */
		}

		if (node != null) {
			if (queue.getExpectedReply() == Defs.FUNC_ID_APPLICATION_COMMAND_HANDLER
					&& queue.getExpectedNodeId() == nodeId) {
				// update RTT
			} else {
				// received unsolicited
			}
		}

		if ((byte) 0x22 == classId) {
			// TODO: Test this class function or implement
		} else if ((byte) 0x21 == classId) {
			// TODO: Test this class function or implement
		} else if (node != null) {
			node.applicationCommandHandler(data);
		}
	}

	public void handleAddNodeToNetworkRequest(byte[] data) {
		commonAddNodeStatusRequestHandler(Defs.FUNC_ID_ZW_ADD_NODE_TO_NETWORK,
				data);
	}

	public void handleCreateNewPrimaryRequest(byte[] data) {
		commonAddNodeStatusRequestHandler(Defs.FUNC_ID_ZW_CREATE_NEW_PRIMARY,
				data);
	}

	public void handleControllerChangeRequest(byte[] data) {
		commonAddNodeStatusRequestHandler(Defs.FUNC_ID_ZW_CONTROLLER_CHANGE,
				data);
	}

	public void handleRemoveNodeFromNetworkRequest(byte[] data) {
		ControllerCmd cci = queue.getCurrentControllerCmd();

		if (cci == null) {
			return;
		}

		ControllerState state = cci.getControllerState();
		switch (data[3]) {
		case Defs.REMOVE_NODE_STATUS_LEARN_READY:
			logs.add("REMOVE_NODE_STATUS_LEARN_READY");
			state = ControllerState.Waiting;
			cci.setControllerCommandNode((byte) 0);
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			break;
		case Defs.REMOVE_NODE_STATUS_NODE_FOUND:
			logs.add("REMOVE_NODE_STATUS_NODE_FOUND");
			state = ControllerState.InProgress;
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			break;
		case Defs.REMOVE_NODE_STATUS_REMOVING_SLAVE:
			logs.add("REMOVE_NODE_STATUS_REMOVING_SLAVE --- Node: "
					+ String.valueOf(data[4]));
			cci.setControllerCommandNode(data[4]);
			break;
		case Defs.REMOVE_NODE_STATUS_REMOVING_CONTROLLER:
			logs.add("REMOVE_NODE_STATUS_REMOVING_CONTROLLER --- Node: "
					+ String.valueOf(data[4]));
			// mCurrentControllerCommand.mControllerCommandNode = data[4];
			cci.setControllerCommandNode(data[4]);
			if (data[4] == (byte) 0) {
				if (data[5] >= 3) {
					for (int i = 0; i < Defs.MAX_TOTAL_NODES; i++) {
						Node node = getNode((byte) i);
						synchronized (node) {
							if (node == null || primaryController == null) {
								continue;
							}
							if (node.getNodeId() == primaryController
									.getNodeId()) {
								continue;
							} // Ignore primary controller
								// See if we can match another way
							if (node.getBasicDeviceClassID() == data[6]
									&& node.getGenericDeviceClassID() == data[7]
									&& node.getSpecificDeviceClassID() == data[8]) {
								if (cci.getControllerCommandNode() != 0) {
									// TODO: Alternative controller found
								} else {
									cci.setControllerCommandNode(node
											.getNodeId());
								}
							}
						}
					}
				} else {
					// TODO: error message not enough data
				}
			} else {
				cci.setControllerCommandNode(data[4]);
			}
			break;
		case Defs.REMOVE_NODE_STATUS_DONE:
			logs.add("REMOVE_NODE_STATUS_DONE");
			state = ControllerState.Completed;
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			if (!cci.isControllerCommandDone()) {
				primaryController
						.updateControllerState(ControllerState.Completed);
				primaryController
						.addNodeStop(Defs.FUNC_ID_ZW_REMOVE_NODE_FROM_NETWORK);
				if (cci.getControllerCommandNode() == (byte) 0) {
					if (data[4] != (byte) 0) {
						cci.setControllerCommandNode(data[4]);
					}
				}

				if (cci.getControllerCommandNode() != (byte) 0
						&& cci.getControllerCommandNode() != (byte) 0xFF) {
					removeNode(cci.getControllerCommandNode());
				}
			}
			return;
		case Defs.REMOVE_NODE_STATUS_FAILED:
			logs.add("REMOVE_NODE_STATUS_FAILED");
			primaryController
					.addNodeStop(Defs.FUNC_ID_ZW_REMOVE_NODE_FROM_NETWORK);
			state = ControllerState.Failed;
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			break;
		default:
			logs.add("No Detected ...");
			break;
		}

		primaryController.updateControllerState(state);
	}

	// -----------------------------------------------------------------------------------------
	// Request Variable
	// -----------------------------------------------------------------------------------------
	// private boolean handleErrorResponse(byte error, byte nodeId, String
	// funcStr) { return handleErrorResponse(error, nodeId, funcStr, false); }
	public synchronized void checkCompletedNodeQueries() {
		if (!allNodesQueried) {
			boolean all = true;
			boolean sleepingOnly = true;
			boolean deadFound = false;

			for (int i = 0; i < nodes.size(); ++i) {
				Node node = getNode((byte) i);
				if (node != null && node.getQueryStage() != QueryStage.Complete) {
					if (!node.isNodeAlive()) {
						deadFound = true;
					} else {
						all = false;
						if (!node.isListeningDevice()) {
							sleepingOnly = false;
						}
					}
				}
			}

			if (all) {
				if (deadFound) {
					logs.add("All node complete - All Init Process Complete (With dead founded)");
				} else {
					logs.add("All node complete - All Init Process Complete");
				}
				awakeNodesQueried = true;
				allNodesQueried = true;
			} else {
				if (!awakeNodesQueried) {
					awakeNodesQueried = true;
				}
			}
		}

		if (listener != null) {
			listener.onNodeQueryStageCompleteListener();
		}

	}

	private boolean handleErrorResponse(byte error, byte nodeId, String funcStr) {
		return handleErrorResponse(error, nodeId, funcStr, false);
	}

	private boolean handleErrorResponse(byte error, byte nodeId,
			String funcStr, boolean sleepcheck) {
		Node node = getNode(nodeId);
		if (error == Defs.TRANSMIT_COMPLETE_NOROUTE) {
			logs.add(String
					.format("Error: %s failed. No route found.", funcStr));
			// if (node != null) {
			// node.setNodeAlive(false);
			// }
		} else if (error == Defs.TRANSMIT_COMPLETE_NO_ACK) {
			logs.add(String.format("Error: %s failed. No ACK found.", funcStr));
			if (queue.getCurrentMsg() != null) {
				if (moveMsgToWakeUpQueue(
						SafeCast.nodeIdFromMsg(queue.getCurrentMsg()),
						sleepcheck)) {
					return true;
				}
			}
		} else if (error == Defs.TRANSMIT_COMPLETE_FAIL) {
			logs.add(String.format("Error: %s failed. Network busy.", funcStr));
		} else if (error == Defs.TRANSMIT_COMPLETE_NOT_IDLE) {
			logs.add(String.format("Error: %s failed. Network busy.", funcStr));
		}

		if (node != null && node.incErrorCount() >= 3) {
			node.setNodeAlive(false);
		}
		return false;
	}

	private void commonAddNodeStatusRequestHandler(byte funcId, byte[] data) {
		ControllerCmd cci = queue.getCurrentControllerCmd();
		ControllerState state = ControllerState.Normal;

		if (cci != null) {
			state = cci.getControllerState();
		}
		switch (data[3]) {
		case Defs.ADD_NODE_STATUS_LEARN_READY:
			logs.add("ADD_NODE_STATUS_LEARN_READY");
			if (cci != null) {
				cci.setControllerAdded(false);
			}
			state = ControllerState.Waiting;
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			break;
		case Defs.ADD_NODE_STATUS_NODE_FOUND:
			logs.add("ADD_NODE_STATUS_NODE_FOUND");
			state = ControllerState.InProgress;
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			break;
		case Defs.ADD_NODE_STATUS_ADDING_SLAVE:
			logs.add("ADD_NODE_STATUS_ADDING_SLAVE --- Node: "
					+ String.valueOf(data[4]));
			if (cci != null) {
				cci.setControllerAdded(false);
				cci.setControllerCommandNode(data[4]);
			}
			break;
		case Defs.ADD_NODE_STATUS_ADDING_CONTROLLER:
			logs.add("ADD_NODE_STATUS_ADDING_CONTROLLER --- Node: "
					+ String.valueOf(data[4]));
			if (cci != null) {
				cci.setControllerAdded(true);
				cci.setControllerCommandNode(data[4]);
			}
			break;
		case Defs.ADD_NODE_STATUS_PROTOCOL_DONE:
			logs.add("ADD_NODE_STATUS_PROTOCOL_DONE");
			primaryController.addNodeStop(funcId);
			break;
		case Defs.ADD_NODE_STATUS_DONE:
			logs.add("ADD_NODE_STATUS_DONE");
			state = ControllerState.Completed;
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			if (cci != null && cci.getControllerCommandNode() != (byte) 0xFF) {
				addNode(cci.getControllerCommandNode());
			}
			if (funcId != Defs.FUNC_ID_ZW_ADD_NODE_TO_NETWORK && cci != null
					&& cci.isControllerAdded()) {
				initAllNodes();
			}

			break;
		case Defs.ADD_NODE_STATUS_FAILED:
			logs.add("ADD_NODE_STATUS_FAILED");
			state = ControllerState.Failed;
			if (cci != null && cci.getControllerCallback() != null) {
				cci.getControllerCallback().onAction(state, null, null);
			}
			queue.removeCurrentMsg();
			primaryController.addNodeStop(funcId);
			break;
		default:
			logs.add("No detected ...");
			break;
		}

		primaryController.updateControllerState(state);
	}
}
