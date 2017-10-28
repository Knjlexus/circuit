package com.cas.circuit.po;

public class ElecCompPO {
	private String id;
//	元器件模型名称
	private String mdlName;
//	元器件标签名
	private String tagName;

	@Deprecated
	private String compRef;
//	元器件额外信息
	private String elementText;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the mdlName
	 */
	public String getMdlName() {
		return mdlName;
	}

	/**
	 * @param mdlName the mdlName to set
	 */
	public void setMdlName(String mdlName) {
		this.mdlName = mdlName;
	}

	/**
	 * @return the tagName
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * @param tagName the tagName to set
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * @return the compRef
	 */
	@Deprecated
	public String getCompRef() {
		return compRef;
	}

	/**
	 * @param compRef the compRef to set
	 */
	public void setCompRef(String compRef) {
		this.compRef = compRef;
	}

	/**
	 * @return the elementText
	 */
	public String getElementText() {
		return elementText;
	}

	/**
	 * @param elementText the elementText to set
	 */
	public void setElementText(String elementText) {
		this.elementText = elementText;
	}

}
