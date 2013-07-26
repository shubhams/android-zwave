package com.azwave.androidzwave.zwave;

//-----------------------------------------------------------------------------
//Copyright (c) 2011 Mal Lansell <openzwave@lansell.org>
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

import java.util.Iterator;
import java.util.Vector;

public class BitField implements Iterable<Integer> {
	public Vector<Integer> bits = new Vector<Integer>();
	public int numBitSet = 0;

	public void set(int idx) {
		if (!isSet(idx)) {
			int newSize = (idx >> 5) + 1;
			if (newSize > bits.size()) {
				for (int i = 0; i < newSize - bits.size(); i++) {
					bits.add(0);
				}
			}
			bits.set(idx >> 5, bits.get(idx >> 5) | (1 << (idx & 0x1F)));
			++numBitSet;
		}
	}

	void clear(int idx) {
		if (isSet(idx)) {
			bits.set(idx >> 5, bits.get(idx >> 5) & ~(1 << (idx & 0x1F)));
			--numBitSet;
		}
	}

	public boolean isSet(int idx) {
		if ((idx >> 5) < bits.size()) {
			return ((bits.get(idx >> 5) & (1 << (idx & 0x1F))) != 0);
		}
		return false;
	}

	public int getNumSetBits() {
		return numBitSet;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new BitFieldIterator(this);
	}

	private class BitFieldIterator implements Iterator<Integer> {

		public BitField mBitField;
		public int mIdx = 0;

		public BitFieldIterator(BitField mBitField) {
			this.mBitField = mBitField;
		}

		@Override
		public boolean hasNext() {
			return (mIdx < (mBitField.bits.size() << 5)) ? true : false;
		}

		@Override
		public Integer next() {
			while (((++mIdx) >> 5) < mBitField.bits.size()) {
				// See if there are any bits left to find in the current uint32
				if ((mBitField.bits.get(mIdx >> 5) & ~((1 << (mIdx & 0x1F)) - 1)) == 0) {
					// No more bits - move on to next uint32 (or rather one less
					// than
					// the next uint32 because of the preincrement in the while
					// statement)
					mIdx = (mIdx & 0xffffffE0) + 31;
				} else {
					if ((mBitField.bits.get(mIdx >> 5) & (1 << (mIdx & 0x1F))) != 0) {
						return mIdx;
					}
				}
			}
			return mIdx;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
		}

	}
}
