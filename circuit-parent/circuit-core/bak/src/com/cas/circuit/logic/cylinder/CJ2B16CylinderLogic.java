package com.cas.circuit.logic.cylinder;

import com.cas.circuit.control.StickControl;
import com.cas.circuit.logic.CylinderLogic;
import com.cas.gas.vo.GasPort;
import com.cas.robot.common.util.JmeUtil;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class CJ2B16CylinderLogic extends CylinderLogic {
	protected StickControl control;

	public CJ2B16CylinderLogic() {
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		Spatial stick = elecCompMdl.getChild("stick");
		if (stick == null) {
			throw new RuntimeException("气缸模型的杆子模型不存在或者名称不是‘stick’");
		}

//		driveds.add(stick);

		String length = null;
		if (elecCompMdl.getUserData("length") != null) {
			length = elecCompMdl.getUserData("length");
		} else {
			length = stick.getUserData("length");
		}

		if (length == null) {
			throw new RuntimeException("没有指定气缸杆子的长度:" + elecCompMdl);
		}

		float stickLen = Float.parseFloat(length);
		Vector3f dir = null;
		if (elecCompMdl.getUserData("dir") != null) {
			dir = JmeUtil.getVector3f(String.valueOf(elecCompMdl.getUserData("dir")));
		} else {
			dir = JmeUtil.getVector3f(String.valueOf(stick.getUserData("dir")));
		}

		float speed = .2f; // 默认速度为0.2;
		if (elecCompMdl.getUserData("speed") != null) {
			speed = Float.parseFloat(String.valueOf(elecCompMdl.getUserData("speed")));
		}

		control = new StickControl(driveds, dir, stickLen, speed);
		control.setEnabled(false);
		stick.addControl(control);
	}

	@Override
	public void onReceivedGP(GasPort gasPort) {
//			判断两个气嘴的气压
		if (portA.isPressure() && !portB.isPressure()) { // 杆子伸出
			control.stickOut();
		} else if (!portA.isPressure() && portB.isPressure()) { // 杆子收回
			control.stickIn();
		} else {
			control.setEnabled(false);
		}
	}
}
