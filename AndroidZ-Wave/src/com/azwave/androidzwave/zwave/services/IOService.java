package com.azwave.androidzwave.zwave.services;

/* Copyright 2011 Google Inc.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
* USA.
*
* Project home page: http://code.google.com/p/usb-serial-for-android/
*/

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.items.QueueManager;
import com.azwave.androidzwave.zwave.utils.Log;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

/**
 * Original author: mike wakerly (opensource@hoho.com)
 * Modified for Android Z-Wave by: peradnya (peradnya@gmail.com)
 */
public class IOService implements Runnable {

	private static final int BUFFER_SIZE = 256;
	private static final int NORMAL_SPEED = Defs.BYTE_TIMEOUT * 2;

	private enum IOServiceCommands {
		Running, Stopping, Stopped
	}

	private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private IOServiceCommands service = IOServiceCommands.Stopped;

	private UsbSerialDriver serialDriver;
	private IOServiceListener listener;
	private Log log;
	private QueueManager queue;

	private LinkedList<byte[]> listData;

	public IOService(UsbSerialDriver serialDriver, IOServiceListener listener,
			QueueManager queue, Log log) {
		this.serialDriver = serialDriver;
		this.listener = listener;
		this.log = log;
		this.queue = queue;

		listData = new LinkedList<byte[]>();
	}

	@Override
	public void run() {
		synchronized (this) {
			if (service != IOServiceCommands.Stopped) {
				log.add("IOService Error: IO Service already running.");
				return;
			}
			service = IOServiceCommands.Running;
		}

		try {
			while (service == IOServiceCommands.Running) {
				step();
			}
		} catch (Exception x) {
			for (int i = 0; i < x.getStackTrace().length; i++) {
				log.add("IOService Error: " + x.getStackTrace()[i].toString());
			}
		} finally {
			synchronized (this) {
				service = IOServiceCommands.Stopped;
			}
		}
	}

	private void step() throws IOException, InterruptedException {
		final IOServiceListener temp = getListener();
		if (temp == null) {
			return;
		}

		if (listData.size() > 0) {
			temp.onReadData(listData.poll());
		}

		int timeout = NORMAL_SPEED;
		synchronized (queue) {
			timeout = queue.isWaitingForACK() ? Defs.ACK_TIMEOUT : NORMAL_SPEED;
		}

		int len = serialDriver.read(readBuffer.array(), timeout);
		if (len > 0) {
			final byte[] data = new byte[len];
			readBuffer.get(data, 0, len);

			try {
				temp.onReadData(data);
			} catch (Exception x) {
				listData.add(data);
			}
			readBuffer.clear();
		} else {
			temp.onWriteData();
		}
	}

	public synchronized void terminate() {
		if (service == IOServiceCommands.Running) {
			service = IOServiceCommands.Stopping;
		}
	}

	private synchronized IOServiceListener getListener() {
		return listener;
	}

}
