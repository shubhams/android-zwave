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
import java.util.Arrays;
import java.util.Map.Entry;

import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.values.Value;

public class MultiInstance extends CommandClass {

	public static final byte COMMAND_CLASS_ID = 0x60;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_MULTI_INSTANCE/CHANNEL";

	public static final byte MULTI_INSTANCE_CMD_GET = 0x04;
	public static final byte MULTI_INSTANCE_CMD_REPORT = 0x05;
	public static final byte MULTI_INSTANCE_CMD_ENCAP = 0x06;

	public static final byte MULTI_CHANNEL_CMD_ENDPOINT_GET = 0x07;
	public static final byte MULTI_CHANNEL_CMD_ENDPOINT_REPORT = 0x08;
	public static final byte MULTI_CHANNEL_CMD_CAP_GET = 0x09;
	public static final byte MULTI_CHANNEL_CMD_CAP_REPORT = 0x0A;
	public static final byte MULTI_CHANNEL_CMD_ENDPOINT_FIND = 0x0B;
	public static final byte MULTI_CHANNEL_CMD_ENDPOINT_FIND_REPORT = 0x0C;
	public static final byte MULTI_CHANNEL_CMD_ENCAP = 0x0D;

	private static final byte[] genericClass = { (byte) 0x21, // Multilevel
			// Sensor
			(byte) 0x20, // Binary Sensor
			(byte) 0x31, // Meter
			(byte) 0x08, // Thermostat
			(byte) 0x11, // Multilevel Switch
			(byte) 0x10, // Binary Switch
			(byte) 0x12, // Remote Switch
			(byte) 0xA1, // Alarm Sensor
			(byte) 0x16, // Ventilation
			(byte) 0x30, // Pulse Meter
			(byte) 0x40, // Entry Control
			(byte) 0x13, // Toggle Switch
			(byte) 0x03, // AV Control Point
			(byte) 0x04, // Display
			(byte) 0x00 // End of list
	};

	private boolean numEndPointsCanChange;
	private boolean endPointsAreSameClass;
	private byte numEndPoints = 0;

	private byte endPointFindIndex;
	private byte numEndPointsFound;
	private ArrayList<Byte> endPointCommandClasses = new ArrayList<Byte>();

	private byte numEndPointsHint = 0; // for nodes that do not report correct
	// number of end points
	private MultiInstanceMapping endPointMap = MultiInstanceMapping.MULTI_INSTANCE_MAP_ALL; // Determine
	// how
	// to
	// map
	// end
	// points
	// to
	// value
	// id
	// instances
	private boolean endPointFindSupported = false;

	public enum MultiInstanceMapping {
		MULTI_INSTANCE_MAP_ALL, MULTI_INSTANCE_MAP_END_POINTS, MULTI_INSTANCE_MAP_OTHER
	}

