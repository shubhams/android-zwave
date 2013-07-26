package com.azwave.androidzwave.zwave.items;

import java.util.Comparator;

public class QueueItemComparator implements Comparator<QueueItem> {

	@Override
	public int compare(QueueItem arg0, QueueItem arg1) {
		int priority0 = arg0.getPriority().ordinal();
		int priority1 = arg1.getPriority().ordinal();
		if (priority0 < priority1) {
			return -1;
		} else if (priority0 > priority1) {
			return 1;
		} else {
			if (arg0.getQueueCount() < arg1.getQueueCount()) {
				return -1;
			} else if (arg0.getQueueCount() > arg1.getQueueCount()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
