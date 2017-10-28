package com.cas.gas.vo;

import com.cas.gas.po.BlockStatePO;
import com.cas.util.vo.BaseVO;

/**
 * 通阻关系信息封装对象
 * @功能 BlockState.java
 * @作者 CWJ
 * @创建日期 2016年5月18日
 * @修改人 CWJ
 */
public class BlockState extends BaseVO<BlockStatePO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3971869487577365723L;
	private boolean isDef;

	/**
	 * 
	 */
	public BlockState() {
	}

	/**
	 * @param po
	 */
	public BlockState(BlockStatePO po) {
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
