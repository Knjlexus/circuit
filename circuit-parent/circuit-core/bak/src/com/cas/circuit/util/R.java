/**
 * 
 */
package com.cas.circuit.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.cas.circuit.ILinker;
import com.cas.circuit.SignalVolt;
import com.cas.circuit.Voltage;
import com.cas.circuit.vo.CR;
import com.cas.circuit.vo.ElecCompDef;
import com.cas.circuit.vo.IP;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.ResisRelation;
import com.cas.circuit.vo.Terminal;
import com.cas.circuit.vo.Wire;
import com.cas.util.Util;
import com.jme3.math.FastMath;

/**
 * @author 张振宇 Aug 18, 2015 8:09:12 PM
 */
public class R {
	private static final Map<String, R> cache = new HashMap<String, R>();
	private static final String[] envx = new String[] { "u", "v", "w", "uv", "vw", "wu" };

	private Voltage voltage;

	private Terminal startTerminal;
	private Terminal endTerminal;

//	存放在本次测量中所有的电势位
	private List<IP> allIsopoList = new CopyOnWriteArrayList<IP>();
//	随着电阻的合并,(串联电阻之间的电势位会被去掉),记录合并电路后的所有电势位
	private List<IP> isopoList = new CopyOnWriteArrayList<IP>();
//	随着电阻的合并,(串联电阻之间的电势位会被去掉),记录要移除的电势位
	List<IP> toRemoveIsopoList = new CopyOnWriteArrayList<IP>();

	private IP startIP;
	private IP endIP;
	private Map<ResisRelation, CR> resisRelationWithCR = new HashMap<ResisRelation, CR>();

	private Set<IP> readyToCheckIPs = new HashSet<IP>();
	private Set<IP> passedIP = new HashSet<IP>();

	public R(String env, Terminal startTerminal, Terminal endTerminal) {
		this(startTerminal, endTerminal, new Voltage(env, Voltage.IS_NULL, 0, 0));
	}

	private R(Terminal startTerminal, Terminal endTerminal, Voltage voltage) {
		this.voltage = voltage;
		this.startTerminal = startTerminal;
		this.endTerminal = endTerminal;
	}

	/**
	 * @param voltage.getEnv() 表示电源,电源(能提供电压的元器件的TagName)
	 * @param voltType 电压类型: true表示直流,false表示交流
	 * @param startTerminal 电势较高的一个连接头.对于直流来说,start 与 end 不能写反了,对于交流无所谓顺序.
	 * @param endTerminal 电势较低的一个连接头. 对于直流来说,start 与 end 不能写反了,对于交流无所谓顺序.
	 * @param volt 电源的输出电压值
	 */
	public static R create(String env, int voltType, Terminal startTerminal, Terminal endTerminal, float volt) {
		synchronized (cache) {
			if (!cache.containsKey(env)) {
				cache.put(env, new R(startTerminal, endTerminal, new Voltage(env, voltType, volt, 0)));
//		} else {
//			System.err.println("have been exsited evn named " + env);
			}
			return getR(env);
		}
	}

	/**
	 * @param env 表示电源,电源(能提供电压的元器件的TagName)
	 * @param volt 电源的输出电压值,两两之间的电压
	 */
	public static List<R> create3Phase(String env, Terminal uTerminal, Terminal vTerminal, Terminal wTerminal, Terminal peTerminal, float volt) {
		synchronized (cache) {
//			System.out.println("R.create3Phase()" + env);
			Terminal[][] term = get3PhaseGroup(uTerminal, vTerminal, wTerminal, peTerminal);
			List<R> phase3RList = get3Phase(env);
			if (phase3RList != null) {
				for (int i = 0; i < phase3RList.size(); i++) {
					phase3RList.get(i).startTerminal = term[i][0];
					phase3RList.get(i).endTerminal = term[i][1];
				}
				return phase3RList;
			}

			String perEnv = null;
			int phase = 0;
			for (int i = 0; i < envx.length; i++) {
				perEnv = env + envx[i];
				if (!cache.containsKey(perEnv)) {
					if (i < 3) {
						cache.put(perEnv, new R(term[i][0], term[i][1], new Voltage(perEnv, Voltage.IS_AC, volt / FastMath.sqrt(3), 0)));
					} else {
						cache.put(perEnv, new R(term[i][0], term[i][1], new Voltage(perEnv, Voltage.IS_AC, volt, phase)));
						phase += 120;
					}
				}
			}

			return get3Phase(env);
		}
	}

	private static Terminal[][] get3PhaseGroup(Terminal uTerminal, Terminal vTerminal, Terminal wTerminal, Terminal peTerminal) {
		Terminal[][] term = new Terminal[][] { //
				{ uTerminal, peTerminal }, // U-PE
				{ vTerminal, peTerminal }, // V-PE
				{ wTerminal, peTerminal }, // W-PE
				{ uTerminal, vTerminal }, // U-V
				{ vTerminal, wTerminal }, // V-W
				{ wTerminal, uTerminal } }; // U-W
		return term;
	}

