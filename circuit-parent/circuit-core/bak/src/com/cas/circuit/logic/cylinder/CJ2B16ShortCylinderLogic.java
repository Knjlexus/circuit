package com.cas.circuit.logic.cylinder;

import com.cas.gas.vo.GasPort;
import com.jme3.scene.Node;
/**
 * 单作用气缸
 * @功能 SingleCylinderLogic.java
 * @作者 Cwj
 * @创建日期 2016年8月18日
 * @修改人 Cwj
 */
public class CJ2B16ShortCylinderLogic extends CJ2B16CylinderLogic {

	public CJ2B16ShortCylinderLogic() {
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);
	}

	@Override
	public void onReceivedGP(GasPort gasPort) {
//			判断两个气嘴的气压
		if (portA.isPressure()) { // 杆子伸出
			control.stickOut();
		} else if (!portA.isPressure()) { // 杆子收回
			control.stickIn();
		}
	}
}
