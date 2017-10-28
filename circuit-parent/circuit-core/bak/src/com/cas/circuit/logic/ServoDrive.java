package com.cas.circuit.logic;

import java.util.List;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.Voltage;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.ResisRelation;
import com.cas.circuit.vo.Terminal;
import com.cas.util.MathUtil;
import com.cas.util.Util;
import com.jme3.scene.Node;

/**
 * 伺服驱动器
 * @author Administrator
 */
public class ServoDrive extends BaseElectricCompLogic {
	private String controlVoltEnv;
	private float frequency;

	private float rate = 1;

//	伺服工作的条件
	private Terminal _L1C;
	private Terminal _L2C;
//	能输出控制电压的条件
	private Terminal _L1;
	private Terminal _L2;
	private Terminal _L3;
//	表示能输出控制电压
	private boolean outputEnable;
//	伺服输出控制电压
	private Terminal _U;
	private Terminal _V;
	private Terminal _W;

//	脉冲信号
	private Terminal _OPC1;
	private Terminal _PULS2;
	private boolean receivedPulse;

//	方向信号
	private Terminal _OPC2;
	private Terminal _SIGN2;
	private boolean receivedDir;
	private Terminal _POT;
	private boolean leftLimit;
	private Terminal _NOT;
	private boolean rightLimit;
	private Terminal _SERV_ON;
	private boolean servoOff;

	private Terminal _COM;
	private Terminal _ALM;
	private Terminal _ALM_;
	private ResisRelation resis;

	private Terminal _E_24V;
	private Terminal _E_0V;
	private String encoderEnv;
	private Terminal _E_A_;
	private Terminal _E_B_;
	private Terminal _E_Z_;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);
		rate = MathUtil.parseFloat(elecComp.getProperty("rate"), rate);

		controlVoltEnv = "ServoControlVoltage" + hashCode();
		encoderEnv = "ServoEncoderVoltage" + hashCode();

		_L1C = elecComp.getDef().getTerminal("L1C");
		_L2C = elecComp.getDef().getTerminal("L2C");
//		
		_L1 = elecComp.getDef().getTerminal("L1");
		_L2 = elecComp.getDef().getTerminal("L2");
		_L3 = elecComp.getDef().getTerminal("L3");
//		
		_U = elecComp.getDef().getTerminal("U");
		_V = elecComp.getDef().getTerminal("V");
		_W = elecComp.getDef().getTerminal("W");

		Jack x4 = elecComp.getDef().getJackMap().get("X4");
		_OPC1 = x4.getStitch().get("1");
		_PULS2 = x4.getStitch().get("2");
		_OPC2 = x4.getStitch().get("3");
		_SIGN2 = x4.getStitch().get("4");
//		

		_POT = x4.getStitch().get("5");
		_NOT = x4.getStitch().get("6");
		_SERV_ON = x4.getStitch().get("10");

		_COM = x4.getStitch().get("9");

		_ALM = x4.getStitch().get("7");
		_ALM_ = x4.getStitch().get("11");
		resis = new ResisRelation(_ALM, _ALM_, 0f, true);

		Jack x6 = elecComp.getDef().getJackMap().get("X6");
		_E_24V = x6.getStitch().get("1");
		_E_0V = x6.getStitch().get("2");
		_E_A_ = x6.getStitch().get("3");
		_E_B_ = x6.getStitch().get("4");
		_E_Z_ = x6.getStitch().get("5");
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
//		System.out.println("ServoDrive.onReceivedLocal()" + terminal.getPO().getId());
//		伺服放大器工作的条件
		if (terminal == _L1C || terminal == _L2C) {
			power();
//		输出电压的条件
		} else if (terminal == _L1 || terminal == _L2 || terminal == _L3) {
			output();
//		控制电机转速的条件
		} else if (terminal == _OPC1 || terminal == _PULS2) {
			speedControl();
//		控制电机转向的条件
		} else if (terminal == _OPC2 || terminal == _SIGN2) {
			dirControl();
//		左限位
		} else if (terminal == _POT) {
			MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, _COM, _POT, 24, 2);
//			有24表示没有限位
			leftLimit = result == null;
//		右限位
		} else if (terminal == _NOT) {
			MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, _COM, _NOT, 24, 2);
//			有24表示没有限位
			rightLimit = result == null;
//		伺服开
		} else if (terminal == _SERV_ON) {
			MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, _COM, _SERV_ON, 24, 2);
