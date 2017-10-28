/**
 * 
 */
package com.cas.circuit;

import java.util.Map;

/**
 * @author 张振宇 2015年9月28日 下午1:29:07
 */
public class SignalVolt extends Voltage {
//	指令信号
	private CommandSignal commandSignal = new CommandSignal();
//	数字信号
	private Map<String, Boolean> digitalSign;

	/**
	 * @param env
	 * @param voltType
	 * @param volt
	 * @param i
	 */
	public SignalVolt(String env, int voltType, float volt, int phase) {
		super(env, voltType, volt, phase);
	}

	/**
	 * @return the commandSignal
	 */
	public CommandSignal getCommandSignal() {
		return commandSignal;
	}

	public void changeCommandSignal(CommandSignal commandCnt) {
		commandSignal = new CommandSignal();
		commandSignal.setPulseCount(commandCnt.getPulseCount());
		commandSignal.setCounted(commandCnt.getCounted());
		commandSignal.setMotorID(commandCnt.getMotorID());
		commandSignal.setRotateDir(commandCnt.getRotateDir());
		commandSignal.setRpm(commandCnt.getRpm());
	}

	public void changeDigitalSign(Map<String, Boolean> digitalSign) {
		synchronized (this) {
//			this.digitalSign.clear();
			this.digitalSign = digitalSign;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((commandSignal == null) ? 0 : commandSignal.hashCode());
		result = prime * result + ((digitalSign == null) ? 0 : digitalSign.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof SignalVolt)) {
			return false;
		}
		SignalVolt other = (SignalVolt) obj;
		if (commandSignal == null) {
			if (other.commandSignal != null) {
				return false;
			}
		} else if (!commandSignal.equals(other.commandSignal)) {
			return false;
		}
		if (digitalSign == null) {
			if (other.digitalSign != null) {
				return false;
			}
		} else if (!digitalSign.equals(other.digitalSign)) {
			return false;
		}
		return true;
	}

	/**
	 * @return
	 * @return
	 */
	public Map<String, Boolean> getDigitalSign() {
		return this.digitalSign;
	}

}
