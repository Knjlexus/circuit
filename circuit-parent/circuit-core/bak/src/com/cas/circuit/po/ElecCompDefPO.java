package com.cas.circuit.po;

import com.cas.util.Util;

public class ElecCompDefPO {
//	元气件名称
	private String name;
//	元气件型号
	private String model;
////	元气件模型
//	private String mdlRef;
//	元气件简介
	private String desc;

	// 元气件的处理逻辑类
	private String appStateCls;
//	BaseVO中规定的属性用来保存标签的文本节点内容
	private String elementText;

	private String isCable;

	public String getName() {
		return name;
	}

	public String getDesc() {

		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppStateCls() {
		return appStateCls;
	}

	public void setAppStateCls(String appStateCls) {
		this.appStateCls = appStateCls;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		if (Util.notEmpty(model)) {
			this.model = model.trim();
		}
	}

//	public String getMdlRef() {
//		return mdlRef;
//	}
//
//	public void setMdlRef(String mdlRef) {
//		this.mdlRef = mdlRef;
//	}

	public String getElementText() {
		return elementText;
	}

	public void setElementText(String elementText) {
		this.elementText = elementText;
	}

	public String getIsCable() {
		return isCable;
	}

	public void setIsCable(String isCable) {
		this.isCable = isCable;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + name + "]";
	}

}
