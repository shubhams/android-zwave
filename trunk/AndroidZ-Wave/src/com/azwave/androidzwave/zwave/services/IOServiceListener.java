package com.azwave.androidzwave.zwave.services;

import java.io.IOException;
import java.util.EventListener;

public interface IOServiceListener extends EventListener {

	public void onReadData(byte[] data) throws IOException;

	public void onWriteData() throws IOException;

}
