package com.cas.gas.vo;

import com.cas.gas.po.BlockRelationPO;
import com.cas.util.vo.BaseVO;

public class BlockRelation extends BaseVO<BlockRelationPO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8733409213683941995L;
	private GasPort port1;
	private GasPort port2;
	private boolean activated;

	/**
	 * 
	 */
	public BlockRelation() {
		super();
	}

	/**
	 * @param po
	 */
	public BlockRelation(BlockRelationPO po) {
		super(po);
	}

	@Override
	protected void toValueObject() {
	}

	public GasPort getPort1() {
		return port1;
	}

	public void setPort1(GasPort port1) {
		this.port1 = port1;
	}

	public GasPort getPort2() {
		return port2;
	}

	public void setPort2(GasPort port2) {
		this.port2 = port2;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public String getAnotherPortId(String portId) {
		if (po.getPortId1().equals(portId)) {
			return po.getPortId2();
		} else if (po.getPortId2().equals(portId)) {
			return po.getPortId1();
		}
		return null;
	}
}