	/**
	 * @param env
	 * @param volt
	 */
	public static void set3PhaseVolt(String env, float volt) {
		String perEnv = null;
		R r = null;
		for (int i = 0; i < envx.length; i++) {
			perEnv = env + envx[i];
			r = getR(perEnv);
			if (i < 3) {
				r.voltage.setValue(volt);
			} else {
				r.voltage.setValue(volt * FastMath.sqrt(3));
			}
		}
	}

	/**
	 * @param env
	 * @param i
	 */
	public static void set3PhaseFrequency(String env, float frequency) {
		String perEnv = null;
		R r = null;
		for (int i = 0; i < envx.length; i++) {
			perEnv = env + envx[i];
			r = getR(perEnv);
			r.voltage.setFrequency(frequency);
		}
	}

	/**
	 * @param env
	 * @param equals
	 */
	public static void reversePhase(String env, boolean positive) {
		String perEnv = null;
		R r = null;
		for (int i = 3; i < envx.length; i++) {
			perEnv = env + envx[i];
			r = getR(perEnv);
			if (positive) {
				r.voltage.setPhase(Math.abs(r.voltage.getPhase()));
			} else {
				r.voltage.setPhase(Math.abs(r.voltage.getPhase()) * -1);
			}
		}
	}

	/**
	 * @param string
	 * @param isDc
	 * @param ipLower
	 * @param ipHigher
	 * @param cbSignalVolt
	 * @return
	 */
	public static R createSignal(String env, Terminal startTerminal, Terminal endTerminal, float volt) {
		synchronized (cache) {
			if (!cache.containsKey(env)) {
				cache.put(env, new R(startTerminal, endTerminal, new SignalVolt(env, Voltage.IS_DC, volt, 0)));
//		} else {
//			System.err.println("have been exsited evn named " + env);
			}
			return getR(env);
		}
	}

	/**
	 * @param env
	 * @return
	 */
	public static List<R> get3Phase(String env) {
		synchronized (env) {
			List<R> retR = new ArrayList<R>();
			R r = null;
			for (int i = 0; i < envx.length; i++) {
				r = getR(env + envx[i]);
				if (r == null) {
					return null;
				}
				retR.add(r);
			}
			return retR;
		}
	}

	/**
	 * 测量三相电相位
	 * @return 1:正, -1:负
	 */
	public static int mesurePhase(Terminal u, Terminal v, Terminal w) {
		int phaseUV = 0;
		int phaseVW = 0;
		int phaseWU = 0;

		Set<String> keys = u.getResidualVolt().keySet();
		for (String string : keys) {
			if (string.endsWith("uv")) {
				phaseUV = u.getResidualVolt(string).getPhase();
				break;
			}
		}
		keys = v.getResidualVolt().keySet();

		for (String string : keys) {
			if (string.endsWith("vw")) {
				phaseVW = v.getResidualVolt(string).getPhase();
				break;
			}
		}
		keys = w.getResidualVolt().keySet();

		for (String string : keys) {
			if (string.endsWith("wu")) {
				phaseWU = w.getResidualVolt(string).getPhase();
				break;
			}
		}

		if (phaseUV < 0 || phaseVW < 0 || phaseWU < 0) {
			phaseUV += 360;
			phaseVW += 360;
			phaseWU += 360;
		}

		if (phaseVW - phaseUV == 120 && phaseWU - phaseVW == 120) {
			return 1;
//		} else if (phaseUV - phaseVW == 120 && phaseVW - phaseWU == 120) {
//			return -1;
		} else {
			return -1;
		}
	}

	public static R getR(String env) {
		synchronized (cache) {
			if (cache.containsKey(env)) {
				return cache.get(env);
			}
//			System.err.println("Do not exsits evn named " + env);
//			throw new RuntimeException("do not exsits evn named " + voltage.getEnv());
			return null;
		}
	}

	public static Set<String> findEnvsOn(Terminal... terminal) {
		HashSet<String> envs = new HashSet<String>();
		for (int i = 0; i < terminal.length; i++) {
			envs.addAll(terminal[i].getResidualVolt().keySet());
		}
		return envs;
	}

	private CR mesure() {
		return mesure(startTerminal, endTerminal);
	}

