package com.cas.circuit;

public class CommandSignal {
//	哪一台电机
	private String motorID;
//	电机旋转方向
	private String rotateDir = "";// MachineConsts.ROTATE_DIR_POSITIVE;
//	脉冲数量
	private PulseLong pulseCount;
//	旋转速度(多少转每分钟)
	private float rpm;
//	电机转动圈数
	private PulseLong counted;
	private boolean done;

	public String getMotorID() {
		return motorID;
	}

	public void setMotorID(String motorID) {
		this.motorID = motorID;
	}

	public String getRotateDir() {
		return rotateDir;
	}

	public void setRotateDir(String rotateDir) {
		this.rotateDir = rotateDir;
	}

	/**
	 * @return the pulseCount
	 */
	public PulseLong getPulseCount() {
		return pulseCount;
	}

	/**
	 * @param pulseCount the pulseCount to set
	 */
	public void setPulseCount(PulseLong pulseCount) {
		this.pulseCount = pulseCount;
	}

	public float getRpm() {
		return rpm;
	}

	public void setRpm(float rpm) {
		this.rpm = rpm;
	}

	/**
	 * @param b
	 */
	public void setDone(boolean done) {
		this.done = done;
	}

	/**
	 * @return the done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * @return the counted
	 */
	public PulseLong getCounted() {
		return counted;
	}

	/**
	 * @param counted the counted to set
	 */
	public void setCounted(PulseLong counted) {
		this.counted = counted;
	}

	// DO NOT MODIFY THIS METHOD
	@Override
	public String toString() {
		return "motorID=" + motorID + ", rotateDir=" + rotateDir + ", rpm=" + rpm;
	}

}
