package com.cas.circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cas.circuit.util.MultiPower;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.ElecComp;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.LightIO;
import com.cas.circuit.vo.Terminal;

public abstract class ElecCompCPU extends BaseElectricCompLogic {
	public static final float CB_SIGNAL_VOLT = 24;
	public static final float JF_SIGNAL_VOLT = 5;
	public static final float COP_SIGNAL_VOLT = 24;

	public static final String Power_Evn_Filter = MultiPower.SIGNAL + "=false";
	public static final String Signal_Evn_Filter = MultiPower.SIGNAL + "=true";

//	默认元器件无法工作
	protected boolean workable;
//	每一个插孔提供的电压环境前缀,防止使用多台机床时数据混乱
	protected String EVN_NAME_PRE;

	protected List<Jack> cbx_jacks = new ArrayList<Jack>();
	protected List<String> cb_evn_names = new ArrayList<String>();
	protected List<Jack> jf_jacks = new ArrayList<Jack>();
	protected List<String> jf_evn_names = new ArrayList<String>();
	protected List<Jack> cop_jacks = new ArrayList<Jack>();
	protected List<String> cop_evn_names = new ArrayList<String>();
	protected List<Jack> jfr_jacks = new ArrayList<Jack>();
	protected List<String> jfr_evn_names = new ArrayList<String>();
	protected String[][] jfrEnvEnds = new String[][] { { "1", "11" }, { "3", "13" }, { "5", "15" } };

