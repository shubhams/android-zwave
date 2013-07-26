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

public class Defs {

	public static final int MAX_TOTAL_NODES = 256;
	public static final int MAX_NODES = 232;
	public static final int MAX_TRIES = 3;
	public static final int MAX_MAX_TRIES = 7;

	public static final int ACK_TIMEOUT = 1000;
	public static final int BYTE_TIMEOUT = 150;
	public static final int RETRY_TIMEOUT = 40000;

	public static final byte NODE_BROADCAST = (byte) 0xFF;

	public static final byte SOF = (byte) 0x01;
	public static final byte ACK = (byte) 0x06;
	public static final byte NAK = (byte) 0x15;
	public static final byte CAN = (byte) 0x18;

	public static final byte NUM_NODE_BITFIELD_BYTES = 29; // 29 bytes = 232
	// bits, one for each
	// possible node in
	// the network.

	public static final byte REQUEST = (byte) 0x00;
	public static final byte RESPONSE = (byte) 0x01;

	public static final byte ZW_CLOCK_SET = (byte) 0x30;

	public static final byte TRANSMIT_OPTION_ACK = (byte) 0x01;
	public static final byte TRANSMIT_OPTION_LOW_POWER = (byte) 0x02;
	public static final byte TRANSMIT_OPTION_AUTO_ROUTE = (byte) 0x04;
	public static final byte TRANSMIT_OPTION_NO_ROUTE = (byte) 0x10;
	public static final byte TRANSMIT_OPTION_EXPLORE = (byte) 0x20;

	public static final byte TRANSMIT_COMPLETE_OK = (byte) 0x00;
	public static final byte TRANSMIT_COMPLETE_NO_ACK = (byte) 0x01;
	public static final byte TRANSMIT_COMPLETE_FAIL = (byte) 0x02;
	public static final byte TRANSMIT_COMPLETE_NOT_IDLE = (byte) 0x03;
	public static final byte TRANSMIT_COMPLETE_NOROUTE = (byte) 0x04;

	public static final byte RECEIVE_STATUS_ROUTED_BUSY = (byte) 0x01;
	public static final byte RECEIVE_STATUS_TYPE_BROAD = (byte) 0x04;

	public static final byte FUNC_ID_SERIAL_API_GET_INIT_DATA = (byte) 0x02;
	public static final byte FUNC_ID_SERIAL_API_APPL_NODE_INFORMATION = (byte) 0x03;
	public static final byte FUNC_ID_APPLICATION_COMMAND_HANDLER = (byte) 0x04;
	public static final byte FUNC_ID_ZW_GET_CONTROLLER_CAPABILITIES = (byte) 0x05;
	public static final byte FUNC_ID_SERIAL_API_SET_TIMEOUTS = (byte) 0x06;
	public static final byte FUNC_ID_SERIAL_API_GET_CAPABILITIES = (byte) 0x07;
	public static final byte FUNC_ID_SERIAL_API_SOFT_RESET = (byte) 0x08;

	public static final byte FUNC_ID_ZW_SEND_NODE_INFORMATION = (byte) 0x12;
	public static final byte FUNC_ID_ZW_SEND_DATA = (byte) 0x13;
	public static final byte FUNC_ID_ZW_GET_VERSION = (byte) 0x15;
	public static final byte FUNC_ID_ZW_R_F_POWER_LEVEL_SET = (byte) 0x17;
	public static final byte FUNC_ID_ZW_GET_RANDOM = (byte) 0x1c;
	public static final byte FUNC_ID_ZW_MEMORY_GET_ID = (byte) 0x20;
	public static final byte FUNC_ID_MEMORY_GET_BYTE = (byte) 0x21;
	public static final byte FUNC_ID_ZW_READ_MEMORY = (byte) 0x23;

