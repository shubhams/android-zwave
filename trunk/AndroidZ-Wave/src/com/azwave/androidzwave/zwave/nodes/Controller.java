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

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.items.ControllerActionListener;
import com.azwave.androidzwave.zwave.items.ControllerCmd;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem;
import com.azwave.androidzwave.zwave.items.QueueManager;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerCommand;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerError;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;
import com.azwave.androidzwave.zwave.items.QueueItem.QueueCommand;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.utils.Log;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.utils.XMLManager;

public class Controller extends Node {

	public static final byte CONTROLLER_CAPS_SECONDARY = 0x01;
	/** < The controller is a secondary. */
	public static final byte CONTROLLER_CAPS_ONOTHERNETWORK = 0x02;
	/** < The controller is not using its default HomeID. */
	public static final byte CONTROLLER_CAPS_SIS = 0x04;
	/** < There is a SUC ID Server on the network. */
	public static final byte CONTROLLER_CAPS_REALPRIMARY = 0x08;
	/** < Controller was the primary before the SIS was added. */
	public static final byte CONTROLLER_CAPS_SUC = 0x10;
	/** < Controller is a static update controller. */

	public static final byte INIT_CAPS_SLAVE = 0x01;
	/** < */
	public static final byte INIT_CAPS_TIMERSUPPORT = 0x02;
	/** < Controller supports timers. */
	public static final byte INIT_CAPS_SECONDARY = 0x04;
	/** < Controller is a secondary. */
	public static final byte INIT_CAPS_SUC = 0x08;
	/** < Controller is a static update controller. */

	private String libraryVersion = "";
	private String libraryTypeName = "";
	private byte libraryType = 0;

	private short manufacturerId = 0;
	private short productType = 0;
	private short productId = 0;

	private byte[] serialAPIVersion = new byte[2];
	private byte[] apiMask = new byte[32];

	private byte initVersion = 0;
	private byte initCapabilities = 0;
	private byte controllerCapabilities = 0;

	public Controller(int homeId, byte nodeId, QueueManager queue,
			XMLManager xml, Log log) {
		super(homeId, nodeId, null, queue, xml, log);
	}

	// -----------------------------------------------------------------------------------------
	// Controller Command
	// -----------------------------------------------------------------------------------------
	public void requestNodeNeighbors(byte nodeId, int requestFlag) {
		if (isAPICallSupported(Defs.FUNC_ID_ZW_GET_ROUTING_INFO)) {
			Msg msg = new Msg(nodeId, Defs.REQUEST,
					Defs.FUNC_ID_ZW_GET_ROUTING_INFO, false);
			msg.appends(new byte[] { nodeId, 0, 0, 3 });
			getQueueManager().sendMsg(msg, QueuePriority.Command);
		}
	}

	// -----------------------------------------------------------------------------------------
	// Controller Public Methods
	// -----------------------------------------------------------------------------------------
	public boolean isPrimaryController() {
		return ((initCapabilities & INIT_CAPS_SECONDARY) == 0);
	}

	public boolean isStaticUpdateController() {
		return ((initCapabilities & INIT_CAPS_SUC) != 0);
	}

	public boolean isInclusionController() {
		return ((controllerCapabilities & CONTROLLER_CAPS_SIS) != 0);
	}

	public boolean isBridgeController() {
		return libraryType == 7;
	}

	public short getManufacturerShortId() {
		return manufacturerId;
	}

	public short getProductShortType() {
		return productType;
	}

	public short getProductShortId() {
		return productId;
	}

	public void setManufacturerShortId(short manufacturerId) {
		this.manufacturerId = manufacturerId;
	}

	public void setProductShortType(short productType) {
		this.productType = productType;
	}

	public void setProductShortId(short productId) {
		this.productId = productId;
	}

	public String getLibraryVersion() {
		return libraryVersion;
	}

	public String getLibraryTypeName() {
		return libraryTypeName;
	}

	public byte getLibraryType() {
		return libraryType;
	}

	public void setLibraryVersion(String libraryVersion) {
		this.libraryVersion = libraryVersion;
	}

	public void setLibraryTypeName(String libraryTypeName) {
		this.libraryTypeName = libraryTypeName;
	}

	public void setLibraryType(byte libraryType) {
		this.libraryType = libraryType;
	}

	public byte[] getSerialAPIVersion() {
		return serialAPIVersion;
	}

	public void setSerialAPIVersion(byte[] serialAPIVersion) {
		this.serialAPIVersion = serialAPIVersion;
	}

	public byte[] getApiMask() {
		return apiMask;
	}

	public void setApiMask(byte[] apiMask) {
		this.apiMask = apiMask;
	}

	public byte getInitVersion() {
		return initVersion;
	}

	public void setInitVersion(byte initVersion) {
		this.initVersion = initVersion;
	}

	public byte getControllerCapabilities() {
		return controllerCapabilities;
	}

	public void setControllerCapabilities(byte ctrCap) {
		this.controllerCapabilities = ctrCap;
	}

