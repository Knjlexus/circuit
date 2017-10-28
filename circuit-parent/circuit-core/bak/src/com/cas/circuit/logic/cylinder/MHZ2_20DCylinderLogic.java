package com.cas.circuit.logic.cylinder;

import com.cas.circuit.control.MHZ2_20DControl;
import com.cas.circuit.logic.CylinderLogic;
import com.cas.gas.vo.GasPort;
import com.cas.util.Util;
import com.jme3.scene.Node;

/**
 * 气动手指
 */
public class MHZ2_20DCylinderLogic extends CylinderLogic {

	private MHZ2_20DControl control;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		String lengthStr = elecCompMdl.getChild(0).getUserData("length");
		float length;
		if (Util.isNumeric(lengthStr)) {
			length = Float.parseFloat(lengthStr);

		} else {
			throw new RuntimeException("没有指定气缸手指移动长度");
		}

		control = new MHZ2_20DControl(length, 1f);
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