	/**
	 * 测电阻调用
	 * @return
	 */
	public CR mesure(Terminal startTerm, Terminal endTerm) {
		synchronized (this) {
//		清理属性值,false表示暂不清理连接头上的电压
			cleanProperty();

//		传入的参数为null值,
			if (startTerm == null || endTerm == null) {
				return new CR(this, CR.MAX_RESIS_VALUE);
			}

//		找到指定电压环境下每一个电阻两端的电势
			R.findAllIsopotential(startTerm, this, false);
//		合并电阻
			mergeCircuit();

			for (IP ip : isopoList) {
				if (startIP == null && ip.hasTerminal(startTerm)) {
					startIP = ip;
				}
				if (endIP == null && ip.hasTerminal(endTerm)) {
					endIP = ip;
				}
//			优化遍历
				if (startIP != null && endIP != null) {
					break;
				}
			}

			if (endIP == null) {
				cleanProperty();
				findAllResisIsopotential(startTerm, false);
				findAllResisIsopotential(endTerm, false);
				// 再找一下保证startIP和endIP不null
				for (IP ip : isopoList) {
					if (startIP == null && ip.hasTerminal(startTerm)) {
						startIP = ip;
					}
					if (endIP == null && ip.hasTerminal(endTerm)) {
						endIP = ip;
					}
//				优化遍历
					if (startIP != null && endIP != null) {
						break;
					}
				}

				CR maxCR = new CR(this, CR.MAX_RESIS_VALUE);
				maxCR.setIospo1(startIP);
				maxCR.setIospo2(endIP);

				startIP.getCRList().add(maxCR);
				endIP.getCRList().add(maxCR);
			}
//			if (startIP == null || endIP == null) {
//				System.err.println(R.this + "startIP = " + startIP + ", endIP = " + endIP);
//			}

			if (startIP == endIP) {
				CR minCR = new CR(this, CR.MIN_RESIS_VALUE);
				minCR.setIospo1(startIP);
				minCR.setIospo2(endIP);
				startIP.getCRList().add(minCR);
				endIP.getCRList().add(minCR);
				return minCR;
			} else {
				boolean needRemive = false;
				do {
					mergeCircuit();
					needRemive = checkRemoveCR();
				} while (needRemive);
				return getCompositeResistance();
			}
		}
	}

	public void shareVoltage() {
		synchronized (this) {
//		记录当前电路中涉及的连接头lastCircuitTerminal
			List<Terminal> lastCircuitTerminal = new ArrayList<Terminal>();
			for (IP ip : allIsopoList) {
				addNonRepeatListAll(lastCircuitTerminal, ip.getTerminals());
//				lastCircuitTerminal.addAll(ip.getTerminals());
			}

			int count = 0;
//		真实的分压操作
			while (shareVoltageLocal()) {
				count++;
				if (count > 10) {
//				防止死循环
					System.out.println("死循环啦");
					break;
				}
			}
			if (startIP == null || endIP == null) {
				return;
			}

//		找出分压后当前电路中的连接头thisCircuitTerminal
			List<ElecCompDef> elecCompList = new ArrayList<ElecCompDef>();
			List<Terminal> thisCircuitTerminal = new ArrayList<Terminal>();
			for (IP ip : allIsopoList) {
				addNonRepeatListAll(thisCircuitTerminal, ip.getTerminals());
//				thisCircuitTerminal.addAll(ip.getTerminals());
//			找出所走到的连接头所属元器件,因为有些元器件如:开关电源,可算是一个电源的,当满足输入电压后,将输出电压
				findElecComp(elecCompList, ip.getTerminals(), true);
			}
//		过滤出电路没有走到的连接头,这些lastCircuitTerminal连接头即将清除电压
			lastCircuitTerminal.removeAll(thisCircuitTerminal);
//		找出要被清理电压的连接头所属的元器件,因为有些元器件如:开关电源,可算是一个电源的将无法输出电压
			findElecComp(elecCompList, lastCircuitTerminal, false);
//		System.out.println("shareVoltage这些连接头 本次电路中没有走到" + lastCircuitTerminal);
//		即将清除电压
			IP ip = null;
			for (Terminal terminal : lastCircuitTerminal) {
//			当前电源的两个输出端,及与输出端子处于同电位的,【不】清除电压
				if (!startIP.hasTerminal(terminal) && !endIP.hasTerminal(terminal)) {
					ip = terminal.getIsopotential(voltage.getEnv());
					if (ip != null) {
						ip.detory();
					}
					terminal.removeVolt(voltage.getEnv());
				}
			}
			for (ElecCompDef elecCompDef : elecCompList) {
				if (elecCompDef != null) {
					elecCompDef.doMagnetism();
				}
			}
		}
	}

