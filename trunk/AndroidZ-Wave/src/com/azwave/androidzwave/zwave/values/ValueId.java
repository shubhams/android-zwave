package com.azwave.androidzwave.zwave.values;

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

import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;

public class ValueId implements Comparable<ValueId> {

	public enum ValueGenre {
		BASIC, /**
		 * < The 'level' as controlled by basic commands. Usually
		 * duplicated by another command class.
		 */
		USER, /** < Basic values an ordinary user would be interested in. */
		CONFIG, /**
		 * < Device-specific configuration parameters. These cannot be
		 * automatically discovered via Z-Wave, and are usually described in the
		 * user manual instead.
		 */
		SYSTEM, /**
		 * < Values of significance only to users who understand the
		 * Z-Wave protocol
		 */
		COUNT
		/**
		 * < A count of the number of genres defined. Not to be used as a genre
		 * itself.
		 */
	};

	public enum ValueType {
		BOOL, /** < Boolean, true or false */
		BYTE, /** < 8-bit unsigned value */
		DECIMAL, /**
		 * < Represents a non-integer value as a string, to avoid
		 * floating point accuracy issues.
		 */
		INT, /** < 32-bit signed value */
		LIST, /** < List from which one item can be selected */
		SCHEDULE, /**
		 * < Complex type used with the Climate Control Schedule
		 * command class
		 */
		SHORT, /** < 16-bit signed value */
		STRING, /** < Text string */
		BUTTON, /**
		 * < A write-only value that is the equivalent of pressing a
		 * button to send a command to a device
		 */
		RAW,
		/** < A collection of bytes */
	};

	private Node node;
	private int id1 = 0, id2 = 0, homeId = 0;

	public ValueId() {
	}

	public ValueId(Node node, ValueGenre genre, byte cmdClassId, byte instance,
			byte valIdx, ValueType type) {
		this.node = node;
		homeId = node.getHomeId();
		id1 = SafeCast.toInt(node.getNodeId()) << 24 | genre.ordinal() << 22
				| SafeCast.toInt(cmdClassId) << 14
				| SafeCast.toInt(valIdx) << 4 | type.ordinal();
		id2 = SafeCast.toInt(instance) << 24;
	}

	public ValueId(Node node, byte instance) {
		this.node = node;
		homeId = node.getHomeId();
		id1 = SafeCast.toInt(node.getNodeId()) << 24;
		id2 = SafeCast.toInt(instance) << 24;
	}

	public ValueId(Node node) {
		this(node, (byte) 0);
	}

	@Override
	public int compareTo(ValueId another) {
		if ((homeId == another.homeId) && (id1 == another.id1)
				&& (id2 == another.id2) == true) {
			return 0;
		} else if (homeId == another.homeId) {
			if (id1 == another.id1) {
				return (int) (SafeCast.toLong(id2) - SafeCast
						.toLong(another.id2));
			} else {
				return (int) (SafeCast.toLong(id1) - SafeCast
						.toLong(another.id1));
			}
		} else {
			return (int) (SafeCast.toLong(homeId) - SafeCast
					.toLong(another.homeId));
		}
	}

	public int getValueStoreKey() {
		return (id1 & 0x003ffff0) | (id2 & 0xff000000);
	}

	public static int getValueStoreKey(byte cmdClassId, byte instance,
			byte valIdx) {
		return (SafeCast.toInt(instance) << 24)
				| (SafeCast.toInt(cmdClassId) << 14)
				| (SafeCast.toInt(valIdx) << 4);
	}

	public Node getNode() {
		return node;
	}

	public int getHomeId() {
		return homeId;
	}

	public byte getNodeId() {
		return (byte) ((id1 & 0xff000000) >> 24);
	}

	public ValueGenre getGenre() {
		return ValueGenre.values()[(id1 & 0x00c00000) >> 22];
	}

	public byte getCommandClassId() {
		return (byte) ((id1 & 0x003fc000) >> 14);
	}

	public byte getInstance() {
		return (byte) ((id2 & 0xff000000) >> 24);
	}

	public byte getIndex() {
		return (byte) ((id1 & 0x00000ff0) >> 4);
	}

	public ValueType getType() {
		return ValueType.values()[(id1 & 0x0000000f)];
	}

	public long getId() {
		return (long) ((SafeCast.toLong(id2) << 32) | id1);
	}

}