	public static final byte FUNC_ID_ZW_SET_LEARN_NODE_STATE = (byte) 0x40; // Not
	// implemented
	public static final byte FUNC_ID_ZW_GET_NODE_PROTOCOL_INFO = (byte) 0x41; // Get
	// protocol
	// info
	// (baud
	// rate,
	// listening,
	// etc.)
	// for
	// a
	// given
	// node
	public static final byte FUNC_ID_ZW_SET_DEFAULT = (byte) 0x42; // Reset
	// controller
	// and node
	// info to
	// default
	// (original)
	// values
	public static final byte FUNC_ID_ZW_NEW_CONTROLLER = (byte) 0x43; // Not
	// implemented
	public static final byte FUNC_ID_ZW_REPLICATION_COMMAND_COMPLETE = (byte) 0x44; // Replication
	// send
	// data
	// complete
	public static final byte FUNC_ID_ZW_REPLICATION_SEND_DATA = (byte) 0x45; // Replication
	// send
	// data
	public static final byte FUNC_ID_ZW_ASSIGN_RETURN_ROUTE = (byte) 0x46; // Assign
	// a
	// return
	// route
	// from
	// the
	// specified
	// node
	// to
	// the
	// controller
	public static final byte FUNC_ID_ZW_DELETE_RETURN_ROUTE = (byte) 0x47; // Delete
	// all
	// return
	// routes
	// from
	// the
	// specified
	// node
	public static final byte FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE = (byte) 0x48; // Ask
	// the
	// specified
	// node
	// to
	// update
	// its
	// neighbors
	// (then
	// read
	// them
	// from
	// the
	// controller)
	public static final byte FUNC_ID_ZW_APPLICATION_UPDATE = (byte) 0x49; // Get
	// a
	// list
	// of
	// supported
	// (and
	// controller)
	// command
	// classes
	public static final byte FUNC_ID_ZW_ADD_NODE_TO_NETWORK = (byte) 0x4a; // Control
	// the
	// addnode
	// (or
	// addcontroller)
	// process...start,
	// stop,
	// etc.
	public static final byte FUNC_ID_ZW_REMOVE_NODE_FROM_NETWORK = (byte) 0x4b; // Control
	// the
	// removenode
	// (or
	// removecontroller)
	// process...start,
	// stop,
	// etc.
	public static final byte FUNC_ID_ZW_CREATE_NEW_PRIMARY = (byte) 0x4c; // Control
	// the
	// createnewprimary
	// process...start,
	// stop,
	// etc.
	public static final byte FUNC_ID_ZW_CONTROLLER_CHANGE = (byte) 0x4d; // Control
	// the
	// transferprimary
	// process...start,
	// stop,
	// etc.
	public static final byte FUNC_ID_ZW_SET_LEARN_MODE = (byte) 0x50; // Put a
	// controller
	// into
	// learn
	// mode
	// for
	// replication/
	// receipt
	// of
	// configuration
	// info
	public static final byte FUNC_ID_ZW_ASSIGN_SUC_RETURN_ROUTE = (byte) 0x51; // Assign
	// a
	// return
	// route
	// to
	// the
	// SUC
	public static final byte FUNC_ID_ZW_ENABLE_SUC = (byte) 0x52; // Make a
	// controller
	// a Static
	// Update
	// Controller
	public static final byte FUNC_ID_ZW_REQUEST_NETWORK_UPDATE = (byte) 0x53; // Network
	// update
	// for
	// a
	// SUC(?)
	public static final byte FUNC_ID_ZW_SET_SUC_NODE_ID = (byte) 0x54; // Identify
	// a
	// Static
	// Update
	// Controller
	// node
	// id
	public static final byte FUNC_ID_ZW_DELETE_SUC_RETURN_ROUTE = (byte) 0x55; // Remove
	// return
	// routes
	// to
	// the
	// SUC
	public static final byte FUNC_ID_ZW_GET_SUC_NODE_ID = (byte) 0x56; // Try to
	// retrieve
	// a
	// Static
	// Update
	// Controller
	// node
	// id
	// (zero
	// if no
	// SUC
	// present)
	public static final byte FUNC_ID_ZW_REQUEST_NODE_NEIGHBOR_UPDATE_OPTIONS = (byte) 0x5a; // Allow
	// options
	// for
	// request
	// node
	// neighbor
	// update
	public static final byte FUNC_ID_ZW_REQUEST_NODE_INFO = (byte) 0x60; // Get
	// info
	// (supported
	// command
	// classes)
	// for
	// the
	// specified
	// node
	public static final byte FUNC_ID_ZW_REMOVE_FAILED_NODE_ID = (byte) 0x61; // Mark
	// a
	// specified
	// node
	// id
	// as
	// failed
	public static final byte FUNC_ID_ZW_IS_FAILED_NODE_ID = (byte) 0x62; // Check
	// to
	// see
	// if a
	// specified
	// node
	// has
	// failed
	public static final byte FUNC_ID_ZW_REPLACE_FAILED_NODE = (byte) 0x63; // Remove
	// a
	// failed
	// node
	// from
	// the
	// controller's
	// list
	// (?)
	public static final byte FUNC_ID_ZW_GET_ROUTING_INFO = (byte) 0x80; // Get a
	// specified
	// node's
	// neighbor
	// information
	// from
	// the
	// controller
	public static final byte FUNC_ID_SERIAL_API_SLAVE_NODE_INFO = (byte) 0xA0; // Set
	// application
	// virtual
	// slave
	// node
	// information
	public static final byte FUNC_ID_APPLICATION_SLAVE_COMMAND_HANDLER = (byte) 0xA1; // Slave
	// command
	// handler
	public static final byte FUNC_ID_ZW_SEND_SLAVE_NODE_INFO = (byte) 0xA2; // Send
	// a
	// slave
	// node
	// information
	// frame
	public static final byte FUNC_ID_ZW_SEND_SLAVE_DATA = (byte) 0xA3; // Send
	// data
	// from
	// slave
	public static final byte FUNC_ID_ZW_SET_SLAVE_LEARN_MODE = (byte) 0xA4; // Enter
	// slave
	// learn
	// mode
	public static final byte FUNC_ID_ZW_GET_VIRTUAL_NODES = (byte) 0xA5; // Return
	// all
	// virtual
	// nodes
	public static final byte FUNC_ID_ZW_IS_VIRTUAL_NODE = (byte) 0xA6; // Virtual
	// node
	// test
	public static final byte FUNC_ID_ZW_SET_PROMISCUOUS_MODE = (byte) 0xD0; // Set
	// controller
	// into
	// promiscuous
	// mode
	// to
	// listen
	// to
	// all
	// frames
	public static final byte FUNC_ID_PROMISCUOUS_APPLICATION_COMMAND_HANDLER = (byte) 0xD1;