	/**
	 * 电源调用,
	 * @return
	 */
	protected boolean shareVoltageLocal() {
//		找出上一次电路中参与的连接头
		List<Terminal> lastCircuitTerminal = new ArrayList<Terminal>();
		for (IP ip : allIsopoList) {
			addNonRepeatListAll(lastCircuitTerminal, ip.getTerminals());
//			lastCircuitTerminal.addAll(ip.getTerminals());
		}
//		计算电路电阻
		CR cr = mesure();

		if (startIP == null || endIP == null) {
			Logger.getLogger(this.getClass().getCanonicalName()).warn("startIP == null || endIP == null");
			return true;
		}

		if (cr.getValue() == CR.MIN_RESIS_VALUE) {
			startTerminal.getElecComp().powerShorted(startTerminal, endTerminal);
			return false;
		}

		cr.getIsopo2().setVoltageValue(0);
		cr.getIsopo1().setVoltageValue(voltage.getValue());
//		根据电阻分压
		cr.shareVoltage();

		if (startIP == null || endIP == null) {
//			System.out.println("电压已经清除了");
			return false;
		}
//		记录所走到元器件
		List<ElecCompDef> elecCompList = new ArrayList<ElecCompDef>();
//		找出本次电路中参与的连接头
		List<Terminal> thisCircuitTerminal = new ArrayList<Terminal>();
		for (IP ip : allIsopoList) {
//			thisCircuitTerminal.addAll(ip.getTerminals());
			addNonRepeatListAll(thisCircuitTerminal, ip.getTerminals());
			findElecComp(elecCompList, ip.getTerminals(), true);
		}

//		剩余的连接头表示是此次电路中没有涉及的,需要清理连接头上的电压
		lastCircuitTerminal.removeAll(thisCircuitTerminal);
//		elecCompSet.clear();
		IP ip = null;
//		FIXME false
		findElecComp(elecCompList, lastCircuitTerminal, false);
		for (Terminal terminal : lastCircuitTerminal) {
			if (!startIP.hasTerminal(terminal) && !endIP.hasTerminal(terminal)) {
				ip = terminal.getIsopotential(voltage.getEnv());
				if (ip != null) {
					ip.detory();
				}
				terminal.removeVolt(voltage.getEnv());
			}
		}

		for (ElecCompDef elecCompDef : elecCompList) {
			if (elecCompDef != null) {
				elecCompDef.doMagnetism();
			}
		}
		return lastCircuitTerminal.size() != 0;
	}

	public void shutPowerDown() {
		List<ElecCompDef> elecCompList = new ArrayList<ElecCompDef>();
		for (IP ip : allIsopoList) {
			findElecComp(elecCompList, ip.getTerminals(), true);
			ip.clearVolt();
		}

		cleanProperty();

		for (ElecCompDef elecCompDef : elecCompList) {
			if (elecCompDef != null) {
				elecCompDef.doMagnetism();
			}
		}

		if ("Tmp_REMOVE".equals(voltage.getEnv())) {
			return;
		}

		R removed = cache.remove(voltage.getEnv());
		boolean result = this == removed;
//		assert result;
//		String success = "成功." + Thread.currentThread().getName();
//		if (removed == null) {
//			success = "但是已经关闭了." + Thread.currentThread().getName();
//		} else if (!result) {
//			success = "失败!!" + Thread.currentThread().getName();
//		}
//		if(result){
//			Logger.getLogger(R.class.getCanonicalName()).log(Level.INFO, "移除电源{0}{1} 剩余电源{2}:{3}", new String[] { voltage.getEnv(), success, String.valueOf(cache.size()), cache.values().toString() });
//		}

		if (result) {
			Logger.getLogger(R.class.getCanonicalName()).info("移除电源" + voltage.getEnv() + "剩余电源" + String.valueOf(cache.size()) + ":" + cache.values().toString());
		}
	}

	public void cleanProperty() {
		for (IP ip : allIsopoList) {
			ip.detory();
		}

		allIsopoList.clear();
		isopoList.clear();

		resisRelationWithCR.clear();
		toRemoveIsopoList.clear();
		passedIP.clear();
		startIP = null;
		endIP = null;
	}

	private void findElecComp(List<ElecCompDef> elecCompList, List<Terminal> terminals, boolean considerEVN) {
		if (voltage instanceof SignalVolt) {
			return;
		}

		Terminal terminal = null;
		ElecCompDef comp = null;
		ElecCompDef toRemove = null;

		Iterator<Terminal> it = terminals.iterator();
		while (it.hasNext()) {
			terminal = it.next();
			comp = terminal.getElecComp();

			if (!considerEVN || terminal.getResidualVolt().containsKey(voltage.getEnv())) {
				if (comp == null) {
					if (terminal.getParent() != null && terminal.getParent() instanceof Jack) {
						if (terminal == startTerminal || terminal == endTerminal) {
							toRemove = (ElecCompDef) terminal.getParent().getParent(); // 针脚的parent是插孔Jack,插孔的parent是ElecCompDef
						}
					}
				}
				if (comp != null && !elecCompList.contains(comp)) {
					elecCompList.add(comp);
				}
			}
		}
		if (toRemove != null) {
			elecCompList.remove(toRemove);
		}
//		System.out.println(voltage.getEnv() + "-" +elecCompList);
	}

