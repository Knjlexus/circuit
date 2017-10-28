package com.cas.circuit.logic.plc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.cas.circuit.logic.Encoder;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.ResisRelation;
import com.cas.circuit.vo.Signal;
import com.cas.circuit.vo.Terminal;
import com.cas.robot.common.util.Pool;
import com.cas.util.MathUtil;
import com.cas.util.StringUtil;

import javafish.clients.opc.exception.VariantTypeException;
import javafish.clients.opc.variant.Variant;
import javafish.clients.opc.variant.VariantList;

public class FX3UPLCLogicAssist {
	private static final Logger log = FX3UPLCLogic.log;

	protected Map<Terminal, ResisRelation> resisRelations = new HashMap<Terminal, ResisRelation>();

	private FX3UPLCLogic logic;

	private Map<String, Boolean> hs = new HashMap<String, Boolean>();

	private boolean counting;

	Map<String, String> entry = new HashMap<String, String>();

	private Map<String, Integer> dest = new HashMap<String, Integer>();
	private Float pulsePerMillis;
	private Integer dir;

	public FX3UPLCLogicAssist(FX3UPLCLogic plcLogic) {
		this.logic = plcLogic;

		hs.put("X000", false);
		hs.put("X001", false);
//		hs.put("X002", false);
//		hs.put("X003", false);
//		hs.put("X004", false);
//		hs.put("X004", false);
//		hs.put("X004", false);

		dest.put("Y0", 0);
		dest.put("Y1", 0);

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("productionLine/data/Programe.ini"))));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}
				if (line.contains("=")) {
					String[] arr = line.split("=");
					if (arr.length == 2) {
						entry.put(Signal.formatSignal(arr[0].trim(), 4), arr[1].trim());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 处理高速输入信号 <br>
	 * dealWith High-Speed input signal
	 * @param signalX
	 * @param i
	 */
	public void dealWithHSXSignal(String signalX, final MesureResult result) {
		if (!hs.containsKey(signalX)) {
			return;
		}

		hs.put(signalX, result != null);

		if ("X000".equals(signalX) || "X001".equals(signalX)) {
			boolean tmp = hs.get("X000").booleanValue() && hs.get("X001").booleanValue();

			if (tmp && !counting) {
//				System.out.println("FX3UPLCLogicAssist.dealWithHSXSignal(counting = true)");
				counting = true;
//				FIXME 渣代码
				Pool.getCachedPool("计数器").execute(new Runnable() {

					private char[] currentPulseCount;

					public void run() {
						pulsePerMillis = MathUtil.parseFloat(result.getData(Encoder.KEY_PulsePerMillis), pulsePerMillis);
						dir = MathUtil.parseInt(result.getData(Encoder.KEY_PulseDir), dir);
						float higher = 0;
						float lower = 0;
						try {
							VariantList currentPulseCount = logic.getOPCClient().getItemValue(logic.getDeivceName() + ".C.CN251").getArray();
							lower = currentPulseCount.get(0).getWord() & 0xFFFF;
							higher = currentPulseCount.get(1).getWord() & 0xFFFF;
//							System.out.println("FX3UPLCLogicAssist.dealWithHSXSignal(...).new Runnable() {...}.run()" + currentPulseCount);
						} catch (VariantTypeException e) {
							log.error(e.getMessage(), e);
						}
						while (counting && logic.isWorkable()) {
							lower += pulsePerMillis * dir;
//							System.err.println(currentPulseCount);
							if (lower >= 65535) {
								lower = 0;
								higher += 1;
							} else if (lower <= 0) {
								lower = 65534;
								higher -= 1;

								if (higher < 0) {
									higher = 65534;
								}
							}

							VariantList variantList = new VariantList(Variant.VT_ARRAY);
							variantList.add(new Variant((short) lower & 0xFFFF));
							variantList.add(new Variant((short) higher & 0xFFFF));
							Variant variant = new Variant(variantList);
							logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".C.CN251", variant);
//							if (currentPulseCount >= 65535 || currentPulseCount <= 0) {
//								break;
////								int c252 = logic.getOPCClient().getItemValue(logic.getDeivceName() + ".C.CN252").getInteger();
////								logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".C.CN252", c252 + 1);
////								currentPulseCount = 0;
////							} else if (currentPulseCount < 0) {
////								int c252 = logic.getOPCClient().getItemValue(logic.getDeivceName() + ".C.CN252").getInteger();
////								logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".C.CN252", c252 - 1);
////								currentPulseCount = 65534;
//							}
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								log.error(e.getMessage(), e);
							}
						}
						counting = false;
					}
				});
			} else if (!tmp && counting) {
//				System.out.println("FX3UPLCLogicAssist.dealWithHSXSignal(counting = false)");
				counting = false;
			}
		}
	}

	public void dealWithYSignal(String singalAddress, Variant value) {
		dealWithYSignal(singalAddress, value, null);
	}

	private void dealWithYSignal(String singalAddress, Variant value, Map<String, String> data) {
//		Y信号：当某Y信号点有信号时将该端子与对应的COM端子导通
//		System.out.println("PLCLogic.update()");
//		获取信号所对应的连接头id
		String signal = singalAddress.substring(singalAddress.lastIndexOf('.') + 1);
		String termId = Signal.untiFormatSignal(signal);
		int index = Integer.parseInt(termId.substring(1));

//		找到信号所对应的COM端连接头ID
		int a = index / 10;
		int b = index % 10 >= 4 ? 1 : 0;
//			int group = 2 * a + b;		// COM0 开始
		int group = 2 * a + b + 1; // COM1 开始
		if (group > 5) {
//			FX3u-48M, 20 - 27 都与COM5连接
			group = 5;
		}
//		System.out.println("Y" + index + " group=" + group);
		Terminal term = logic.getElecComp().getDef().getTerminal(termId);
		Terminal docom = logic.getElecComp().getDef().getTerminal("COM" + group);
		if (term == null) {
			throw new RuntimeException("信号：" + singalAddress + "所对应的连接头" + termId + "没有找到");
		}
		if (docom == null) {
			throw new RuntimeException("连接头：" + termId + "所对应的COM" + group + "端没有找到");
		}

		ResisRelation resis = resisRelations.get(term);
		if (resis == null) {
			resisRelations.put(term, resis = new ResisRelation(term, docom, 0f, true));
		}

		if (value.getBoolean()) { // 没有信号
			term.getResisRelationMap().put(docom, resis);
			docom.getResisRelationMap().put(term, resis);
			resis.setActivated(true);
		} else {// 有信号
			term.getResisRelationMap().remove(docom);
			docom.getResisRelationMap().remove(term);
			resis.setActivated(false);
		}

		Set<String> envs = docom.getResidualVolt().keySet();
		R r = null;
		for (String env : envs) {
			r = R.getR(env);
			r.getVoltage().setData(data);
			r.shareVoltage();
		}
	}

	public void execute(String singalAddress, Variant value) {
		try {
			String addr = Signal.formatSignal(singalAddress, 4);
			getClass().getMethod(addr, Variant.class).invoke(this, value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @param variant
	 */
	public void M0500(Variant variant) {
		if (variant.getBoolean()) {

		}
	}

	/**
	 * HFA 方向
	 * @param variant
	 */
	public void M0510(Variant variant) {
		if (variant.getBoolean()) {
			Variant value = logic.getItemValue(logic.getDeivceName() + ".D.D110");
			int vt_int = value.getWord() & 0xFFFF;
			Map<String, String> data = new HashMap<String, String>();
			data.put("HFA", String.valueOf(vt_int));
			logic.s_rs485(data);
		} else {
			Map<String, String> data = new HashMap<String, String>();
			data.put("HFA", "0");
			logic.s_rs485(data);
		}
	}

	/**
	 * HEE 频率
	 * @param variant
	 */
	public void M0511(Variant variant) {
		if (variant.getBoolean()) {
			Variant value = logic.getItemValue(logic.getDeivceName() + ".D.D113");
			int vt_int = value.getWord() & 0xFFFF;
			Map<String, String> data = new HashMap<String, String>();
			data.put("HEE", String.valueOf(vt_int));
			logic.s_rs485(data);
		} else {
			Map<String, String> data = new HashMap<String, String>();
			data.put("HEE", "0");
			logic.s_rs485(data);
		}
	}

	// 回零
	public void M0560(final Variant variant) {
		if (variant.getBoolean()) {
//			回零
			String value = entry.get("M0560");
			final List<String> arr = StringUtil.split(value, ' ');
//			回零速度
			Integer sp = MathUtil.parseInt(arr.get(1).substring(1), 20000);
//			使用回零速度
			int[] d = getPulseSome(sp);
//			多久数一次
			int period = d[0];
//			一次数多少
			final int pulsePeriod = d[1];
			final Timer timer = new Timer();
			final String outputAddr = arr.get(4);
			Integer current = dest.get(outputAddr);
			final Integer pulseAmount = 0;
			// true表示正，false表示负，
			final boolean dir = current < pulseAmount;

			final Map<String, String> data = new HashMap<String, String>();
			data.put("pulseAmount", arr.get(1).substring(1));
			data.put("pulseFrequency", arr.get(2).substring(1));
			dealWithYSignal(outputAddr, new Variant(true), data);

			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						System.out.println("FX3UPLCLogicAssist.M0560(...).new TimerTask() {...}.run()" + variant.getBoolean());
						boolean crawl = logic.getInputSignal(arr.get(3));
						// 使用爬行速度
						if (crawl) {
							timer.cancel();
//							爬行速度
							Integer cp = MathUtil.parseInt(arr.get(2).substring(1), 1000);
//							使用回零速度
							int[] d = getPulseSome(cp);
//							多久数一次
							int period = d[0];
//							一次数多少
							final int pulsePeriod = d[1];
							final Timer timer2 = new Timer();
							timer2.schedule(new TimerTask() {
								@Override
								public void run() {
									String outputAddr = arr.get(4);
									Integer current = dest.get(outputAddr);
									Integer pulseAmount = 0;
									// true表示正，false表示负，
									boolean dir = current < pulseAmount;

									if (dir) {
										dest.put(outputAddr, dest.get(outputAddr) + pulsePeriod);
										if (dest.get(outputAddr) >= pulseAmount) {
											dest.put(outputAddr, pulseAmount);
//											数完脉冲后 断开对应Y端口
											dealWithYSignal(outputAddr, new Variant(false), null);
											timer2.cancel();
										}
									} else {
										dest.put(outputAddr, dest.get(outputAddr) - pulsePeriod);
										if (dest.get(outputAddr) <= pulseAmount) {
											dest.put(outputAddr, pulseAmount);
//											数完脉冲后 断开对应Y端口
											dealWithYSignal(outputAddr, new Variant(false), null);
											timer2.cancel();
										}
									}
									count(outputAddr);
								}
							}, 0, period);
						}

						if (dir) {
							dest.put(outputAddr, dest.get(outputAddr) + pulsePeriod);
						} else {
							dest.put(outputAddr, dest.get(outputAddr) - pulsePeriod);
						}
						count(outputAddr);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}, 0, period);
		} else {

		}
	}

	public void M0570(Variant variant) {
		ddrva("M0570", variant);
	}

	public void M0571(Variant variant) {
		ddrva("M0571", variant);
	}

	public void M0572(Variant variant) {
		ddrva("M0572", variant);
	}

	public void M0573(Variant variant) {
		ddrva("M0573", variant);
	}

	public void M8029(Variant variant) {
	}

	private void ddrva(String addr, Variant variant) {
		if (variant.getBoolean()) {
			String value = entry.get(addr);
			List<String> arr = StringUtil.split(value, ',');
			final String outputAddr = arr.get(3);

			final Map<String, String> data = new HashMap<String, String>();
			data.put("pulseAmount", arr.get(1).substring(1));
			data.put("pulseFrequency", arr.get(2).substring(1));
			dealWithYSignal(outputAddr, new Variant(true), data);

			String str_pulseAmount = data.get("pulseAmount");
			String str_pulseFrequency = data.get("pulseFrequency");
//			频率
			final Integer pulseFrequency = MathUtil.parseInt(str_pulseFrequency);
			final Integer pulseAmount = MathUtil.parseInt(str_pulseAmount);

			int[] d = getPulseSome(pulseFrequency);
			int period = d[0];
			final int pulsePeriod = d[1];
//			System.out.println("FX3UPLCLogicAssist.servo() 每" + period + "毫秒发送" + pulsePeriod + "个脉冲信号");

			Integer current = dest.get(outputAddr);
			// true表示正，false表示负，
			final boolean dir = current < pulseAmount;
//			System.out.println("FX3UPLCLogicAssist.ddrva(当前" + current + ") 目标" + pulseAmount + ";dir=" + dir);
			final String dirAddr = Signal.formatSignal(arr.get(4), 4);
//			Map<String, String> dirData = new HashMap<String, String>();
//			dirData.put("dir", dir);
			dealWithYSignal(dirAddr, new Variant(dir));
//			logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".Y." + dirAddr, new Variant(dir));

			final Timer timer = new Timer("DDRVA");
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (dir) {
						dest.put(outputAddr, dest.get(outputAddr) + pulsePeriod);
						if (dest.get(outputAddr) >= pulseAmount) {
							dest.put(outputAddr, pulseAmount);
							logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".M.M8029", new Variant(true));
//							数完脉冲后 断开对应Y端口
							dealWithYSignal(outputAddr, new Variant(false), null);
							timer.cancel();
						}
					} else {
						dest.put(outputAddr, dest.get(outputAddr) - pulsePeriod);
						if (dest.get(outputAddr) <= pulseAmount) {
							dest.put(outputAddr, pulseAmount);
							logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".M.M8029", new Variant(true));
//							数完脉冲后 断开对应Y端口
							dealWithYSignal(outputAddr, new Variant(false), null);
							timer.cancel();
						}
					}
//					System.out.println("FX3UPLCLogicAssist.servo(...).new TimerTask() {...}.run()" + count);
					count(outputAddr);
				}
			}, 0, period);
		} else {
			Variant m570 = logic.getItemValue(logic.getDeivceName() + ".M.M570");
			Variant m571 = logic.getItemValue(logic.getDeivceName() + ".M.M571");
			Variant m572 = logic.getItemValue(logic.getDeivceName() + ".M.M572");
			Variant m573 = logic.getItemValue(logic.getDeivceName() + ".M.M573");
			if (!(m570.getBoolean() || m571.getBoolean() || m572.getBoolean() || m573.getBoolean())) {
				logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".M.M8029", new Variant(false));
			}
		}
	}

	public int[] getPulseSome(int pulseFrequency) {
		int[] data = new int[2];
		int period = 1000;
		final int pulsePeriod;
		if (pulseFrequency > 1000) { // 2000, 2000 / 1000 = 2/1
			period = 1;
			pulsePeriod = pulseFrequency / 1000; // 500, 500/1000 = 5/10
		} else if (pulseFrequency > 100) {
			period = 10;
			pulsePeriod = pulseFrequency / 100;
		} else if (pulseFrequency > 10) { // 50, 50/1000 = 5/100
			period = 100;
			pulsePeriod = pulseFrequency / 10;
		} else {
			period = 1000;
			pulsePeriod = pulseFrequency;
		}
		data[0] = period;
		data[1] = pulsePeriod;

		return data;
	}

	private void count(final String outputAddr) {
		int count = dest.get(outputAddr);
		int higher = count / 65536;
		int lower = count % 65536;
		VariantList variantList = new VariantList(Variant.VT_ARRAY);
		variantList.add(new Variant((short) lower & 0xFFFF));
		variantList.add(new Variant((short) higher & 0xFFFF));
		Variant new_variant = new Variant(variantList);

		if ("Y0".equals(outputAddr)) {
			logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".D.D8340", new_variant);
		} else if ("Y1".equals(outputAddr)) {
			logic.getOPCClient().updateItemValue(logic.getDeivceName() + ".D.D8350", new_variant);
		}
	}
}