	public static final byte ADD_NODE_ANY = (byte) 0x01;
	public static final byte ADD_NODE_CONTROLLER = (byte) 0x02;
	public static final byte ADD_NODE_SLAVE = (byte) 0x03;
	public static final byte ADD_NODE_EXISTING = (byte) 0x04;
	public static final byte ADD_NODE_STOP = (byte) 0x05;
	public static final byte ADD_NODE_STOP_FAILED = (byte) 0x06;

	public static final byte ADD_NODE_STATUS_LEARN_READY = (byte) 0x01;
	public static final byte ADD_NODE_STATUS_NODE_FOUND = (byte) 0x02;
	public static final byte ADD_NODE_STATUS_ADDING_SLAVE = (byte) 0x03;
	public static final byte ADD_NODE_STATUS_ADDING_CONTROLLER = (byte) 0x04;
	public static final byte ADD_NODE_STATUS_PROTOCOL_DONE = (byte) 0x05;
	public static final byte ADD_NODE_STATUS_DONE = (byte) 0x06;
	public static final byte ADD_NODE_STATUS_FAILED = (byte) 0x07;

	public static final byte REMOVE_NODE_ANY = (byte) 0x01;
	public static final byte REMOVE_NODE_CONTROLLER = (byte) 0x02;
	public static final byte REMOVE_NODE_SLAVE = (byte) 0x03;
	public static final byte REMOVE_NODE_STOP = (byte) 0x05;

	public static final byte REMOVE_NODE_STATUS_LEARN_READY = (byte) 0x01;
	public static final byte REMOVE_NODE_STATUS_NODE_FOUND = (byte) 0x02;
	public static final byte REMOVE_NODE_STATUS_REMOVING_SLAVE = (byte) 0x03;
	public static final byte REMOVE_NODE_STATUS_REMOVING_CONTROLLER = (byte) 0x04;
	public static final byte REMOVE_NODE_STATUS_DONE = (byte) 0x06;
	public static final byte REMOVE_NODE_STATUS_FAILED = (byte) 0x07;

	public static final byte CREATE_PRIMARY_START = (byte) 0x02;
	public static final byte CREATE_PRIMARY_STOP = (byte) 0x05;
	public static final byte CREATE_PRIMARY_STOP_FAILED = (byte) 0x06;

	public static final byte CONTROLLER_CHANGE_START = (byte) 0x02;
	public static final byte CONTROLLER_CHANGE_STOP = (byte) 0x05;
	public static final byte CONTROLLER_CHANGE_STOP_FAILED = (byte) 0x06;

	public static final byte LEARN_MODE_STARTED = (byte) 0x01;
	public static final byte LEARN_MODE_DONE = (byte) 0x06;
	public static final byte LEARN_MODE_FAILED = (byte) 0x07;
	public static final byte LEARN_MODE_DELETED = (byte) 0x80;

