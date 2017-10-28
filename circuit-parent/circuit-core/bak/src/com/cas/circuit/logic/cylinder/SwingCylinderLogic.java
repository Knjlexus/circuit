package com.cas.circuit.logic.cylinder;

import com.cas.circuit.control.SwingControl;
import com.cas.circuit.logic.CylinderLogic;
import com.cas.gas.vo.GasPort;
import com.cas.robot.common.util.JmeUtil;
import com.cas.util.Util;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class SwingCylinderLogic extends CylinderLogic {
	private SwingControl control;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

//		旋转方向
		String dir = elecCompMdl.getChild(0).getUserData("dir");
		Vector3f rotDir = JmeUtil.getVector3f(dir);

//		旋转的角度
		String degreeStr = elecCompMdl.getChild(0).getUserData("degree");
		float degree;
		if (Util.isNumeric(degreeStr)) {
			degree = FastMath.DEG_TO_RAD * Float.parseFloat(degreeStr);
		} else {
			throw new RuntimeException("没有指定气缸手指移动长度");
		}

		Spatial axis = elecCompMdl.getChild("axis");
		driveds.add(axis);

		assert driveds.size() <= 2;
		control = new SwingControl(driveds, rotDir, degree, 1f);
		control.setEnabled(false);
		axis.addControl(control);
	}

	@Override
	public void onReceivedGP(GasPort gasPort) {
//		判断两个气嘴的气压
		if (portA.isPressure() && !portB.isPressure()) { // 杆子伸出
			control.clamp();
		} else if (!portA.isPressure() && portB.isPressure()) { // 杆子收回
			control.unclamp();
		} else {
//			System.out.println("气缸不动" + elecComp);
		}
	}
}
