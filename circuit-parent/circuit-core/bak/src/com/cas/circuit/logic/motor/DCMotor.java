package com.cas.circuit.logic.motor;

import com.cas.circuit.Voltage;
import com.cas.circuit.logic.Motor;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Terminal;
import com.cas.util.MathUtil;
import com.jme3.scene.Node;

/**
 * 直流电机
 * @author Administrator
 */
public class DCMotor extends Motor {
	private Terminal _24v;
	private Terminal _0v;

//	额定电压
	private float ratedVolt = 24;
	private float wheelSpeed = 1f;
	private float trackSpeed = 0.1f;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		_24v = elecComp.getDef().getTerminal("24V");
		_0v = elecComp.getDef().getTerminal("0V");

		ratedVolt = MathUtil.parseFloat(elecComp.getProperty("ratedVolt"), ratedVolt);
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		super.onReceivedLocal(terminal);
		int dir = 0;
		MesureResult result1 = R.matchRequiredVolt(Voltage.IS_DC, _24v, _0v, ratedVolt, 2);
		MesureResult result2 = R.matchRequiredVolt(Voltage.IS_DC, _0v, _24v, ratedVolt, 2);
		boolean tmp = workable;
		if (result1 != null) {
			dir = 1;
		} else if (result2 != null) {
			dir = -1;
		}
		if (dir == 0) {
			System.out.println("电压不满足工作条件！");
			workable = false;
		} else {
			int rotateDir = dir / Math.abs(dir);
			motorControl.setRotateDir(rotateDir);
			motorControl.setSpeed(wheelSpeed);
			if (trackControl != null) {
				trackControl.setDir(rotateDir);
				trackControl.setSpeed(trackSpeed);
			}
			workable = true;
		}
		if (tmp && !workable) {
			System.out.println("DCMotor.onReceivedLocal(电机停止转动)");
			motorControl.setEnabled(false);
			// 停止相关的履带运动
			trackControl.setEnabled(false);
		} else if (!tmp && workable) {
			System.out.println("DCMotor.onReceivedLocal(电机开始转动)");
			motorControl.setEnabled(true);
			// 带动相关的履带运动
			trackControl.setEnabled(true);
		}
	}
}
