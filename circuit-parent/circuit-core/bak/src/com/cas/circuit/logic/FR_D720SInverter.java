package com.cas.circuit.logic;

import java.util.List;
import java.util.Map;

import com.cas.circuit.Voltage;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Terminal;
import com.cas.util.Util;
import com.jme3.scene.Node;

/**
 * 变频器
 */
public class FR_D720SInverter extends Inverter {
	protected Terminal _l;
	protected Terminal _n;
	private Terminal _u;
	private Terminal _v;
	private Terminal _w;

//	PU网线接口中针脚
	private Terminal _rda;
	private Terminal _rdb;
	private Terminal _sda;
	private Terminal _sdb;

	private InvertorAssist assist;
//	需要改变变频器输出的控制电压
	private boolean voltNeedChange;
	private String controlVoltEnv;
	private Dir dir;
//	控制电压的频率
	private float frequency;

	public FR_D720SInverter() {
	}

	enum Dir {
		/**
		 * 顺时针方向（clockwise）
		 */
		CW(120),
		/**
		 * 反时针方向（counterclockwise）
		 */
		CCW(-120);
//		相位差
		private int phase;

		Dir(int phase) {
			this.phase = phase;
		}

		public int getPhase() {
			return phase;
		}
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		assist = new InvertorAssist(this);
		controlVoltEnv = "Invertor_" + hashCode() + "_ControlVoltage";

		_l = elecComp.getDef().getTerminal("L");
		_n = elecComp.getDef().getTerminal("N");

		_u = elecComp.getDef().getTerminal("U");
		_v = elecComp.getDef().getTerminal("V");
		_w = elecComp.getDef().getTerminal("W");

		Jack jack_pu = elecComp.getDef().getJackMap().get("PU");
		_rda = jack_pu.getStitch().get("RDA");
		_rdb = jack_pu.getStitch().get("RDB");
		_sda = jack_pu.getStitch().get("SDA");
		_sdb = jack_pu.getStitch().get("SDB");
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		super.onReceivedLocal(terminal);
		if (_l == terminal || _n == terminal) {
//			分别测量uvw三者之间的电压情况
			MesureResult result = R.matchRequiredVolt(Voltage.IS_AC, _l, _n, 220, 10);

//			判断三者之间是否存在电势差

			boolean tmp = Util.notEmpty(result);

			List<R> controlVoltPower = R.get3Phase(controlVoltEnv);
			if (tmp && !workable) {
				workable = true;
				if (controlVoltPower == null) {
					controlVoltPower = R.create3Phase(controlVoltEnv, _u, _v, _w, new Terminal(), 380);
				}
				R.set3PhaseFrequency(controlVoltEnv, 50);
				R.reversePhase(controlVoltEnv, dir != Dir.CW);

				for (R r : controlVoltPower) {
					r.shareVoltage();
				}
			} else if (!tmp && workable) {
				if (controlVoltPower != null) {
					for (R r : controlVoltPower) {
						r.shutPowerDown();
					}
				}
				workable = false;
			}
		} else if (_rda == terminal || _rdb == terminal) {
//			处理PLC侧发来的数据
			MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, _rda, _rdb, 5, 1);
			if (result != null) {
				Map<String, String> data = R.getR(result.getEvn()).getVoltage().getData();
				assist.decode(data);
			}

			if (voltNeedChange) {
			}
		} else {

		}

	}

	/**
	 * 设置要控制电机旋转的方向
	 * @param dir
	 */
	public void setDir(Dir dir) {
		this.voltNeedChange = dir == this.dir;
		this.dir = dir;
	}

	/**
	 * 设置要控制电机旋转的速度
	 * @param frequency
	 */
	public void setFrequency(float frequency) {
		this.voltNeedChange = frequency == this.frequency;
		this.frequency = frequency;
	}
}