	public byte getInitCapabilities() {
		return initCapabilities;
	}

	public void setInitCapabilities(byte initCapabilities) {
		this.initCapabilities = initCapabilities;
	}

	public boolean isAPICallSupported(byte apinum) {
		return (apiMask[(SafeCast.toInt(apinum) - 1) >> 3] & (1 << ((byte) (SafeCast
				.toInt(apinum) - 1) & 0x07))) != 0;
	}

	public void setAPICall(byte apinum, boolean toSet) {
		if (toSet) {
			apiMask[(SafeCast.toInt(apinum) - 1) >> 3] |= (1 << ((SafeCast
					.toInt(apinum) - 1) & 0x07));
		} else {
			apiMask[(SafeCast.toInt(apinum) - 1) >> 3] &= ~(1 << ((SafeCast
					.toInt(apinum) - 1) & 0x07));
		}
	}

	// -----------------------------------------------------------------------------------------
	// Controller Commands
	// -----------------------------------------------------------------------------------------
	public boolean cancelControllerCommand() {
		ControllerCmd cci = getQueueManager().getCurrentControllerCmd();
		Msg msg;

		if (cci == null) {
			return false;
		}
		switch (cci.getControllerCommand()) {
		case AddDevice:
			cci.setControllerCommandNode((byte) 0xFF);
			addNodeStop(Defs.FUNC_ID_ZW_ADD_NODE_TO_NETWORK);
			break;
		case CreateNewPrimary:
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_CREATE_NEW_PRIMARY, true);
			msg.append(Defs.CREATE_PRIMARY_STOP);
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case ReceiveConfiguration:
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_SET_LEARN_MODE, false, false);
			msg.append((byte) 0);
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case RemoveDevice:
			cci.setControllerCommandNode((byte) 0xFF);
			addNodeStop(Defs.FUNC_ID_ZW_REMOVE_NODE_FROM_NETWORK);
			break;
		case TransferPrimaryRole:
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_CONTROLLER_CHANGE, true);
			msg.append(Defs.CONTROLLER_CHANGE_STOP);
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case ReplicationSend:
			cci.setControllerCommandNode((byte) 0xFF); // identify the fact that
			// there is no new node
			// to initialize
			addNodeStop(Defs.FUNC_ID_ZW_ADD_NODE_TO_NETWORK);
			break;
		case CreateButton:
		case DeleteButton:
			if (cci.getControllerCommandNode() != 0) {
				// sendSlaveLearnModeOff();
			}
			break;
		case None:
		case RequestNetworkUpdate:
		case RequestNodeNeighborUpdate:
		case AssignReturnRoute:
		case DeleteAllReturnRoutes:
		case RemoveFailedNode:
		case HasNodeFailed:
		case ReplaceFailedNode:
		case SendNodeInformation:
			return false;
		}

		updateControllerState(ControllerState.Cancel);
		return true;
	}

	public void doControllerCommand() {
		ControllerCmd cci = getQueueManager().getCurrentControllerCmd();
		Msg msg;

		updateControllerState(ControllerState.Starting);

		switch (cci.getControllerCommand()) {
		case AddDevice:
			if (!isPrimaryController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotPrimary);
			} else {
				msg = new Msg((byte) 0xFF, Defs.REQUEST,
						Defs.FUNC_ID_ZW_ADD_NODE_TO_NETWORK, true);
				msg.append(cci.isHighPower() ? Defs.ADD_NODE_ANY
						| Defs.OPTION_HIGH_POWER : Defs.ADD_NODE_ANY);
				getQueueManager().sendMsg(msg, QueuePriority.Command);
			}
			break;
		case CreateNewPrimary:
			if (isPrimaryController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotSecondary);
			} else if (!isStaticUpdateController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotSUC);
			} else {
				msg = new Msg((byte) 0xFF, Defs.REQUEST,
						Defs.FUNC_ID_ZW_CREATE_NEW_PRIMARY, true);
				msg.append(Defs.CREATE_PRIMARY_START);
				getQueueManager().sendMsg(msg, QueuePriority.Command);
			}
			break;
		case ReceiveConfiguration:
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_SET_LEARN_MODE, true);
			msg.append((byte) 0xFF);
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case RemoveDevice:
			if (!isPrimaryController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotPrimary);
			} else {
				msg = new Msg((byte) 0xFF, Defs.REQUEST,
						Defs.FUNC_ID_ZW_REMOVE_NODE_FROM_NETWORK, true);
				msg.append(cci.isHighPower() ? Defs.REMOVE_NODE_ANY
						| Defs.OPTION_HIGH_POWER : Defs.REMOVE_NODE_ANY);
				getQueueManager().sendMsg(msg, QueuePriority.Command);
			}
			break;
		case HasNodeFailed:
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_IS_FAILED_NODE_ID, false);
			msg.append(cci.getControllerCommandNode());
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case RemoveFailedNode:
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_REMOVE_FAILED_NODE_ID, true);
			msg.append(cci.getControllerCommandNode());
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case ReplaceFailedNode:
			msg = new Msg((byte) 0xFF, Defs.REQUEST,
					Defs.FUNC_ID_ZW_REPLACE_FAILED_NODE, true);
			msg.append(cci.getControllerCommandNode());
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case TransferPrimaryRole:
			if (!isPrimaryController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotPrimary);
			} else {
				msg = new Msg((byte) 0xFF, Defs.REQUEST,
						Defs.FUNC_ID_ZW_CONTROLLER_CHANGE, true);
				msg.append(cci.isHighPower() ? Defs.CONTROLLER_CHANGE_START
						| Defs.OPTION_HIGH_POWER : Defs.CONTROLLER_CHANGE_START);
				getQueueManager().sendMsg(msg, QueuePriority.Command);
			}
			break;
		case RequestNetworkUpdate:
			if (!isStaticUpdateController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotSUC);
			} else {
				msg = new Msg((byte) 0xFF, Defs.REQUEST,
						Defs.FUNC_ID_ZW_REQUEST_NETWORK_UPDATE, true);
				getQueueManager().sendMsg(msg, QueuePriority.Command);
			}
			break;
		case RequestNodeNeighborUpdate:
			if (!isPrimaryController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotPrimary);
			} else {
				boolean opts = isAPICallSupported(Defs.FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE_OPTIONS);
				if (opts) {
					msg = new Msg(
							cci.getControllerCommandNode(),
							Defs.REQUEST,
							Defs.FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE_OPTIONS,
							true);
				} else {
					msg = new Msg(cci.getControllerCommandNode(), Defs.REQUEST,
							Defs.FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE, true);
				}
				msg.append(cci.getControllerCommandNode());
				if (opts) {
					msg.append(getQueueManager().getTransmitOptions());
				}
				getQueueManager().sendMsg(msg, QueuePriority.Command);
			}
			break;
		case AssignReturnRoute:
			msg = new Msg(cci.getControllerCommandNode(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_ASSIGN_RETURN_ROUTE, true);
			msg.append(cci.getControllerCommandNode()); // from the node
			msg.append(cci.getControllerCommandArg()); // to the specific
			// destination
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case DeleteAllReturnRoutes:
			msg = new Msg(cci.getControllerCommandNode(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_DELETE_RETURN_ROUTE, true);
			msg.append(cci.getControllerCommandNode()); // from the node
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case SendNodeInformation:
			msg = new Msg(cci.getControllerCommandNode(), Defs.REQUEST,
					Defs.FUNC_ID_ZW_SEND_NODE_INFORMATION, true);
			msg.append(cci.getControllerCommandNode()); // to the node
			msg.append(getQueueManager().getTransmitOptions());
			getQueueManager().sendMsg(msg, QueuePriority.Command);
			break;
		case ReplicationSend:
			if (!isPrimaryController()) {
				updateControllerState(ControllerState.Error,
						ControllerError.NotPrimary);
			} else {
				msg = new Msg((byte) 0xFF, Defs.REQUEST,
						Defs.FUNC_ID_ZW_ADD_NODE_TO_NETWORK, true);
				msg.append(cci.isHighPower() ? Defs.ADD_NODE_CONTROLLER
						| Defs.OPTION_HIGH_POWER : Defs.ADD_NODE_CONTROLLER);
				getQueueManager().sendMsg(msg, QueuePriority.Command);
			}
			break;
		case CreateButton:
			// TODO: Need implemented
			break;
		case DeleteButton:
			// TODO: Need implemented
			break;
		case None:
			break;
		}
	}

	public void updateControllerState(ControllerState state) {
		updateControllerState(state, ControllerError.None);
	}

	public void updateControllerState(ControllerState state,
			ControllerError error) {
		ControllerCmd cci = getQueueManager().getCurrentControllerCmd();
		if (cci != null) {
			if (state != cci.getControllerState()) {
				cci.setControllerStateChanged(true);
				cci.setControllerState(state);
				switch (state) {
				case Error:
				case Cancel:
				case Failed:
				case Sleeping:
				case NodeFailed:
				case NodeOK:
				case Completed:
					cci.setControllerCommandDone(true);
					break;
				default:
					break;
				}

			}
			if (error != ControllerError.None) {
				cci.setControllerReturnError(error);
			}
		}
	}

	public void addNodeStop(byte functionId) {
		ControllerCmd cci = getQueueManager().getCurrentControllerCmd();
		Msg msg;

		if (cci == null) {
			return;
		}
		if (serialAPIVersion[0] == (byte) 2 && serialAPIVersion[1] == (byte) 76) {
			msg = new Msg((byte) 0xFF, Defs.REQUEST, functionId, false, false);
			msg.append(Defs.ADD_NODE_STOP);
			getQueueManager().sendMsg(msg, QueuePriority.Command);
		} else {
			msg = new Msg((byte) 0xFF, Defs.REQUEST, functionId, false, true);
			msg.append(Defs.ADD_NODE_STOP);
			getQueueManager().sendMsg(msg, QueuePriority.Command);
		}
	}

}
