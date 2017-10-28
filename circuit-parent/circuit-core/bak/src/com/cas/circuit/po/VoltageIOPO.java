package com.cas.circuit.po;

public class VoltageIOPO {

	private String type;
	private String term1Id;
	private String term2Id;
	private String value;
	private String switchIn;
	private String deviation;
	private String group;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTerm1Id() {
		return term1Id;
	}

	public void setTerm1Id(String term1Id) {
		this.term1Id = term1Id;
	}

	public String getTerm2Id() {
		return term2Id;
	}

	public void setTerm2Id(String term2Id) {
		this.term2Id = term2Id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSwitchIn() {
		return switchIn;
	}

	public void setSwitchIn(String switchIn) {
		this.switchIn = switchIn;
	}

	/**
	 * @return the deviation
	 */
	public String getDeviation() {
		return deviation;
	}

	/**
	 * @param deviation the deviation to set
	 */
	public void setDeviation(String deviation) {
		this.deviation = deviation;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VoltageIO:" + switchIn + "-" + term1Id + "-" + term2Id + ":" + value;
	}
}
