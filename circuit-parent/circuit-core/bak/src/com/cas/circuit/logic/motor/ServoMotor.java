package com.cas.circuit.logic.motor;

import com.cas.circuit.Voltage;
import com.cas.circuit.logic.Motor;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Terminal;
import com.cas.util.MathUtil;
import com.cas.util.Util;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;

/**
 * 伺服电机
 * @author Administrator
 */
public class ServoMotor extends Motor {
	protected Terminal u;
	protected Terminal v;
	protected Terminal w;
	protected Terminal pe;

	private float resolution = 10000;

	public ServoMotor() {
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		Jack jack_power = elecComp.getDef().getJackMap().get("Power");
		if (jack_power == null) {
			throw new RuntimeException("伺服电机没有配置电源插口，提示 <Jack id=\"Power\">");
		}
		u = jack_power.getStitch().get("1");
		v = jack_power.getStitch().get("2");
		w = jack_power.getStitch().get("3");
		pe = jack_power.getStitch().get("4");
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
//			5000Hz = 5000个/s
//			resolution = 10000个/圈
//			rpm = 5000Hz/resolution = 圈/s * 60 = 圈/min
//			5000 -> 30圈/min
			rpm = MathUtil.round(5, resultUV.getFrequency() / resolution * 60);
//			String pulseAmount = resultUV.getData("pulseAmount");
//			System.out.println("ServoMotor.onReceivedLocal()" + rpm);
//			计算方向
			int dir = R.mesurePhase(u, v, w);
//			System.out.println("_80YS25GY38Motor.onReceivedLocal(rotateDir): " + dir);
			if (dir == 0) {
				System.err.println("相位不对");
			} else {
//				 1：表示电机正转
//				-1：表示电机反转
				int rotateDir = dir / Math.abs(dir);
				motorControl.setRotateDir(rotateDir);

				if (trackControl != null) {
					trackControl.setDir(rotateDir);
				}

//				轴承旋转速度
//				转速/减速比，转化为弧度：得到弧度/MIN, 最后转化成弧度/s
				motorControl.setSpeed(Math.abs(rpm / slowdown * FastMath.TWO_PI / 60));
				if (trackControl != null) {
					trackControl.setDir(rotateDir);
//					转速/减速比*每圈移动的距离：得到mm/min,然后转化为 m/s
//					30圈/min/60 * 60mm / 1000
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
			System.out.println("_80YS25GY38Motor.onReceivedLocal(电机停止转动)");
//			电机停止转动
//			elecCompEnd();
			motorControl.setEnabled(false);
			if (trackControl != null) {
				// 停止相关的履带运动
				trackControl.setEnabled(false);
			}
		} else if (!tmp && workable) {
			System.out.println("_80YS25GY38Motor.onReceivedLocal(电机开始转动)");
//			电机开始转动
//			elecCompStart();
			motorControl.setEnabled(true);
			if (trackControl != null) {
				// 带动相关的履带运动
				trackControl.setEnabled(true);
			}
		}
	}
}
