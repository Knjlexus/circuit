package com.cas.circuit.vo;

import com.cas.circuit.po.SignalMappingPO;
import com.cas.util.vo.BaseVO;

public class SignalMapping extends BaseVO<SignalMappingPO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -996399561874181126L;

	public SignalMapping() {
	}

	public String getStitchName() {
		return ((Jack) parent).getPO().getName() + "-" + po.getStitch();
	}

}
