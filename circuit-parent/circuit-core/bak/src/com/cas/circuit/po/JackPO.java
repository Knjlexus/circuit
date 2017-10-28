package com.cas.circuit.po;

public class JackPO {
	private String id;
	/**
	 * 当前插口对应线缆类型
	 */
	private String cable;
	/**
	 * 当前插口对应线缆的插头制式
	 */
	private String format;
	private String mdlName;
	private String name;
	private String module;

	// 插座的方向
	private String jackDirection;
	// 插座上线连出去的方向
	private String direction;
	private String rotation;
	private String isPositive;
//	private String flyHeight;
//	private String flydistance;
	private String core;

	private String belongElecComp;
	
	public JackPO() {
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

	public String getCable() {
		return cable;
	}

	public void setCable(String cable) {
		this.cable = cable;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getMdlName() {
		return mdlName;
	}

	public void setMdlName(String mdlName) {
		this.mdlName = mdlName;
	}

	public String getIsPositive() {
		return isPositive;
	}

	public void setIsPositive(String isPositive) {
		this.isPositive = isPositive;
	}

//	public String getFlyHeight() {
//		return flyHeight;
//	}
//
//	public void setFlyHeight(String flyHeight) {
//		this.flyHeight = flyHeight;
//	}
//
//	public String getFlydistance() {
//		return flydistance;
//	}
//
//	public void setFlydistance(String flydistance) {
//		this.flydistance = flydistance;
//	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getJackDirection() {
		return jackDirection;
	}

	public void setJackDirection(String jackDirection) {
		this.jackDirection = jackDirection;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getRotation() {
		return rotation;
	}

	public void setRotation(String rotation) {
		this.rotation = rotation;
	}

	/**
	 * @return
	 */
	public String getCore() {
		return core;
	}

	/**
	 * @param core the core to set
	 */
	public void setCore(String core) {
		this.core = core;
	}

	public String getBelongElecComp() {
		return belongElecComp;
	}

	public void setBelongElecComp(String belongElecComp) {
		this.belongElecComp = belongElecComp;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id + "--" + name;
	}
}
