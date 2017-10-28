package com.cas.circuit.logic.cylinder;

import com.cas.circuit.control.MHC2_20DControl;
import com.cas.circuit.logic.CylinderLogic;
import com.cas.gas.vo.GasPort;
import com.cas.util.Util;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;

/**
 * 气动手指
 */
public class MHC2_20DCylinderLogic extends CylinderLogic {
	/**
	 * 气缸闭合张开移动一次的距离
	 */
	private float degree;

	private MHC2_20DControl control;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		String degreeStr = elecCompMdl.getChild(0).getUserData("degree");
		if (Util.isNumeric(degreeStr)) {
			degree = FastMath.DEG_TO_RAD * Float.parseFloat(degreeStr);
		} else {
			throw new RuntimeException("没有指定气缸手指移动长度");
		}

		control = new MHC2_20DControl(degree, 1f);
		control.setEnabled(false);
		elecCompMdl.addControl(control);
	}

	@Override
	public void onReceivedGP(GasPort gasPort) {
//		判断两个气嘴的气压
		if (portA.isPressure() && !portB.isPressure()) { // 手指抓紧
			control.clamp();
		} else if (!portA.isPressure() && portB.isPressure()) { // 手指松开
			control.unclamp();
		} else {
//			System.out.println("气缸不动" + elecComp);
		}
	}
}
