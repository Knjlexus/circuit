package com.cas.circuit.logic;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import com.cas.circuit.logic.Inverter.Dir;
import com.cas.util.MathUtil;

public class InvertorAssist {

	public static final String H0 = "0";
	public static final String H2 = "2";
	public static final String H4 = "4";

	private Inverter inverter;

	public InvertorAssist(Inverter inverter) {
		this.inverter = inverter;
	}

	void decode(Map<String, String> data) {
		if (data == null) {
			return;
		}

		for (Entry<String, String> entry : data.entrySet()) {
			try {
				Method paramNameMethod = getClass().getMethod(entry.getKey(), String.class);
				paramNameMethod.invoke(this, entry.getValue());
			} catch (Exception e) {
				Inverter.log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param value H2:正转、H4：反转、H0：停止;
	 */
	public void HFA(String value) {
		if (H2.equals(value)) {
			inverter.setDir(Dir.CW);
		} else if (H4.equals(value)) {
			inverter.setDir(Dir.CCW);
		} else if (H0.equals(value)) {
			inverter.setDir(null);
		}
	}

	/**
	 * 转速
	 */
	public void HEE(String value) {
		float hertz = 0;
		try {
			hertz = MathUtil.parseFloat(value, 0f) / 100;
		} catch (NumberFormatException e) {
			Inverter.log.error(e.getMessage(), e);
		}
		inverter.setFrequency(hertz);
	}

	public void HED(String value) {
		HEE(value);
	}

}
