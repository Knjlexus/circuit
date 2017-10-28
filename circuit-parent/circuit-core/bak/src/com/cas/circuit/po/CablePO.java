package com.cas.circuit.po;

public class CablePO {

	private String id;

	private String name;

	private String format;
	private String width;
	private String desc;
	private String mdlRef;

	private String core;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public CablePO() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getMdlRef() {
		return mdlRef;
	}

	public void setMdlRef(String mdlRef) {
		this.mdlRef = mdlRef;
	}

	/**
	 * @param core the core to set
	 */
	public void setCore(String core) {
		this.core = core;
	}

	/**
	 * @return
	 */
	public String getCore() {
		return core;
	}

}
