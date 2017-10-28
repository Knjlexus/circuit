/**
 * 
 */
package com.cas.circuit.util;

import java.util.Map;

import com.cas.circuit.Voltage;

/**
 * @author 张振宇 2015年9月16日 上午9:15:41
 */
public class MesureResult {
	private int type;
	private float volt;
	private String evn;
	private float frequency;
	private Map<String, String> datas;

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the volt
	 */
	public float getVolt() {
		return volt;
	}

	/**
	 * @param volt the volt to set
	 */
	public void setVolt(Float volt) {
		this.volt = volt;
	}

	/**
	 * @return the evn
	 */
	public String getEvn() {
		return evn;
	}

	/**
	 * @param evn the evn to set
	 */
	public void setEvn(String evn) {
		this.evn = evn;
	}

	/**
	 * @param frequency
	 */
	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the frequency
	 */
	public float getFrequency() {
		return frequency;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MesureResult [type={" + (type == Voltage.IS_AC ? "交流" : (type == Voltage.IS_DC ? "直流" : "什么鬼电压")) + "}, volt=" + volt + ", evn={" + evn + "}]";
	}

	public void setDatas(Map<String, String> datas) {
		this.datas = datas;
	}

	public String getData(String key) {
		if (datas == null) {
			return null;
		}
		return datas.get(key);
	}

}