//			有24表示伺服打开
			servoOff = result == null;
		}

//		if (terminal == _POT || terminal == _NOT || terminal == _SERV_ON) {
//			if (leftLimit || rightLimit || servoOff) {
//				_ALM.getResisRelationMap().put(_ALM_, resis);
//				_ALM_.getResisRelationMap().put(_ALM, resis);
//				resis.setActivated(true);
//			} else {// 有信号
//				_ALM.getResisRelationMap().remove(_ALM_);
//				_ALM_.getResisRelationMap().remove(_ALM);
//				resis.setActivated(false);
//			}
//
//			Set<String> envs = _ALM_.getResidualVolt().keySet();
//			R r = null;
//			for (String env : envs) {
//				r = R.getR(env);
//				r.shareVoltage();
//			}
//		}
	}

	private void dirControl() {
		receivedDir = R.matchRequiredVolt(Voltage.IS_DC, _OPC2, _SIGN2, 24, 2) != null;
//		if (tmp && !receivedDir) {
//			log.warn("伺服放大器收到方向信号");
//			receivedDir = true;
////			rotateChange();
//		} else if (!tmp && receivedDir) {
//			log.warn("伺服放大器停止方向信号");
//			receivedDir = false;
		rotateChange();
//		}
	}

	private void speedControl() {
		MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, _OPC1, _PULS2, 24, 2);
		boolean tmp = result != null;
		if (tmp && !receivedPulse) {
			log.info("伺服放大器收到脉冲信号");
			receivedPulse = true;
			Float f = MathUtil.parseFloat(result.getData("pulseFrequency"), 0f) / rate;
			if (f != frequency) {
				frequency = f;
			}
		} else if (!tmp && receivedPulse) {
			log.warn("伺服放大器脉冲信号停止");
			receivedPulse = false;
			if (frequency != 0) {
				frequency = 0;
			}
		}
		rotateChange();
	}

	private void output() {
		MesureResult resultuv = R.matchRequiredVolt(Voltage.IS_AC, _L1, _L2, 380, 10);
		MesureResult resultvw = R.matchRequiredVolt(Voltage.IS_AC, _L2, _L3, 380, 10);
		MesureResult resultwu = R.matchRequiredVolt(Voltage.IS_AC, _L3, _L1, 380, 10);
		boolean matchRequiredVolt = Util.notEmpty(resultuv) && Util.notEmpty(resultwu) && Util.notEmpty(resultvw);
		if (matchRequiredVolt && !outputEnable) {
			log.info("伺服放大器能够输出电压");
			outputEnable = true;
			rotateChange();
		} else if (!matchRequiredVolt && outputEnable) {
			log.info("伺服放大器无法输出电压");
			outputEnable = false;
			rotateChange();
		}
	}

	private void power() {
		MesureResult result = R.matchRequiredVolt(Voltage.IS_AC, _L1C, _L2C, 380, 10);
		boolean tmp = result != null;
		if (tmp && !workable) {
			workable = true;
			log.info("伺服放大器开始工作");

			R r = R.getR(encoderEnv);
			if (r == null) {
				r = R.create(encoderEnv, Voltage.IS_DC, _E_24V, _E_0V, 24);
			}
			r.shareVoltage();
		} else if (!tmp && workable) {
			log.info("伺服放大器停止工作");
			workable = false;

			List<R> rs = R.get3Phase(controlVoltEnv);
			if (rs != null) {
				for (R r : rs) {
					r.shutPowerDown();
				}
			}

			R r = R.getR(encoderEnv);
			if (r != null) {
				r.shutPowerDown();
			}
		}
	}

	private void rotateChange() {
//		让电机停止转动
		List<R> rs = R.get3Phase(controlVoltEnv);
		if (rs != null) {
			for (R r : rs) {
				r.shutPowerDown();
			}
		}

		if (!outputEnable) {
			return;
		}
//		
//		if (leftLimit || rightLimit || servoOff) {
//			return;
//		}

		rs = R.create3Phase(controlVoltEnv, _U, _V, _W, new Terminal(), 380);
		System.out.println("ServoDrive.rotateChange() cw " + receivedDir);
		R.reversePhase(controlVoltEnv, receivedDir);
		R.set3PhaseFrequency(controlVoltEnv, frequency);

		for (R r : rs) {
//			r.getVoltage().setFrequency(frequency);
//			r.getVoltage().putData("pulseAmount", result.getData("pulseAmount"));
			r.shareVoltage();
		}
	}
}