	public static final byte REQUEST_NEIGHBOR_UPDATE_STARTED = (byte) 0x21;
	public static final byte REQUEST_NEIGHBOR_UPDATE_DONE = (byte) 0x22;
	public static final byte REQUEST_NEIGHBOR_UPDATE_FAILED = (byte) 0x23;

	public static final byte FAILED_NODE_OK = (byte) 0x00;
	public static final byte FAILED_NODE_REMOVED = (byte) 0x01;
	public static final byte FAILED_NODE_NOT_REMOVED = (byte) 0x02;

	public static final byte FAILED_NODE_REPLACE_WAITING = (byte) 0x03;
	public static final byte FAILED_NODE_REPLACE_DONE = (byte) 0x04;
	public static final byte FAILED_NODE_REPLACE_FAILED = (byte) 0x05;

	public static final byte FAILED_NODE_REMOVE_STARTED = (byte) 0x00;
	public static final byte FAILED_NODE_NOT_PRIMARY_CONTROLLER = (byte) 0x02;
	public static final byte FAILED_NODE_NO_CALLBACK_FUNCTION = (byte) 0x04;
	public static final byte FAILED_NODE_NOT_FOUND = (byte) 0x08;
	public static final byte FAILED_NODE_REMOVE_PROCESS_BUSY = (byte) 0x10;
	public static final byte FAILED_NODE_REMOVE_FAIL = (byte) 0x20;

	public static final byte SUC_UPDATE_DONE = (byte) 0x00;
	public static final byte SUC_UPDATE_ABORT = (byte) 0x01;
	public static final byte SUC_UPDATE_WAIT = (byte) 0x02;
	public static final byte SUC_UPDATE_DISABLED = (byte) 0x03;
	public static final byte SUC_UPDATE_OVERFLOW = (byte) 0x04;

	public static final byte SUC_FUNC_BASIC_SUC = (byte) 0x00;
	public static final byte SUC_FUNC_NODEID_SERVER = (byte) 0x01;

	public static final byte UPDATE_STATE_NODE_INFO_RECEIVED = (byte) 0x84;
	public static final byte UPDATE_STATE_NODE_INFO_REQ_DONE = (byte) 0x82;
	public static final byte UPDATE_STATE_NODE_INFO_REQ_FAILED = (byte) 0x81;
	public static final byte UPDATE_STATE_ROUTING_PENDING = (byte) 0x80;
	public static final byte UPDATE_STATE_NEW_ID_ASSIGNED = (byte) 0x40;
	public static final byte UPDATE_STATE_DELETE_DONE = (byte) 0x20;
	public static final byte UPDATE_STATE_SUC_ID = (byte) 0x10;

	public static final byte APPLICATION_NODEINFO_LISTENING = (byte) 0x01;
	public static final byte APPLICATION_NODEINFO_OPTIONAL_FUNCTIONALITY = (byte) 0x02;

	public static final byte SLAVE_ASSIGN_COMPLETE = (byte) 0x00;
	public static final byte SLAVE_ASSIGN_NODEID_DONE = (byte) 0x01; // Node ID
	// has been
	// assigned
	public static final byte SLAVE_ASSIGN_RANGE_INFO_UPDATE = (byte) 0x02; // Node
	// is
	// doing
	// neighbor
	// discovery

	public static final byte SLAVE_LEARN_MODE_DISABLE = (byte) 0x00; // disable
	// add/remove
	// virtual
	// slave
	// nodes
	public static final byte SLAVE_LEARN_MODE_ENABLE = (byte) 0x01; // enable
	// ability
	// to
	// include/exclude
	// virtual
	// slave
	// nodes
	public static final byte SLAVE_LEARN_MODE_ADD = (byte) 0x02; // add node
	// directly but
	// only if
	// primary/inclusion
	// controller
	public static final byte SLAVE_LEARN_MODE_REMOVE = (byte) 0x03; // remove
	// node
	// directly
	// but only
	// if
	// primary/inclusion
	// controller

	public static final byte OPTION_HIGH_POWER = (byte) 0x80;

	// Device request related
	public static final byte BASIC_SET = (byte) 0x01;
	public static final byte BASIC_REPORT = (byte) 0x03;

	public static final byte COMMAND_CLASS_BASIC = (byte) 0x20;
	public static final byte COMMAND_CLASS_CONTROLLER_REPLICATION = (byte) 0x21;
	public static final byte COMMAND_CLASS_APPLICATION_STATUS = (byte) 0x22;
	public static final byte COMMAND_CLASS_HAIL = (byte) 0x82;
}
