package com.cas.circuit.po;

public class TerminalPO {
	private String id;
	private String name;
	private String mdlName;

	private String direction;

	private String index;
	private String mark;
	private String voltage;
	private String switchIn;
	private String type;
	private String team;

	private String num;// 限制可连接导线的数量，要么是1，要么是2.

	public TerminalPO() {
	}

	public TerminalPO(String index) {
		this.index = index;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getMdlName() {
		return mdlName;
	}

	public void setMdlName(String mdlName) {
		this.mdlName = mdlName;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		if (direction != null) {
			this.direction = direction.toUpperCase();
		}
	}

	public String getVoltage() {
		return voltage;
	}

	public void setVoltage(String voltage) {
		this.voltage = voltage;
	}

	public String getSwitchIn() {
		return switchIn;
	}

	public void setSwitchIn(String switchIn) {
		this.switchIn = switchIn;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the team
	 */
	public String getTeam() {
		return team;
	}

	/**
	 * @param team the team to set
	 */
	public void setTeam(String team) {
		this.team = team;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id + "-" + name;
	}

}
