package com.cas.circuit.vo;

import com.cas.circuit.po.ResisStatePO;
import com.cas.util.vo.BaseVO;

public class ResisState extends BaseVO<ResisStatePO> {
	private static final long serialVersionUID = 759209439364831844L;
	private boolean isDef;

	/**
	 * 
	 */
	public ResisState() {
	}

	/**
	 * @param po
	 */
	public ResisState(ResisStatePO po) {
		super(po);
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
		isDef = "1".equals(po.getIsDef());
	}

	public String getId() {
		return po.getId();
	}

	public boolean isDef() {
		return isDef;
	}
}