	/**
	 * @param voltType 电压类型{@link R.IS_AD, R.IS_DC}
	 * @param startTerminal 测量的两点
	 * @param endTerminal 测量的两点
	 * @param required 所需要的电压值 若是3相电,则为两两之间的电压值
	 * @param deviation 允许的误差范围
	 * @return
	 */
	public static MesureResult matchRequiredVolt(int voltType, Terminal startTerminal, Terminal endTerminal, float required, float deviation, String... conditions) {
		if (conditions.length > 0) {
			MesureResult result = mesureVoltage(startTerminal, endTerminal, conditions);
			if (result != null) {
				if (result.getType() == voltType) {
					if (Math.abs(result.getVolt() - required) <= deviation) {
						return result;
					}
				}
			}
		} else {
			MesureResult result = null;
			boolean found = false;
			String cond = MultiPower.NOTIN + "=";
			do {
				result = mesureVoltage(startTerminal, endTerminal, cond);
				if (result != null) {
					found = result.getType() == voltType && Math.abs(result.getVolt() - required) <= deviation;
					if (found) {
						return result;
					} else {
						cond += "," + result.getEvn();
					}
				}
			} while (result != null);
		}
		return null;
	}

	/**
	 * @param startTerminal
	 * @param endTerminal
	 * @param condition :{ IN=xxx,xxx NOTIN=xxx,xxx }
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static MesureResult mesureVoltage(Terminal startTerminal, Terminal endTerminal, String... condition) {
		MesureResult result = null;
//		System.out.println(str + "R.mesureVoltage()"+startTerminal +" --- "+ endTerminal);
		Set<String> env1 = startTerminal.getResidualVolt().keySet();
		Set<String> env2 = endTerminal.getResidualVolt().keySet();

		Set<String> all = new HashSet<String>();

//		获取交集
		Util.intersection(all, env1, env2);

		MultiPower.filters(all, condition);

		if (all.size() == 0) {
//			result.setType(IS_NULL);
//			result.setEvn(null);
//			result.setVolt(0f);
		} else if (all.size() == 1) {
			try {
				String envName = all.iterator().next();
				Voltage startTermVoltValue = startTerminal.getResidualVolt(envName);
				Voltage endTermVoltValue = endTerminal.getResidualVolt(envName);
//				获取该电源环境的电压类型,true表示直流,false表示交流
				R r = R.getR(envName);
				if (r != null) {
					result = new MesureResult();
					result.setType(r.voltage.getVoltType());
					result.setEvn(envName);
					result.setFrequency(r.voltage.getFrequency());
					float volt = startTermVoltValue.getValue() - endTermVoltValue.getValue();
					result.setVolt(r.voltage.getVoltType() == Voltage.IS_AC ? Math.abs(volt) : volt);
					result.setDatas(r.getVoltage().getData());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
//			if (condition.length == 0) {
//				result = MultiPower.mesure(startTerminal, endTerminal, all);
//			} else {
			result = MultiPower.mesure(startTerminal, endTerminal, all, condition);
//			}
		}
		return result;
	}

	/**
	 * @param startIsopo
	 * @return
	 */
	private void mergeCircuit() {
		int count = 0;
		int lastIPNum = isopoList.size();
		while (isopoList.size() > 2 && count < 10) {
			mergeSeries();
			mergeParallel();
			for (IP ip : toRemoveIsopoList) {
				isopoList.remove(ip);
			}
			toRemoveIsopoList.clear();
			// 如果有衰减
			if (lastIPNum > isopoList.size()) {
				lastIPNum = isopoList.size();
			} else {
				count++;
			}
		}
		if (isopoList.size() == 2) {
			IP ip1 = isopoList.get(0);
			IP ip2 = isopoList.get(1);
			if (ip1.getCRList().size() == 1 && ip2.getCRList().size() == 1 && ip1.getCRList().get(0) == ip2.getCRList().get(0)) {
//				break;
			} else {
				mergeParallel2();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean checkRemoveCR() {
		// 这里可能需要丢弃n坨
		boolean hasFlgTuo = false;// 用于记录这里的n坨都是可丢弃的
		// 检查中心拖出来的电阻
		if (readyToCheckIPs.size() > 0) {
			for (IP centerIP : readyToCheckIPs) {
				if (centerIP.getCRList().size() <= 1 || centerIP == startIP || centerIP == endIP) {
					continue;
				}
				List<CR> tuoCRs = new ArrayList<CR>();
				tuoCRs.addAll(centerIP.getCRList());
				tuoCRs.removeAll(startIP.getCRList());
				tuoCRs.removeAll(endIP.getCRList());
				for (CR tuoCR : tuoCRs) {
					IP anotherIP = tuoCR.getAnotherIP(centerIP);
					tuoCR.removeFromIP(centerIP);
					if (anotherIP != null) {
						addToRemoveIPList(anotherIP);
					}
				}
				if (!hasFlgTuo) {
					hasFlgTuo = true;
				}
			}
			readyToCheckIPs.clear();
		}

		if (startIP.getCRList().size() > 1 || endIP.getCRList().size() > 1) {
			// startip或endIP外侧需要丢弃n坨
			List<CR> commonCR = new ArrayList<CR>();
			Util.intersection(commonCR, startIP.getCRList(), endIP.getCRList());

			List<CR> startTempCRs = new ArrayList<CR>();
			startTempCRs.addAll(startIP.getCRList());
			startTempCRs.removeAll(commonCR);
			for (CR cr : startTempCRs) {
				cr.removeFromIP(startIP);
			}
			List<CR> endTempCRs = new ArrayList<CR>();
			endTempCRs.addAll(endIP.getCRList());
			endTempCRs.removeAll(commonCR);
			for (CR cr : endTempCRs) {
				cr.removeFromIP(endIP);
			}

			hasFlgTuo = true;

		} else if (startIP.getCRList().size() == 1 && endIP.getCRList().size() == 1) {
//			System.err.println(str + "电路电阻复合完毕");
		} else {
			System.err.println("不该出现的情况3");
		}

		return hasFlgTuo;
	}

	private CR getCompositeResistance() {
//		startIP与endIP仅有一个CR对象
		if (startIP.getCRList().size() == 1 && endIP.getCRList().size() == 1 && startIP.getCRList().get(0) == endIP.getCRList().get(0)) {
			return startIP.getCRList().get(0);
		} else {
			CR cr = new CR(this);
			return cr;
//			throw new RuntimeException("nonononononono");
		}
	}

	public static void findAllIsopotential(Terminal start, R r, boolean stopWhileTermElectricity) {
		if (start.hasIsopotential(r.voltage.getEnv())) {
			return;
		}

		if (stopWhileTermElectricity && start.getIsopotential().keySet().size() != 0) {
			return;
		}
//		if (start == endTerminal) {
//			System.out.println(str + "R.findAllIsopotential()");
////			return;
//		}

		IP isop = new IP(r.voltage);
		r.isopoList.add(isop);
		r.allIsopoList.add(isop);

//		1.收集与start连接头在同一电势位上的所有连接头
//		2.收集与start连接头在同一电势位上的所有是电阻一端的连接头
		collectIsopotentialTerminal(start, isop, r);

		List<ResisRelation> resisRelations = isop.getResisRelationList();
//		if (resisRelations.size() == 1) {
//			if (start != endTerminal && start != startTerminal) {
////				断路
//				removeBrokenCircuitResistance(isop);
//			}
//		}
		for (ResisRelation resisRelation : resisRelations) {
			findAllIsopotential(resisRelation.getTerm1(), r, false);
			findAllIsopotential(resisRelation.getTerm2(), r, false);
		}
	}

	/**
	 * DING找出从此端子开始联系到的所有端子，包括有电阻的(电压测量法)
	 * @param start
	 * @param r
	 * @param stopWhileTermElectricity
	 */
	private void findAllResisIsopotential(Terminal start, boolean stopWhileTermElectricity) {
		if (start.hasIsopotential(voltage.getEnv())) {
			return;
		}

		if (stopWhileTermElectricity && start.getIsopotential().keySet().size() != 0) {
			System.err.println("有电了, stop");
			return;
		}

		IP isop = new IP(voltage);
		isopoList.add(isop);
		allIsopoList.add(isop);

//		1.收集与start连接头在同一电势位上的所有连接头
//		2.收集与start连接头在同一电势位上的所有是电阻一端的连接头
		collectResisIsopotentialTerminal(start, isop);

		List<ResisRelation> resisRelations = isop.getResisRelationList();
		for (ResisRelation resisRelation : resisRelations) {
			findAllResisIsopotential(resisRelation.getTerm1(), false);
			findAllResisIsopotential(resisRelation.getTerm2(), false);
		}
	}

	/**
	 * 
	 */
	private void mergeSeries() {
		for (IP ip : isopoList) {
			if (toRemoveIsopoList.contains(ip)) {
				continue;
			}
			if ((ip.getTerminals().contains(startTerminal) && ip.getTerminals().contains(endTerminal)) && ip.getCRList().size() != 1) {
				// 短路
				isopoList.remove(ip);
				for (IP removeIp : isopoList) {
					removeIp.detory();
				}
				isopoList.clear();
				isopoList.add(ip);
				return;
			}
			if ((ip.getTerminals().contains(startTerminal) || ip.getTerminals().contains(endTerminal))) {
				continue;
			}
			if ((!ip.getTerminals().contains(startTerminal) && !ip.getTerminals().contains(endTerminal)) && ip.getCRList().size() > 2) {
				readyToCheckIPs.add(ip);
				continue;
			}

			passedIP.clear();

			List<CR> seriesResistanceList = findSeriesResistance(ip);
			// FIXME Modify
			if (seriesResistanceList.size() > 0) {
				CR one = new CR(this);
				one.setType(CR.SERIES);
				for (CR cr : seriesResistanceList) {
					one.attach(cr);
				}
				one.merge();
			}
		}
	}

	/**
	 * @param ip
	 * @return
	 */
	private List<CR> findSeriesResistance(IP ip) {
		List<CR> resisRelations = new ArrayList<CR>();
		if (ip == null) {
			return resisRelations;
		}
		if (ip.getTerminals().contains(startTerminal) || ip.getTerminals().contains(endTerminal)) {
			return resisRelations;
		}
		if (passedIP.contains(ip)) {
			return resisRelations;
		}
		if (ip.getCRList().size() == 2) {
			List<CR> twoCR = ip.getCRList();
			addResisRelations(resisRelations, twoCR);

			toRemoveIsopoList.add(ip);
			passedIP.add(ip);

			addResisRelations(resisRelations, findSeriesResistance(twoCR.get(0).getIsopo1()));
			addResisRelations(resisRelations, findSeriesResistance(twoCR.get(0).getIsopo2()));
			addResisRelations(resisRelations, findSeriesResistance(twoCR.get(1).getIsopo1()));
			addResisRelations(resisRelations, findSeriesResistance(twoCR.get(1).getIsopo2()));
		}

		return resisRelations;
	}

	private void addResisRelations(List<CR> list, List<CR> added) {
		list.removeAll(added);
		list.addAll(added);
	}

	/**
	 * 
	 */
	private void mergeParallel() {
		for (IP ip : isopoList) {
			if (ip.getCRList().size() < 3) {
				continue;
			}
			passedIP.clear();

			mergeParallel(ip);
		}
	}

	private void mergeParallel2() {
		assert isopoList.size() == 2;
		for (IP ip : isopoList) {
			passedIP.clear();

			mergeParallel(ip);
		}
	}

	/**
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void mergeParallel(IP ip) {
		if (passedIP.contains(ip)) {
			return;
		}
		List<CR> crList = ip.getCRList();
		if (crList.size() >= 2) {
			IP anotherIsopo = null;
			List<CR> runCRList = new ArrayList<CR>();
			runCRList.addAll(crList);
			for (CR cr : runCRList) {
				anotherIsopo = cr.getAnotherIP(ip);
				if (passedIP.contains(anotherIsopo)) {
					continue;
				}

				if (anotherIsopo != null) {
					List<CR> parallelCR = new ArrayList<CR>();
					Util.intersection(parallelCR, ip.getCRList(), anotherIsopo.getCRList());
					if (parallelCR.size() > 1) {
						CR one = new CR(this);
						one.setType(CR.PARALLEL);
						for (CR parallel : parallelCR) {
							one.attach(parallel);
						}
						one.merge();
					}
				}
			}
		}
	}

	public static void collectIsopotentialTerminal(Terminal ref, IP isopotential, R r) {
//		if (ref == null) {
//			return;
//		}
		if (!isopotential.hasTerminal(ref)) {
			isopotential.addTerminal(ref);
		}
//		遍历所有和ref关联的导线
		List<Terminal> wireAnotherTerms = new ArrayList<Terminal>();
		List<ILinker> wires = ref.getLinkers();
		Wire wire = null;

//		List<ILinker> toRemove = new ArrayList<ILinker>();
		for (ILinker linker : wires) {
			wire = (Wire) linker;
			if (!linker.isBothBinded()) {
//				toRemove.add(linker);
				continue;
			}

			if (isopotential.getPassedWires().contains(wire) || wire.isBrokenBreak()) {
				continue;
			}
			isopotential.getPassedWires().add(wire);
			Terminal anotherTerm = wire.getAnother(ref);
			if (anotherTerm != null) {
//				System.out.println(str + "R.collectIsopotentialTerminal ()" + wire);
				wireAnotherTerms.add(anotherTerm);
				collectIsopotentialTerminal(anotherTerm, isopotential, r);
			}
		}
//		FIXME 删除只接了一个连接头的导线？？？？
//		wires.removeAll(toRemove);

//		获取线缆中与ref关联的连接头
		Terminal contacted = ref.getContacted();

		if (contacted != null && !isopotential.getPassedContacted().contains(contacted)) {
			isopotential.getPassedContacted().add(contacted);
			collectIsopotentialTerminal(contacted, isopotential, r);
		}

//		遍历和ref有电阻关系的电阻
		Map<Terminal, ResisRelation> resisRelationMap = ref.getResisRelationMap();
		Iterator<Entry<Terminal, ResisRelation>> iter = resisRelationMap.entrySet().iterator();

		Terminal key_Terminal = null;
		ResisRelation resisRelation = null;
		while (iter.hasNext()) {
			Map.Entry<Terminal, ResisRelation> entry = iter.next();
			key_Terminal = entry.getKey();
			resisRelation = entry.getValue();

			if (!isopotential.getPassedResis().contains(key_Terminal)) {
				isopotential.getPassedResis().add(key_Terminal);
				if (resisRelation.getValue() == 0 || wireAnotherTerms.contains(key_Terminal)) {
//					阻值为零,则以ref对应的连接头递归处理   或  此电阻被短接了
					collectIsopotentialTerminal(key_Terminal, isopotential, r);
				} else {
					isopotential.getResisRelationList().add(resisRelation);
					if (r != null) {
//						遇到连接头是电阻的一端的,先创建出一个复合电阻对象
						if (r.resisRelationWithCR.get(resisRelation) != null) {
							CR cr = r.resisRelationWithCR.get(resisRelation);
							isopotential.getCRList().add(cr);
							cr.setIospo2(isopotential);
						} else {
							CR cr = new CR(r);
//							设置这个复合电阻的所指代的真实电阻
							cr.addResisRelation(resisRelation);
							isopotential.getCRList().add(cr);
							cr.setIospo1(isopotential);
							r.resisRelationWithCR.put(resisRelation, cr);
						}
					}
				}
			}
		}
	}

	/**
	 * DING找出从此端子开始联系到的所有端子，包括有电阻的(电压测量法)
	 * @param ref
	 * @param isopotential
	 * @param r
	 */
	private void collectResisIsopotentialTerminal(Terminal ref, IP isopotential) {
		if (!isopotential.hasTerminal(ref)) {
			isopotential.addTerminal(ref);
		}
//		遍历所有和ref关联的导线
		List<Terminal> wireAnotherTerms = new ArrayList<Terminal>();
		List<ILinker> wires = ref.getLinkers();
		Wire wire = null;
//		List<ILinker> toRemove = new ArrayList<ILinker>();
		for (ILinker linker : wires) {
			wire = (Wire) linker;
			if (!linker.isBothBinded()) {
//				toRemove.add(linker);
				continue;
			}
			if (isopotential.getPassedWires().contains(wire) || wire.isBrokenBreak()) {
				continue;
			}
			isopotential.getPassedWires().add(wire);
			Terminal anotherTerm = wire.getAnother(ref);
			wireAnotherTerms.add(anotherTerm);
			collectResisIsopotentialTerminal(anotherTerm, isopotential);
		}
//		FIXME 删除只接了一个连接头的导线？？？？
//		wires.removeAll(toRemove);

//		获取线缆中与ref关联的连接头
		Terminal contacted = ref.getContacted();

		if (contacted != null && !isopotential.getPassedContacted().contains(contacted)) {
			isopotential.getPassedContacted().add(contacted);
			collectResisIsopotentialTerminal(contacted, isopotential);
		}

//		遍历和ref有电阻关系的电阻
		Map<Terminal, ResisRelation> resisRelationMap = ref.getResisRelationMap();
//		System.out.println(str + resisRelationMap);
		Iterator<Entry<Terminal, ResisRelation>> iter = resisRelationMap.entrySet().iterator();

		Terminal key_Terminal = null;
//		ResisRelation resisRelation = null;
		while (iter.hasNext()) {
			Map.Entry<Terminal, ResisRelation> entry = iter.next();
			key_Terminal = entry.getKey();
//			resisRelation = entry.getValue();

			if (!isopotential.getPassedResis().contains(key_Terminal)) {
				isopotential.getPassedResis().add(key_Terminal);
//				阻值为零,则以ref对应的连接头递归处理   或  此电阻被短接了
				collectResisIsopotentialTerminal(key_Terminal, isopotential);
			}
		}
	}

	/**
	 * @return the toRemoveCRList
	 */
	public void addToRemoveIPList(IP toRemoveIP) {
		toRemoveIsopoList.add(toRemoveIP);
	}

	/**
	 * @return the startTerminal
	 */
	public Terminal getStartTerminal() {
		return startTerminal;
	}

	/**
	 * @return the endTerminal
	 */
	public Terminal getEndTerminal() {
		return endTerminal;
	}

	/**
	 * @return the allIsopoList
	 */
	public List<IP> getAllIsopoList() {
		return allIsopoList;
	}

	/**
	 * @return the voltage
	 */
	public Voltage getVoltage() {
		return voltage;
	}

	public static void clearCache() {
		cache.clear();
	}

	/**
	 * 脉冲
	 */
	public void pulse() {
		for (IP ip : allIsopoList) {
//			if(ip == startIP || ip == endIP){
//				continue;
//			}
			ip.getVoltage().pulse();
		}
//		voltage.pulse();
//		shareVoltage();
//		voltage.setPulse(0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return voltage.getEnv();
	}

	private static <T> void addNonRepeatListAll(List<T> list, List<T> toAdd) {
		for (T t : toAdd) {
			if (list.indexOf(t) == -1) {
				list.add(t);
			}
		}
	}

	public static float mesureResistence(Terminal term1, Terminal term2) {
		R r = R.create("TMP_REMOVE", Voltage.IS_DC, term1, term1, 10);
		CR resistence = r.mesure(term1, term2);

		r.shutPowerDown();
		return resistence.getValue();
	};

}
