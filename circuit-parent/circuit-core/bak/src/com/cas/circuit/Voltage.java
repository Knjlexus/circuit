package com.cas.circuit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cas.circuit.vo.Terminal;

public class Voltage implements Cloneable {
	public static final int IS_DC = 0;
	public static final int IS_AC = 1;
	public static final int IS_NULL = 2;

//	电压环境
	protected String env;
//	电压类型（DC、AC）
	protected int voltType;
//	电压值
	protected float value;
//	电压相位(0,120,240)
	private int phase;
//	电压频率（仅针对交流电）
	private float frequency = 50; // 电压正常情况下是50赫兹

	private Set<Terminal> terminals;

	private Map<String, String> data;

	public Voltage() {
	}

	/**
	 * @param env
	 * @param voltType
	 * @param value
	 * @param phase
	 */
	public Voltage(String env, int voltType, float value, int phase) {
		super();
		this.env = env;
		this.voltType = voltType;
		this.value = value;
		this.phase = phase;
	}

	/**
	 * @return the value
	 */
	public float getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(float value) {
		this.value = value;
	}

	/**
	 * @return the voltType
	 */
	public int getVoltType() {
		return voltType;
	}

	/**
	 * @param voltType the voltType to set
	 */
	public void setVoltType(int voltType) {
		this.voltType = voltType;
	}

	/**
	 * @return the env
	 */
	public String getEnv() {
		return env;
	}

	/**
	 * @param env the env to set
	 */
	public void setEnv(String env) {
		this.env = env;
	}

	/**
	 * @return the phase
	 */
	public int getPhase() {
		return phase;
	}

	/**
	 * @param phase the phase to set
	 */
	public void setPhase(int phase) {
		this.phase = phase;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((env == null) ? 0 : env.hashCode());
		result = prime * result + phase;
		result = prime * result + Float.floatToIntBits(value);
		result = prime * result + voltType;
		return result;
	}

	/*
	 * DO NOT MODIFY THIS METHOD (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Voltage)) {
			return false;
		}
		Voltage other = (Voltage) obj;
		if (env == null) {
			if (other.env != null) {
				return false;
			}
		} else if (!env.equals(other.env)) {
			return false;
		}
		if (phase != other.phase) {
			return false;
		}
		if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) {
			return false;
		}
		if (voltType != other.voltType) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Voltage clone() {
		Voltage cloned = null;
		try {
			cloned = (Voltage) super.clone();
			cloned.value = 0;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return cloned;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "env=" + env + ", voltType=" + voltType + ", value=" + value + ", phase=" + phase;
	}

	/**
	 * @param terminal
	 */
	public void addTermianl(Terminal terminal) {
		if (terminals == null) {
			terminals = new HashSet<Terminal>();
		}
		terminals.add(terminal);
	}

	/**
	 * @param terminal
	 */
	public void removeTerminal(Terminal terminal) {
		if (terminals != null) {
			terminals.remove(terminal);
		}
	}

	public void pulse() {
		if (terminals == null) {
			return;
		}
		for (Terminal terminal : terminals) {
			terminal.pulse();
		}
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

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public void putData(String key, String value) {
		if (data == null) {
			data = new HashMap<String, String>();
		}
		data.put(key, value);
	}
}
