/**
 * 
 */
package com.cas.circuit.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.cas.circuit.SignalVolt;
import com.cas.circuit.Voltage;
import com.cas.circuit.vo.Terminal;

/**
 * @author 张振宇 2015年10月12日 上午10:34:11
 */
public class MultiPower {
	public static final String SIGNAL = "SIGNAL";
	public static final String IN = "IN";
	public static final String NOTIN = "NotIN";

	/**
	 * @param envs
	 * @param condition :{ IN=xxx,xxx NOTIN=xxx,xxx }
	 * @param startTerminal
	 * @param endTerminal
	 * @return
	 */
	public static MesureResult mesure(Terminal startTerminal, Terminal endTerminal, Set<String> envs, String[] condition) {
		filters(envs, condition);
		Set<String> toRemove = null;
		for (String envName : envs) {
			if (startTerminal.getResidualVolt(envName).equals(endTerminal.getResidualVolt(envName))) {
				if (toRemove == null) {
					toRemove = new HashSet<String>();
				}
				toRemove.add(envName);
			}
		}

		if (toRemove != null) {
//			System.out.println(envs + "removeAll " + toRemove);
			envs.removeAll(toRemove);
		}
		if (envs.size() == 3) {// 是三相电
			Iterator<String> it = envs.iterator();
			while (it.hasNext()) {
				String env = it.next();
				if (env.indexOf(startTerminal.getPO().getMark()) == -1 || env.indexOf(endTerminal.getPO().getMark()) == -1) {
					it.remove();
				}
			}
		}
		MesureResult result = null;
		if (envs.size() == 1) {
			String envName = envs.iterator().next();
			Voltage startTermVoltValue = startTerminal.getResidualVolt(envName);
			Voltage endTermVoltValue = endTerminal.getResidualVolt(envName);

			R r = R.getR(envName);
			result = new MesureResult();
			result.setType(r.getVoltage().getVoltType());
			result.setEvn(envName);
			float volt = startTermVoltValue.getValue() - endTermVoltValue.getValue();
			result.setVolt(r.getVoltage().getVoltType() == Voltage.IS_AC ? Math.abs(volt) : volt);
			result.setDatas(r.getVoltage().getData());
		} else {
			for (String env : envs) {
				Voltage startTermVoltValue = startTerminal.getResidualVolt(env);
				Voltage endTermVoltValue = endTerminal.getResidualVolt(env);

				R r = R.getR(env);
				float volt = startTermVoltValue.getValue() - endTermVoltValue.getValue();
				float v = r.getVoltage().getVoltType() == Voltage.IS_AC ? Math.abs(volt) : volt;
//				System.out.println(v);

				if (result == null) {
					result = new MesureResult();
					result.setType(r.getVoltage().getVoltType());
					result.setEvn(env);
					result.setVolt(v);
					result.setDatas(r.getVoltage().getData());
				} else if (Math.abs(result.getVolt() - v) > 10) {
					System.err.println("MultiPower.mesure() " + env + (result.getVolt() - v) + "..." + startTerminal + "-- " + endTerminal);
					return null;
				}
			}
		}
		return result;
	}

	/**
	 * @param cond
	 * @param envs
	 */
	private static void filter(Collection<String> envs, String cond) {
		String[] arr = cond.split("=");
		if (arr.length != 2) {
			return;
		}
		R r = null;
		Iterator<String> it = envs.iterator();
		if (SIGNAL.equalsIgnoreCase(arr[0])) {
			if ("true".equals(arr[1])) {
				while (it.hasNext()) {
					String env = it.next();
					r = R.getR(env);
					if (!(r.getVoltage() instanceof SignalVolt)) {
						it.remove();
					}
				}
			} else {
				while (it.hasNext()) {
					String env = it.next();
					r = R.getR(env);
					if (r != null && r.getVoltage() instanceof SignalVolt) {
						it.remove();
					}
				}
			}
		} else if (IN.equalsIgnoreCase(arr[0])) {
//			arr[1]=ddd,ddd,ddd
			String[] ins = arr[1].split(",");
			List<String> insList = Arrays.asList(ins);
			while (it.hasNext()) {
				String env = it.next();
				if (!insList.contains(env)) {
					it.remove();
				}
			}
		} else if (NOTIN.equalsIgnoreCase(arr[0])) {
//			arr[1]=ddd,ddd,ddd
			String[] ins = arr[1].split(",");
			List<String> insList = Arrays.asList(ins);
			while (it.hasNext()) {
				String env = it.next();
				if (insList.contains(env)) {
					it.remove();
				}
			}
		}
	}

	/**
	 * @param all
	 * @param condition
	 */
	public static void filters(Set<String> envs, String[] condition) {
		for (String env : condition) {
			filter(envs, env);
		}
	}

}
