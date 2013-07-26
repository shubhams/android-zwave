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

import java.util.ArrayList;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueList;

public class Association extends CommandClass {

	public static final byte COMMAND_CLASS_ID = (byte) 0x85;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_ASSOCIATION";

	public static final byte ASSOC_CMD_SET = 0x01;
	public static final byte ASSOC_CMD_GET = 0x02;
	public static final byte ASSOC_CMD_REPORT = 0x03;
	public static final byte ASSOC_CMD_REMOVE = 0x04;
	public static final byte ASSOC_CMD_GROUPINGS_GET = 0x05;
	public static final byte ASSOC_CMD_GROUPINGS_REPORT = 0x06;

	private boolean queryAll = false; // When true, once a group has been
	// queried, we request the next one.
	private byte numGroups = 0; // Number of groups supported by the device. 255
	// is reported by certain manufacturers and
	// requires special handling.
	private ArrayList<Byte> pendingMembers = new ArrayList<Byte>();

	public Association(Node node) {
		super(node);
		setStaticRequest(STATIC_REQUEST_VALUES);
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
		msg.append(getNodeId());
		msg.append((byte) 2);
		msg.append(getCommandClassId());
		msg.append(ASSOC_CMD_GROUPINGS_GET);
		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	public void requestAllGroup(int requestFlags) {
		queryAll = true;
		if (numGroups == (byte) 0xFF) {
			queryGroup((byte) 0xFF, requestFlags);
		} else {
			queryGroup((byte) 1, requestFlags);
		}
	}

	public void queryGroup(byte groupIdx, int requestFlags) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
				COMMAND_CLASS_ID);
		msg.appends(new byte[] { getNodeId(), 3, COMMAND_CLASS_ID,
				ASSOC_CMD_GET, groupIdx,
				node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
	}

	public void set(byte groupIdx, byte targetNodeId) {
		Msg msg = new Msg(getNodeId(), Defs.REQUEST, Defs.FUNC_ID_ZW_SEND_DATA,
				true);
		msg.append(getNodeId());
		msg.append((byte) 4);
		msg.append(getCommandClassId());
		msg.append(ASSOC_CMD_SET);
		msg.append(groupIdx);
		msg.append(targetNodeId);
		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
	}

	public void remove(byte groupIdx, byte targetNodeId) {
		Msg msg = new Msg(getNodeId(), Defs.REQUEST, Defs.FUNC_ID_ZW_SEND_DATA,
				true);
		msg.append(getNodeId());
		msg.append((byte) 4);
		msg.append(getCommandClassId());
		msg.append(ASSOC_CMD_REMOVE);
		msg.append(groupIdx);
		msg.append(targetNodeId);
		msg.append(node.getQueueManager().getTransmitOptions());
		node.getQueueManager().sendMsg(msg, QueuePriority.Send);
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
		boolean handled = false;
		if (node != null) {
			if (ASSOC_CMD_GROUPINGS_REPORT == data[0]) {
				numGroups = data[1];
				clearStaticRequest(STATIC_REQUEST_VALUES);
				handled = true;
			} else if (ASSOC_CMD_REPORT == data[0]) {
				byte groupIdx = data[1];
				byte maxAssociations = data[2]; // If the maxAssociations is
				// zero, this is not a supported
				// group.
				byte numReportsToFollow = data[3]; // If a device supports a lot
				// of associations, they may
				// come in more than one
				// message.

				if (maxAssociations != 0) {
					if (length >= 5) {
						byte numAssociations = (byte) (length - 5);
						if (numAssociations != 0) {
							for (int i = 0; i < SafeCast.toInt(numAssociations); ++i) {
								pendingMembers.add(data[i + 4]);
							}
						}
					}

					if (numReportsToFollow != 0) {
						// We're expecting more reports for this group
						return true;
					} else {
						// TODO: GROUP.
						/*
						 * Group* group = node->GetGroup( groupIdx ); if( NULL
						 * == group ) { // Group has not been created yet group
						 * = new Group( GetHomeId(), GetNodeId(), groupIdx,
						 * maxAssociations ); node->AddGroup( group ); }
						 * 
						 * // Update the group with its new contents
						 * group->OnGroupChanged( m_pendingMembers );
						 * m_pendingMembers.clear();
						 */
					}
				} else {
					/*
					 * node->AutoAssociate();
					 */
					queryAll = false;
				}

				if (queryAll) {
					/*
					 * uint8 nextGroup = groupIdx + 1; if( !nextGroup ) {
					 * nextGroup = 1; }
					 * 
					 * if( nextGroup <= m_numGroups ) { // Query the next group
					 * QueryGroup( nextGroup, 0 ); } else { // We're all done
					 * Log::Write( LogLevel_Info, GetNodeId(),
					 * "Querying associations for node %d is complete.",
					 * GetNodeId() ); node->AutoAssociate(); m_queryAll = false;
					 * }
					 */
				}

				handled = true;
			}
		}
		/*
		 * if (ASSOC_CMD_REPORT == data[0]) { ValueList value = (ValueList)
		 * getValue((byte) instance, (byte) 0); if (value != null) {
		 * value.onValueRefreshed(SafeCast.toInt(data[1])); } return true; }
		 */

		return handled;
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
