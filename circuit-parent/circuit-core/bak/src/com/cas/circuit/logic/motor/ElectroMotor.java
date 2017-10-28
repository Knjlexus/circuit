package com.cas.circuit.logic.motor;

import com.cas.circuit.Voltage;
import com.cas.circuit.logic.Motor;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Terminal;
import com.cas.util.MathUtil;
import com.cas.util.Util;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;

/**
 * 交流电动机
 * @author Administrator
 */
public class ElectroMotor extends Motor {
	protected Terminal u;
	protected Terminal v;
	protected Terminal w;
	protected Terminal pe;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);
		u = elecComp.getDef().getTerminal("U");
		v = elecComp.getDef().getTerminal("V");
		w = elecComp.getDef().getTerminal("W");
		pe = elecComp.getDef().getTerminal("PE");
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		if (pe == terminal) {
			return;
		}
//		分别测量u、v、w三者之间的电压情况
		MesureResult resultUV = R.matchRequiredVolt(Voltage.IS_AC, u, v, 380, 10);
		MesureResult resultVW = R.matchRequiredVolt(Voltage.IS_AC, v, w, 380, 10);
		MesureResult resultWU = R.matchRequiredVolt(Voltage.IS_AC, w, u, 380, 10);
//		判断三者之间是否存在电势差
		boolean matchRequiredVolt = Util.notEmpty(resultUV) && Util.notEmpty(resultWU) && Util.notEmpty(resultVW);
		float rpm = 0;
		if (matchRequiredVolt) {
			assert resultUV.getFrequency() == resultVW.getFrequency() && resultWU.getFrequency() == resultVW.getFrequency();
//			计算电机的转速
//			电机的转速 = 频率 * 60 / 2 / 电机减速比（有正负）
			rpm = MathUtil.round(5, resultUV.getFrequency() * 60 / 2);
//			计算电机本身的方向
			int dir = R.mesurePhase(u, v, w);
			if (dir == 0) {
				System.err.println("相位不对");
			} else {
//				 1：表示轴承正转
//				-1：表示轴承反转
//				if (dir < 0) {
//					System.out.println("电机反转");
//				} else {
//					System.out.println("电机正转");
//				}

				int rotateDir = (int) (dir * slowdown / Math.abs(dir) / Math.abs(slowdown));
//				if (rotateDir < 0) {
//					System.out.println("轴承反转");
//				} else {
//					System.out.println("轴承正转");
//				}

				motorControl.setRotateDir(rotateDir);
//				轴承旋转速度
//				转速/减速比，转化为弧度：得到弧度/MIN, 最后转化成弧度/s
				motorControl.setSpeed(Math.abs(rpm / slowdown * FastMath.TWO_PI / 60));
				if (trackControl != null) {
					trackControl.setDir(rotateDir);
//					转速/减速比*每圈移动的距离：得到mm/min,然后转化为 m/s
					trackControl.setSpeed(Math.abs(rpm / slowdown * movePerCycle / 60000));
				}
			}
		}

		boolean tmp = workable;
		if (rpm > 0) {
			workable = true;
		} else {
			workable = false;
		}
		if (tmp && !workable) {
//			电机停止转动
			motorControl.setEnabled(false);
			// 停止相关的履带运动
			if (trackControl != null) {
				// 停止相关的履带运动
				trackControl.setEnabled(false);
			}
			for (MotionEvent control : trackContrls) {
				control.setEnabled(false);
			}
		} else if (!tmp && workable) {
//			电机开始转动
			motorControl.setEnabled(true);
			// 带动相关的履带运动
			if (trackControl != null) {
				// 带动相关的履带运动
				trackControl.setEnabled(true);
			}
			for (MotionEvent control : trackContrls) {
				control.setEnabled(true);
			}
		}
	}

	@Override
	public void addTrackControl(MotionEvent motionClone) {
		super.addTrackControl(motionClone);
		motionClone.setEnabled(workable);
	}
}