	public ElecCompCPU() {
		EVN_NAME_PRE = String.valueOf(hashCode());
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.me.appState.electric.BaseElectricCompState#setElecComp(com.cas.me.data.layout.ElecComp)
	 */
	@Override
	public void setElecComp(ElecComp elecComp) {
		super.setElecComp(elecComp);
		Map<String, Jack> jackMap = elecComp.getDef().getJackMap();
		for (Jack jack : jackMap.values()) {
			if ("JF".equals(jack.getPO().getFormat())) {
				jf_evn_names.add(EVN_NAME_PRE + jack.getPO().getName());
				jf_jacks.add(jack);
			} else if ("CB".equals(jack.getPO().getFormat())) {
				cb_evn_names.add(EVN_NAME_PRE + jack.getPO().getName());
				cbx_jacks.add(jack);
			} else if ("COP".equals(jack.getPO().getFormat())) {
				cop_evn_names.add(EVN_NAME_PRE + jack.getPO().getName());
				cop_jacks.add(jack);
			} else if (jack.getPO().getFormat().indexOf("JFR") != -1) {
				jfr_evn_names.add(EVN_NAME_PRE + jack.getPO().getName());
				jfr_jacks.add(jack);
			} else {
				log.info(jack.getPO().getFormat() + "这个插孔不能产生信号电,关于'产生信号电'不理解的,可以问振宇");
			}
		}
	}

	/**
	 * @param terminal
	 */
	public final void onPulse(Terminal terminal) {
		if (terminal.getParent() instanceof Jack) {
			Jack jack = (Jack) terminal.getParent();
			if (CfgConst.BROKEN_STATE_BREAK.equals(jack.getBrokenState())) {
				return;
			}
		}
		TermTeam team = terminal.getTeam();
		team.signIn(terminal);
		if (!team.resetIfReady()) {
			return;
		}
		onPulseLocal(terminal);
	}

	/**
	 * @param terminal
	 */
	protected void onPulseLocal(Terminal terminal) {

	}

	public Map<String, Jack> getJackMap() {
		return getElecComp().getDef().getJackMap();
	}

	/**
	 * @param jackNm 信号发生的插孔名称
	 * @param index 具体的针脚号
	 */
	public Terminal getStitch(String jackName, String index) {
		Jack jack = getJackMap().get(jackName);
		if (jack == null) {
			System.err.println("没有找到名称为: " + jackName + "的插孔");
		}
		Terminal stitch = jack.getStitch().get(index);
		if (stitch == null) {
			System.err.println(jackName + "插孔中没有找到第: " + index + "跟针脚");
		}
		return stitch;
	}

	/**
	 * @param jackNm 信号发生的插孔名称
	 * @param startIndex 具体的针脚号
	 * @param endIndex 具体的针脚号
	 * @param sign 电压对象
	 */
	public void sendSignal(String jackNm, String startIndex, String endIndex, CommandSignal sign) {
		synchronized (this) {
			// 判断当前插口是否允许发送指令
			Jack jack = getJackMap().get(jackNm);
			if (jack.isStopSend()) {
				log.warn("无法发送信号!![" + jackNm + "]");
				return;
			}
			Terminal startTerminal = getStitch(jackNm, startIndex);
			Terminal endTerminal = getStitch(jackNm, endIndex);
			String env = EVN_NAME_PRE + jackNm;
			R r = R.getR(env);
			if (r == null) {
				r = R.createSignal(env, startTerminal, endTerminal, 24);
				r.shareVoltage();
			}

			SignalVolt sv = (SignalVolt) r.getVoltage();
			sv.changeCommandSignal(sign);
			r.shareVoltage();
		}
	}

	/**
	 * 开元器件指示灯<br>
	 * 产生信号电<br>
	 */
	protected void elecCompStart() {
		log.info("开始启动" + elecComp.getPO().getTagName());
//		System.err.println(this.getClass().getCanonicalName() + ".elecCompStart(start...)");
		for (LightIO lightIO : getElecComp().getDef().getLightIOs()) {
			lightIO.openLight();
		}
//		信号电压起源
		R r = null;
		Terminal ipHigher = null;
		Terminal ipLower = null;
//		CBX - CB104 CB105 CB106 CB107
		if (cb_evn_names != null) {
			for (int i = 0; i < cb_evn_names.size(); i++) {
				r = R.getR(cb_evn_names.get(i));
				if (r == null) {
					ipLower = cbx_jacks.get(i).getStitch().get("1");
					ipHigher = cbx_jacks.get(i).getStitch().get("2");
					if (ipHigher == null || ipLower == null) {
//						System.err.println(getClass() + cbx_jacks.get(i).getPO().getId() + "中找不到1 或 2号针脚");
						continue;
					}
					r = R.createSignal(cb_evn_names.get(i), ipHigher, ipLower, CB_SIGNAL_VOLT);
				}
				r.shareVoltage();
			}
		}
//		JF - JD1A JD1B
		if (jf_evn_names != null) {
			for (int i = 0; i < jf_evn_names.size(); i++) {
				r = R.getR(jf_evn_names.get(i));
				if (r == null) {
					ipHigher = jf_jacks.get(i).getStitch().get("3");
					ipLower = jf_jacks.get(i).getStitch().get("13");
					if (ipHigher == null || ipLower == null) {
//						System.err.println(getClass() + jf_jacks.get(i).getPO().getId() + "中找不到3 或 13号针脚");
						continue;
					}
					r = R.createSignal(jf_evn_names.get(i), ipHigher, ipLower, JF_SIGNAL_VOLT);
				}
				r.shareVoltage();
			}
		}
//		COP - COP10B COP10B
		if (cop_jacks != null) {
			for (int i = 0; i < cop_evn_names.size(); i++) {
				r = R.getR(cop_evn_names.get(i));
				if (r == null) {
					ipHigher = cop_jacks.get(i).getStitch().get("1");
					ipLower = cop_jacks.get(i).getStitch().get("3");
					if (ipHigher == null || ipLower == null) {
						System.err.println(getClass() + cop_jacks.get(i).getPO().getId() + "中找不到1 或 3号针脚");
						continue;
					}
					r = R.createSignal(cop_evn_names.get(i), ipHigher, ipLower, COP_SIGNAL_VOLT);
				}
				r.shareVoltage();
			}
		}
//		jfr* - jfr1 jfr2
		if (jfr_jacks != null) {
			String env = null;
			for (int i = 0; i < jfr_evn_names.size(); i++) {
				for (int j = 0; j < jfrEnvEnds.length; j++) {
					env = jfr_evn_names.get(i) + jfrEnvEnds[j][0] + "_" + jfrEnvEnds[j][1];
					r = R.getR(env);
					if (r == null) {
						ipHigher = jfr_jacks.get(i).getStitch().get(jfrEnvEnds[j][0]);
						ipLower = jfr_jacks.get(i).getStitch().get(jfrEnvEnds[j][1]);
						if (ipHigher == null || ipLower == null) {
//							System.err.println(getClass() + jfr_jacks.get(i).getPO().getId() + "中找不到" + jfrEnvEnds[j][0] + " 或 " + jfrEnvEnds[j][1] + "号针脚");
							continue;
						}
						r = R.createSignal(env, ipHigher, ipLower, COP_SIGNAL_VOLT);
					}
					r.shareVoltage();
				}
			}
		}
//		System.err.println(this.getClass().getCanonicalName() + ".elecCompStart(end...)");
		log.info(elecComp.getPO().getTagName() + "启动完成!");
	}

	/**
	 * 关闭元器件指示灯<br>
	 * 关闭信号电<br>
	 */
	protected void elecCompEnd() {
		log.info("shutdown " + elecComp.getPO().getTagName());
//		System.out.println(this.getClass().getCanonicalName() + ".elecCompEnd(start...)");
		for (LightIO lightIO : getElecComp().getDef().getLightIOs()) {
			lightIO.closeLight();
		}

		R r = null;
		if (cb_evn_names != null) {
			for (int i = 0; i < cb_evn_names.size(); i++) {
				r = R.getR(cb_evn_names.get(i));
				if (r != null) {
					r.shutPowerDown();
				}
			}
		}
		if (jf_evn_names != null) {
			for (int i = 0; i < jf_evn_names.size(); i++) {
				r = R.getR(jf_evn_names.get(i));
				if (r != null) {
					r.shutPowerDown();
				}
			}
		}
		if (cop_evn_names != null) {
			for (int i = 0; i < cop_evn_names.size(); i++) {
				r = R.getR(cop_evn_names.get(i));
				if (r != null) {
					r.shutPowerDown();
				}
			}
		}
		if (jfr_evn_names != null) {
			String env = null;
			for (int i = 0; i < jfr_evn_names.size(); i++) {
				for (int j = 0; j < jfrEnvEnds.length; j++) {
					env = jfr_evn_names.get(i) + jfrEnvEnds[j][0] + "_" + jfrEnvEnds[j][1];
					r = R.getR(env);
					if (r != null) {
						r.shutPowerDown();
					}
				}
			}
		}
		log.info("shutdown done " + elecComp.getPO().getTagName());
//		System.out.println(this.getClass().getCanonicalName() + ".elecCompEnd(end...)");
	}
}
