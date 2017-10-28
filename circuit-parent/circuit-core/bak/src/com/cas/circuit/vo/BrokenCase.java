package com.cas.circuit.vo;

import com.cas.circuit.po.BrokenCasePO;
import com.cas.util.vo.BaseVO;

/**
 * @author Administrator
 */
public class BrokenCase extends BaseVO<BrokenCasePO> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2897883305588904701L;
	// 故障名称
	private String name;
	// 对应机型
	private String model;
	// 故障路径
	private String trafficPath;

	@Override
	protected void toValueObject() {
		name = po.getName();
		model = po.getModel();
		trafficPath = po.getCasePath();
	}

	public String getName() {
		return name;
	}

	public String getModel() {
		return model;
	}

	public String getTrafficPath() {
		return trafficPath;
	}

	@Override
	protected String getLocalKey() {
		return po.getModel();
	}

}
