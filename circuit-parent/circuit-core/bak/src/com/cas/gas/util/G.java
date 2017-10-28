package com.cas.gas.util;

import org.apache.log4j.Logger;

import com.cas.gas.vo.GasPort;

public class G {
	private static final Logger log = Logger.getLogger(G.class);

	private static G g;
	private GasPort startPort;

	public static final G g() {
		if (g == null) {
			g = new G();
		}
		return g;
	}

	public void setStartPort(GasPort startPort) {
		if (startPort == null) {
			throw new RuntimeException("设置初始气管为null");
		}
		this.startPort = startPort;
	}

	public void refreshGasPressure() {
		if (startPort == null) {
			log.warn("未设置初始气管！");
			return;
		}
		startPort.addPressure(null);
	}

	public void clearGasPressure() {
		if (startPort == null) {
			log.warn("未设置初始气管！");
			return;
		}
		startPort.removePressure();
	}
}
