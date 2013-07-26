package com.azwave.androidzwave.zwave.xml;

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

import com.azwave.androidzwave.zwave.utils.SafeCast;

public class Product {

	private short manufacturerID;
	private short productType;
	private short productID;
	private String productName;
	private String configPath;

	public Product(short manufacturerID, short productType, short productID,
			String productName, String configName) {
		this.manufacturerID = manufacturerID;
		this.productType = productType;
		this.productID = productID;
		this.productName = productName;
		this.configPath = configName;
	}

	public static long getKey(short manufacturerID, short productType,
			short productID) {
		long key = SafeCast.toLong(manufacturerID) << 32
				| SafeCast.toLong(productType) << 16
				| SafeCast.toLong(productID);
		return key;
	}

	public long getKey() {
		return getKey(manufacturerID, productType, productID);
	}

	public short getManufacturerId() {
		return manufacturerID;
	}

	public short getProductType() {
		return productType;
	}

	public short getProductId() {
		return productID;
	}

	public String getProductName() {
		return productName;
	}

	public String getConfigPath() {
		return configPath;
	}

}
