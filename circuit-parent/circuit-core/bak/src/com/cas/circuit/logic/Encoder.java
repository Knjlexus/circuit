package com.cas.circuit.logic;

import java.util.Set;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.Voltage;
import com.cas.circuit.control.EncoderControl;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.ResisRelation;
import com.cas.circuit.vo.Terminal;
import com.cas.robot.common.util.Pool;
import com.cas.util.MathUtil;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 编码器
 */
public class Encoder extends BaseElectricCompLogic {
	public static final String KEY_PulsePerMillis = "PulsePerMillis";
	public static final String KEY_PulseDir = "PulseDir";

	private Terminal _24v;
	private Terminal _0v;
	private Terminal _A;
	private Terminal _B;
	private Terminal _Z;

	private EncoderControl encoderControl;
	private int resolution = 2000;
//	每个脉冲对应的角度(非弧度)
	private float degPerPulse;
	private float degPerMillis;

	private int dir; // 物体旋转的方向

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);
		_24v = elecComp.getDef().getTerminal("24V");
		_0v = elecComp.getDef().getTerminal("0V");

		_A = elecComp.getDef().getTerminal("A");
		_B = elecComp.getDef().getTerminal("B");
		_Z = elecComp.getDef().getTerminal("Z");

		resolution = MathUtil.parseInt(elecComp.getProperty("resolution"), resolution);

		degPerPulse = 360f / resolution;

//		检测的物体
		Spatial detection = elecCompMdl.getParent().getChild(elecComp.getProperty("detection"));
		elecCompMdl.addControl(encoderControl = new EncoderControl(this, detection));
		encoderControl.setEnabled(false);
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		super.onReceivedLocal(terminal);
		if (_24v == terminal || _0v == terminal) {
//			编码器工作条件
			MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, _24v, _0v, 24, 1);
			boolean tmp = result != null;

			if (tmp && !workable) {
				workable = true;
			} else if (!tmp && workable) {
				workable = false;
//				
			}
			encoderControl.setEnabled(workable);
		}
	}

	/**
	 * @param deg 一毫秒内转过的角度
	 */
	public void setDegPerMillis(float deg) {
		this.degPerMillis = deg;
		System.out.println("Encoder.setDegPerMillis("+degPerMillis+")");
	}

	public void onTargetStart() {
//		System.out.println("Encoder.onTargetStart()");
		Pool.getCircuitPool().execute(new Runnable() {
			public void run() {
//				设置abz与0v接通
				ResisRelation resis = new ResisRelation(_0v, _A, 0f, true);
				_A.getResisRelationMap().put(_0v, resis);
				_0v.getResisRelationMap().put(_A, resis);
				resis.setActivated(true);

				resis = new ResisRelation(_0v, _B, 0f, true);
				_B.getResisRelationMap().put(_0v, resis);
				_0v.getResisRelationMap().put(_B, resis);
				resis.setActivated(true);

				Set<String> envs = _0v.getResidualVolt().keySet();
				R r = null;
				for (String env : envs) {
					r = R.getR(env);
					r.getVoltage().putData("PulsePerMillis", String.valueOf(degPerMillis / degPerPulse));
					r.getVoltage().putData("PulseDir", String.valueOf(dir));
					r.shareVoltage();
				}
			}
		});
	}

	public void onTargetStop() {
//		System.out.println("Encoder.onTargetStop()");
		Pool.getCircuitPool().execute(new Runnable() {
			public void run() {
//				设置abz与0v接通
				ResisRelation resis = _A.getResisRelationMap().remove(_0v);
				_0v.getResisRelationMap().remove(_A);
				if (resis != null) {
					resis.setActivated(false);
				}

				resis = _B.getResisRelationMap().remove(_0v);
				_0v.getResisRelationMap().remove(_B);
				if (resis != null) {
					resis.setActivated(false);
				}

				Set<String> envs = _0v.getResidualVolt().keySet();
				R r = null;
				for (String env : envs) {
					r = R.getR(env);
					r.getVoltage().putData("PulsePerMillis", null);
					r.getVoltage().putData("PulseDir", null);
					r.shareVoltage();
				}
			}
		});
	}

	public void setDir(int dir) {
		this.dir = dir;
	}
//	通电+转起来了
//	断电或没转
}
