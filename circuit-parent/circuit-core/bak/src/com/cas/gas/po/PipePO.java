package com.cas.gas.po;

/**
 * 气管信息对象
 * @功能 PipePO.java
 * @作者 CWJ
 * @创建日期 2016年5月18日
 * @修改人 CWJ
 */
public class PipePO {
	// 气管连接气口1
	private String gasPort1;
	// 气管连接气口2
	private String gasPort2;
	// 用户定义气管名称（预留）
	private String mark;
	// 气管管径（预留）
	private String width;
	// 气管颜色
	private String color;

	public String getGasPort1() {
		return gasPort1;
	}

	public void setGasPort1(String gasPort1) {
		this.gasPort1 = gasPort1;
	}

	public String getGasPort2() {
		return gasPort2;
	}

	public void setGasPort2(String gasPort2) {
		this.gasPort2 = gasPort2;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
