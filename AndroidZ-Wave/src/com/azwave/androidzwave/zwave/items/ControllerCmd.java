package com.azwave.androidzwave.zwave.items;

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

public class ControllerCmd {

	public enum ControllerCommand {
		None, /** < No command. */
		AddDevice, /** < Add a new device or controller to the Z-Wave network. */
		CreateNewPrimary, /**
		 * < Add a new controller to the Z-Wave network. Used
		 * when old primary fails. Requires SUC.
		 */
		ReceiveConfiguration, /**
		 * < Receive Z-Wave network configuration
		 * information from another controller.
		 */
		RemoveDevice, /**
		 * < Remove a device or controller from the Z-Wave
		 * network.
		 */
		RemoveFailedNode, /**
		 * < Move a node to the controller's failed nodes
		 * list. This command will only work if the node cannot respond.
		 */
		HasNodeFailed, /**
		 * < Check whether a node is in the controller's failed
		 * nodes list.
		 */
		ReplaceFailedNode, /**
		 * < Replace a non-responding node with another. The
		 * node must be in the controller's list of failed nodes for this
		 * command to succeed.
		 */
		TransferPrimaryRole, /** < Make a different controller the primary. */
		RequestNetworkUpdate, /** < Request network information from the SUC/SIS. */
		RequestNodeNeighborUpdate, /**
		 * < Get a node to rebuild its neighbour
		 * list. This method also does RequestNodeNeighbors
		 */
		AssignReturnRoute, /** < Assign a network return routes to a device. */
		DeleteAllReturnRoutes, /** < Delete all return routes from a device. */
		SendNodeInformation, /** < Send a node information frame */
		ReplicationSend, /** < Send information from primary to secondary */
		CreateButton, /** < Create an id that tracks handheld button presses */
		DeleteButton
		/** < Delete id that tracks handheld button presses */
	};

	public enum ControllerState {
		Normal, /** < No command in progress. */
		Starting, /** < The command is starting. */
		Cancel, /** < The command was cancelled. */
		Error, /** < Command invocation had error(s) and was aborted */
		Waiting, /** < Controller is waiting for a user action. */
		Sleeping, /** < Controller command is on a sleep queue wait for device. */
		InProgress, /**
		 * < The controller is communicating with the other device
		 * to carry out the command.
		 */
		Completed, /** < The command has completed successfully. */
		Failed, /** < The command has failed. */
		NodeOK, /**
		 * < Used only with ControllerCommand_HasNodeFailed to indicate
		 * that the controller thinks the node is OK.
		 */
		NodeFailed
		/**
		 * < Used only with ControllerCommand_HasNodeFailed to indicate that the
		 * controller thinks the node has failed.
		 */
	};

	public enum ControllerError {
		None, ButtonNotFound, /** < Button */
		NodeNotFound, /** < Button */
		NotBridge, /** < Button */
		NotSUC, /** < CreateNewPrimary */
		NotSecondary, /** < CreateNewPrimary */
		NotPrimary, /** < RemoveFailedNode, AddNodeToNetwork */
		IsPrimary, /** < ReceiveConfiguration */
		NotFound, /** < RemoveFailedNode */
		Busy, /** < RemoveFailedNode, RequestNetworkUpdate */
		Failed, /** < RemoveFailedNode, RequestNetworkUpdate */
		Disabled, /** < RequestNetworkUpdate error */
		Overflow
		/** < RequestNetworkUpdate error */
	};

	private ControllerState controllerState;
	private boolean controllerStateChanged;
	private boolean controllerCommandDone;
	private ControllerCommand controllerCommand;
	private ControllerActionListener controllerCallback = null;
	private ControllerError controllerReturnError = null;
	private Object controllerCallbackContext;
	private boolean highPower = false;
	private boolean controllerAdded;
	private byte controllerCommandNode = (byte) 0xFF;
	private byte controllerCommandArg = 0;

	public ControllerState getControllerState() {
		return controllerState;
	}

	public void setControllerState(ControllerState controllerState) {
		this.controllerState = controllerState;
	}

	public boolean isControllerStateChanged() {
		return controllerStateChanged;
	}

	public void setControllerStateChanged(boolean controllerStateChanged) {
		this.controllerStateChanged = controllerStateChanged;
	}

	public boolean isControllerCommandDone() {
		return controllerCommandDone;
	}

	public void setControllerCommandDone(boolean controllerCommandDone) {
		this.controllerCommandDone = controllerCommandDone;
	}

	public ControllerCommand getControllerCommand() {
		return controllerCommand;
	}

	public void setControllerCommand(ControllerCommand controllerCommand) {
		this.controllerCommand = controllerCommand;
	}

	public ControllerActionListener getControllerCallback() {
		return controllerCallback;
	}

	public void setControllerCallback(
			ControllerActionListener controllerCallback) {
		this.controllerCallback = controllerCallback;
	}

	public ControllerError getControllerReturnError() {
		return controllerReturnError;
	}

	public void setControllerReturnError(ControllerError controllerReturnError) {
		this.controllerReturnError = controllerReturnError;
	}

	public Object getControllerCallbackContext() {
		return controllerCallbackContext;
	}

	public void setControllerCallbackContext(Object controllerCallbackContext) {
		this.controllerCallbackContext = controllerCallbackContext;
	}

	public boolean isHighPower() {
		return highPower;
	}

	public void setHighPower(boolean highPower) {
		this.highPower = highPower;
	}

	public boolean isControllerAdded() {
		return controllerAdded;
	}

	public void setControllerAdded(boolean controllerAdded) {
		this.controllerAdded = controllerAdded;
	}

	public byte getControllerCommandNode() {
		return controllerCommandNode;
	}

	public void setControllerCommandNode(byte controllerCommandNode) {
		this.controllerCommandNode = controllerCommandNode;
	}

	public byte getControllerCommandArg() {
		return controllerCommandArg;
	}

	public void setControllerCommandArg(byte controllerCommandArg) {
		this.controllerCommandArg = controllerCommandArg;
	}

}