	public MultiInstance(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public boolean requestInstances() {
		boolean res = false;
		if (getVersion() == (byte) 1) {
			if (node != null) {
				for (Entry<Byte, CommandClass> entry : node
						.getCommandClassManager().getCommandClassMap()
						.entrySet()) {
					CommandClass cc = entry.getValue();
					if (cc.getCommandClassId() == NoOperation.COMMAND_CLASS_ID) {
						continue;
					}
					if (cc.hasStaticRequest(STATIC_REQUEST_INSTANCES)) {
						Msg msg = Msg.createZWaveApplicationCommandHandler(
								getNodeId(), COMMAND_CLASS_ID);
						msg.appends(new byte[] { getNodeId(), (byte) 3,
								COMMAND_CLASS_ID, MULTI_INSTANCE_CMD_GET,
								cc.getCommandClassId(),
								node.getQueueManager().getTransmitOptions() });
						node.getQueueManager()
								.sendMsg(msg, QueuePriority.Query);
						res = true;
					}
				}
			}
		} else {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.appends(new byte[] { getNodeId(), (byte) 2, COMMAND_CLASS_ID,
					MULTI_CHANNEL_CMD_ENDPOINT_GET,
					node.getQueueManager().getTransmitOptions() });
			node.getQueueManager().sendMsg(msg, QueuePriority.Query);
			res = true;
		}
		return res;
	}

	private void handleMultiChannelEncap(byte[] data, int length) {
		if (node != null) {
			byte endPoint = (byte) (data[1] & (byte) 0x7F);
			byte commandClassId = data[3];
			CommandClass cc = node.getCommandClassManager().getCommandClass(
					commandClassId);
			if (cc != null) {
				byte instance = cc.getInstance(endPoint);
				if (instance != 0) {
					cc.handleMsg(Arrays.copyOfRange(data, 4, data.length),
							length - 4, instance);
				}
			}
		}
	}

	private void handleMultiChannelEndPointFindReport(byte[] data, int length) {
		byte numEndPoints = (byte) (length - 5);
		for (int i = 0; i < numEndPoints; ++i) {
			byte endPoint = (byte) (data[i + 4] & (byte) 0x7F);
			if (endPointsAreSameClass && node != null) {
				for (Byte it : endPointCommandClasses) {
					byte commandClassId = it.byteValue();
					CommandClass cc = node.getCommandClassManager()
							.getCommandClass(commandClassId);
					if (cc != null) {
						cc.setInstance(endPoint);
					}
				}
			} else {
				// Endpoints are different, so request the capabilities
				Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
						COMMAND_CLASS_ID);
				msg.append(getNodeId());
				msg.append((byte) 3);
				msg.append(getCommandClassId());
				msg.append(MULTI_CHANNEL_CMD_CAP_GET);
				msg.append(endPoint);
				msg.append(node.getQueueManager().getTransmitOptions());
				node.getQueueManager().sendMsg(msg, QueuePriority.Send);
			}
		}

		numEndPointsFound += numEndPoints;
		if (!endPointsAreSameClass && data[1] == (byte) 0
				&& numEndPointsFound < numEndPoints) {
			// We have not yet found all the endpoints, so move to the next
			// generic class request
			++endPointFindIndex;
			if (genericClass[endPointFindIndex] > 0) {
				Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
						COMMAND_CLASS_ID);
				msg.append(getNodeId());
				msg.append((byte) 4);
				msg.append(getCommandClassId());
				msg.append(MULTI_CHANNEL_CMD_ENDPOINT_FIND);
				msg.append(genericClass[endPointFindIndex]);
				msg.append((byte) 0xFF);
				msg.append(node.getQueueManager().getTransmitOptions());
				node.getQueueManager().sendMsg(msg, QueuePriority.Send);
			}
		}
	}

	private void handleMultiChannelCapabilityReport(byte[] data, int length) {
		if (node != null) {
			byte endPoint = (byte) (data[1] & (byte) 0x7f);
			boolean dynamic = ((data[1] & (byte) 0x80) != 0);
			boolean afterMark = false;
			byte numCommandClasses = (byte) (length - 5);

			endPointCommandClasses.clear();
			for (int i = 0; i < numCommandClasses; ++i) {
				byte commandClassId = data[i + 4];
				if (commandClassId == (byte) 0xEF) {
					afterMark = true;
					continue;
				}
				endPointCommandClasses.add(commandClassId);
				CommandClass cc = node.getCommandClassManager()
						.getCommandClass(commandClassId);
				if (cc == null) {
					cc = node.getCommandClassManager().addCommandClass(
							commandClassId);
					if (cc != null && afterMark) {
						cc.setAfterMark();
					}
				}
			}

			Basic basic = (Basic) node.getCommandClassManager()
					.getCommandClass(Basic.COMMAND_CLASS_ID);
			if (endPointsAreSameClass) {
				int len;
				if (endPointMap == MultiInstanceMapping.MULTI_INSTANCE_MAP_ALL) {
					endPoint = 0;
					len = numEndPoints + 1;
				} else {
					endPoint = 1;
					len = numEndPoints;
				}

				for (int i = 1; i <= len; i++) {
					for (Byte it : endPointCommandClasses) {
						byte commandClassId = it.byteValue();
						CommandClass cc = node.getCommandClassManager()
								.getCommandClass(commandClassId);
						if (cc != null) {
							cc.setInstance((byte) i);
							if (endPointMap != MultiInstanceMapping.MULTI_INSTANCE_MAP_ALL
									|| i != 1) {
								cc.setEndPoint((byte) i, endPoint);
							}
							// If we support the BASIC command class and it is
							// mapped to a command class
							// assigned to this end point, make sure the BASIC
							// command class is also associated
							// with this end point.
							if (basic != null
									&& basic.getMapping() == commandClassId) {
								basic.setInstance((byte) i);
								if (endPointMap != MultiInstanceMapping.MULTI_INSTANCE_MAP_ALL
										|| i != 1) {
									basic.setEndPoint((byte) i, endPoint);
								}
							}
						}
					}
					endPoint++;
				}
			} else {
				for (Byte it : endPointCommandClasses) {
					byte commandClassId = it.byteValue();
					CommandClass cc = node.getCommandClassManager()
							.getCommandClass(commandClassId);
					if (cc != null) {
						int i = 1;
						for (i = 1; i <= 127; i++) {
							if (endPointMap == MultiInstanceMapping.MULTI_INSTANCE_MAP_ALL) {
								if (!cc.getInstances().isSet(i)) {
									break;
								}
							} else if (i == 1 && cc.getInstances().isSet(i)
									&& cc.getEndPoint((byte) i) == 0) {
								break;
							} else if (!cc.getInstances().isSet(i)) {
								break;
							}
						}
						cc.setInstance((byte) i);
						cc.setEndPoint((byte) i, endPoint);
						// If we support the BASIC command class and it is
						// mapped to a command class
						// assigned to this end point, make sure the BASIC
						// command class is also associated
						// with this end point.
						if (basic != null
								&& basic.getMapping() == commandClassId) {
							basic.setInstance((byte) i);
							basic.setEndPoint((byte) i, endPoint);
						}
					}
				}
			}
		}
	}

	private void handleMultiChannelEndPointReport(byte[] data, int length) {
		if (numEndPoints != 0) {
			return;
		}
		numEndPointsCanChange = ((data[1] & (byte) 0x80) != 0); // Number of
		// endpoints can
		// change.
		endPointsAreSameClass = ((data[1] & (byte) 0x40) != 0); // All endpoints
		// are the same
		// command
		// class.
		numEndPoints = (byte) (data[2] & (byte) 0x7F);

		if (numEndPointsHint != 0) {
			numEndPoints = numEndPointsHint;
		}

		int len = numEndPoints;
		if (endPointsAreSameClass) {
			len = 1;
		}

		// This code assumes the endpoints are all in numeric sequential order.
		// Since the end point finds do not appear to work this is the best
		// estimate.
		for (int i = 1; i <= len; i++) {
			Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(),
					COMMAND_CLASS_ID);
			msg.append(getNodeId());
			msg.append((byte) 3);
			msg.append(getCommandClassId());
			msg.append(MULTI_CHANNEL_CMD_CAP_GET);
			msg.append((byte) i);
			msg.append(node.getQueueManager().getTransmitOptions());
			node.getQueueManager().sendMsg(msg, QueuePriority.Send);
		}
	}

	private void handleMultiInstanceEncap(byte[] data, int length) {
		if (node != null) {
			byte instance = data[1];
			byte commandClassId = data[2];

			if (getVersion() > 1) {
				instance &= (byte) 0x7F;
			}

			CommandClass cc = node.getCommandClassManager().getCommandClass(
					commandClassId);
			if (cc != null) {
				cc.handleMsg(Arrays.copyOfRange(data, 3, data.length),
						length - 3, instance);
			}
		}
	}

	private void handleMultiInstanceReport(byte[] data, int length) {
		if (node != null) {
			byte commandClassId = data[1];
			byte instances = data[2];
			CommandClass cc = node.getCommandClassManager().getCommandClass(
					commandClassId);
			if (cc != null) {
				cc.setInstances(instances);
				cc.clearStaticRequest(STATIC_REQUEST_INSTANCES);
			}
		}
	}

	public byte getMaxVersion() {
		return 2;
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
			handled = true;
			switch (data[0]) {
			case MULTI_INSTANCE_CMD_REPORT:
				handleMultiInstanceReport(data, length);
				break;
			case MULTI_INSTANCE_CMD_ENCAP:
				handleMultiInstanceEncap(data, length);
				break;
			case MULTI_CHANNEL_CMD_ENDPOINT_REPORT:
				handleMultiChannelEndPointReport(data, length);
				break;
			case MULTI_CHANNEL_CMD_CAP_REPORT:
				handleMultiChannelCapabilityReport(data, length);
				break;
			case MULTI_CHANNEL_CMD_ENDPOINT_FIND_REPORT:
				handleMultiChannelEndPointFindReport(data, length);
				break;
			case MULTI_CHANNEL_CMD_ENCAP:
				handleMultiChannelEncap(data, length);
				break;
			default:
				handled = false;
				break;
			}
		}
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
	public boolean requestState(int requestFlags, byte instance,
			QueuePriority queue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean requestValue(int requestFlags, byte index, byte instance,
			QueuePriority queue) {
		// TODO Auto-generated method stub
		return false;
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
