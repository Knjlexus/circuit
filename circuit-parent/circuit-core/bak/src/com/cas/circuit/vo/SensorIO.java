package com.cas.circuit.vo;

import com.cas.circuit.po.SensorIOPO;
import com.cas.util.StringUtil;
import com.cas.util.Util;

public class SensorIO extends SwitchCtrl<SensorIOPO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1553882865544756325L;

	@Override
	protected void toValueObject() {
		super.toValueObject();
		if (!Util.isEmpty(po.getSwitchIn())) {
			resisStateIds = StringUtil.split(po.getSwitchIn());
		}
	}

}
