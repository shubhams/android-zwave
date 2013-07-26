package com.azwave.androidzwave.zwave.values;

import java.util.EventListener;

public interface ValueChangedListener extends EventListener {

	public void onValueChanged(Value value);

}
