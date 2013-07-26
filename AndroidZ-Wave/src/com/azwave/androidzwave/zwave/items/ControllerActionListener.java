package com.azwave.androidzwave.zwave.items;

import java.util.EventListener;

import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerError;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;

public interface ControllerActionListener extends EventListener {

	public void onAction(ControllerState state, ControllerError error,
			Object context);

}
