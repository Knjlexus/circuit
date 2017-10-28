package com.cas.gas.po;

/**
 * 通阻状态信息对象
 * @功能 BlockStatePO.java
 * @作者 CWJ
 * @创建日期 2016年5月18日
 * @修改人 CWJ
 */
public class BlockStatePO {
	private String id;
	private String isDef;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIsDef() {
		return isDef;
	}

	public void setIsDef(String isDef) {
		this.isDef = isDef;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "id=" + id + ", isDef=" + isDef;
	}

}
