package com.azwave.androidzwave.zwave.utils;

import java.io.InputStream;

import org.w3c.dom.Document;

import android.content.Context;
import android.content.res.AssetManager;

public class XMLManagerAndroid extends XMLManager {

	private Context context;
	private AssetManager assetManager;

	public XMLManagerAndroid(Context context, Log log) {
		super(log);
		this.context = context;
		this.assetManager = context.getAssets();
	}

	protected Document readAsset(String filename) throws Exception {
		InputStream inputStream = assetManager.open(filename);
		db = dbf.newDocumentBuilder();
		return db.parse(inputStream);
	}

	public void readDeviceClasses() {
		readDeviceClasses("device_classes.xml");
	}

	public void readManufacturerSpecific() {
		readManufacturerSpecific("manufacturer_specific.xml");
	}
}
