package com.cas.gas.po;

/**
 * 通阻状态
 * @功能 BlockPO.java
 * @作者 CWJ
 * @创建日期 2016年5月18日
 * @修改人 CWJ
 */
public class BlockRelationPO {
	private String portId1;
	private String portId2;
	private String type;

	public String getPortId1() {
		return portId1;
	}

	public void setPortId1(String portId1) {
		this.portId1 = portId1;
	}

	public String getPortId2() {
		return portId2;
	}

	public void setPortId2(String portId2) {
		this.portId2 = portId2;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
