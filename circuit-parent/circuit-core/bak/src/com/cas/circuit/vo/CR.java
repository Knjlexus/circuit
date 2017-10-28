/**
 * 
 */
package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.List;

import com.cas.circuit.util.R;

/**
 * @author 张振宇 Aug 18, 2015 7:59:55 PM
 */
public class CR {
	public static final float MAX_RESIS_VALUE = Float.MAX_VALUE;
	public static final float MIN_RESIS_VALUE = 1 / Float.MAX_VALUE;

	public static final String SERIES = "SERIES";
	public static final String PARALLEL = "PARALLEL";

	private Float value;

	private R belongR;
	private String type;
	private List<ResisRelation> resistances = new ArrayList<ResisRelation>();
	private IP isopo1;
	private List<CR> subCRList = new ArrayList<CR>();
	private IP isopo2;
//	private boolean sorted;

	public CR(R belongR) {
		this(belongR, null);
	}

	public CR(R belongR, Float value) {
		this.belongR = belongR;
		this.value = value;
	}

	/**
	 * @param resisRelation
	 */
	public void addResisRelation(ResisRelation resisRelation) {
//		this.resistance = resisRelation;
		resistances.add(resisRelation);
		copyFrom(resisRelation);
	}

	/**
	 * @param resisRelation
	 */
	private void copyFrom(ResisRelation resisRelation) {

	}

	/**
	 * @return the leftIospo
	 */
	public IP getIsopo1() {
		return isopo1;
	}

	/**
	 * @param leftIospo the leftIospo to set
	 */
	public void setIospo1(IP leftIospo) {
		if (isopo1 != null) {
			System.out.println("------------------!!!!!!!!!!!!!!");
		}
		this.isopo1 = leftIospo;
	}

	/**
	 * @return the rightIospo
	 */
	public IP getIsopo2() {
		return isopo2;
	}

	/**
	 * @param rightIospo the rightIospo to set
	 */
	public void setIospo2(IP rightIospo) {
		if (isopo2 != null) {
			System.out.println("-setIospo2! 但是isopo2 不为空值");
		}
		this.isopo2 = rightIospo;
	}

	/**
	 * @return
	 */
	public float getValue() {
		if (value != null) {
			return value;
		}
//		sort();
		if (type == null) {
			if (resistances.size() == 1) {
				return resistances.get(0).getValue();
			} else {
				return MAX_RESIS_VALUE;
			}
		} else if (type == PARALLEL) { // 并联
			float value = 0;
			for (CR compositeResistance : subCRList) {
				value += 1 / compositeResistance.getValue();
			}
			this.value = 1 / value;
		} else if (type == SERIES) { // 串联
			float value = 0;
			for (CR compositeResistance : subCRList) {
				value += compositeResistance.getValue();
			}
			this.value = value;
		}
		return value;
	}

	/**
	 * 计算电阻分压
	 */
	public void shareVoltage() {
//		sort();

		float voltage = isopo1.getVoltageValue() - isopo2.getVoltageValue();

		if (type == null) {
//			if (resistances.size() == 1) {
//			}
		} else if (type == PARALLEL) { // 并联
			for (CR cr : subCRList) {
				cr.shareVoltage();
			}
		} else if (type == SERIES) { // 串联
			float sumResisValue = getValue();
			float startVoltValue = isopo1.getVoltageValue();

			for (CR cr : subCRList) {
//				System.out.println(this.subCRList + ",串联 ==> " + cr);
				float currentCRVoltageValue = voltage * (cr.getValue() / sumResisValue);
//				cr.isopo1.setVoltage(startVoltValue);

				startVoltValue -= currentCRVoltageValue;
				cr.isopo2.setVoltageValue(startVoltValue);
				cr.shareVoltage();
			}
		}
	}

//	public float getCurrentFlow() {
//		return (isopo1.getVoltage() - isopo2.getVoltage()) / value;
//	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param mergeCircuit
	 */
	public void attach(CR cr) {
		if (cr.isopo1 == null || cr.isopo2 == null) {
			return;
		}
		if (!subCRList.contains(cr)) {
			if (subCRList.size() == 0) {
				subCRList.add(cr);
			} else {
				CR compare = null;
				int insertIndex = subCRList.size();
				for (int i = 0; i < subCRList.size(); i++) {
					compare = subCRList.get(i);
					if (compare.isopo1 == cr.isopo2) {
						insertIndex = i;
						break;
					} else if (compare.isopo2 == cr.isopo1) {
						insertIndex = i + 1;
						break;
					}
				}
				subCRList.add(insertIndex, cr);
//				System.err.println(subCRList);
			}

			resistances.addAll(cr.resistances);
//		} else {
//			System.err.println("cr exsits!!!!");
		}
	}

	/**
	 * 
	 */
	public void merge() {
		if (SERIES.equals(type)) {
//			FIXME 串联合并
			List<IP> singleIsopList = new ArrayList<IP>();
//			List<IP> singleIsop1List = new ArrayList<IP>();
//			List<IP> singleIsop2List = new ArrayList<IP>();

			List<IP> record = new ArrayList<IP>();// 用于记录看过的IP，用来制作doubleIP
			List<IP> doubleIP = new ArrayList<IP>();// 有两次的IP，说明是中间节点
			for (CR cr : subCRList) {
				if (record.contains(cr.isopo1)) {
					doubleIP.add(cr.isopo1);
				} else {
					record.add(cr.isopo1);
				}
				if (record.contains(cr.isopo2)) {
					doubleIP.add(cr.isopo2);
				} else {
					record.add(cr.isopo2);
				}

				singleIsopList.add(cr.isopo1);

				singleIsopList.add(cr.isopo2);
			}

			singleIsopList.removeAll(doubleIP);
//			singleIsop1List.removeAll(doubleIP);
//			singleIsop2List.removeAll(doubleIP);

//			assert singleIsop1List.size() == 1;
//			assert singleIsop2List.size() == 1;
			if (singleIsopList.size() == 2) {
				isopo1 = singleIsopList.get(0);
				isopo2 = singleIsopList.get(1);

				isopo1.getCRList().removeAll(subCRList);
				isopo2.getCRList().removeAll(subCRList);
				isopo1.getCRList().add(this);
				isopo2.getCRList().add(this);
			} else {
				// 串联走了个回路
//				System.err.println("串联走了个回路,黄色显示这些线和端子");

				CR firstCR = subCRList.get(0);
				CR lastCR = subCRList.get(subCRList.size() - 1);
				IP commomIP = null;
				if (firstCR.getIsopo1() == lastCR.getIsopo1()) {
					commomIP = firstCR.getIsopo1();
				} else if (firstCR.getIsopo1() == lastCR.getIsopo2()) {
					commomIP = firstCR.getIsopo1();
				} else if (firstCR.getIsopo2() == lastCR.getIsopo2()) {
					commomIP = firstCR.getIsopo2();
				} else if (firstCR.getIsopo2() == lastCR.getIsopo1()) {
					commomIP = firstCR.getIsopo2();
				}
				if (commomIP != null) {
					isopo1 = commomIP;
					isopo1.getCRList().removeAll(subCRList);
					isopo1.getCRList().add(this);
					isopo2 = null;
//					System.err.println("=========merge========本次电路没有走到的连接头,即将被踢掉 ----- 开始");
					for (CR subCR : subCRList) {
						subCR.removeFromIP(commomIP);
					}
//					System.err.println("=========merge========本次电路没有走到的连接头,即将被踢掉 ----- 结束");
//					removeFromIP(commomIP);

					// 黄色显示端子
//					for (Terminal terminal : commomIP.getTerminals()) {
//						JmeUtil.highLight(terminal.getModel(), "mat_wring", ColorRGBA.Yellow);
////						JmeUtil.hideModel(terminal.getModel());
//					}
				} else {
					System.err.println("不应该出现的：判断进串联走了个回路却找不到回路点");
				}
			}
		} else if (PARALLEL.equals(type)) {
//			并联合并
			// 随便取一个并联的两端
			isopo1 = subCRList.get(0).isopo1;
			isopo2 = subCRList.get(0).isopo2;

			isopo1.getCRList().removeAll(subCRList);
			isopo2.getCRList().removeAll(subCRList);
			isopo1.getCRList().add(this);
			isopo2.getCRList().add(this);
			if (isopo1.getCRList().size() == 1) {
				if (!isopo1.hasTerminal(belongR.getStartTerminal()) && !isopo1.hasTerminal(belongR.getEndTerminal())) {
					removeFromIP(isopo1);
					isopo1.detory();
					belongR.addToRemoveIPList(isopo1);
				}
			}
			if (isopo2.getCRList().size() == 1) {
				if (!isopo2.hasTerminal(belongR.getStartTerminal()) && !isopo2.hasTerminal(belongR.getEndTerminal())) {
					removeFromIP(isopo2);
					isopo2.detory();
					belongR.addToRemoveIPList(isopo2);
				}
			}
		}
	}

	/**
	 * 将此CR从IP上脱离
	 */
	public void removeFromIP(IP fromIP) {
		if (isopo1 == fromIP) {
			fromIP.getCRList().remove(this);
			fromIP.getResisRelationList().removeAll(resistances);
			if (isopo2 != null && !isopo2.hasTerminal(belongR.getStartTerminal()) && !isopo2.hasTerminal(belongR.getEndTerminal())) {
				// DING为了满足电压测量法，把电阻的另一头的端子设为等电势---开始
				for (Terminal terminal : isopo2.getTerminals()) {
					isopo1.addTerminal(terminal);
				}
				// DING为了满足电压测量法，把电阻的另一头的端子设为等电势---结束
				isopo2.detory();
				belongR.addToRemoveIPList(isopo2);
			}
		} else if (isopo2 == fromIP) {
			fromIP.getCRList().remove(this);
			fromIP.getResisRelationList().removeAll(resistances);
			if (isopo1 != null && !isopo1.hasTerminal(belongR.getStartTerminal()) && !isopo1.hasTerminal(belongR.getEndTerminal())) {
				// DING为了满足电压测量法，把电阻的另一头的端子设为等电势---开始
				for (Terminal terminal : isopo1.getTerminals()) {
					isopo2.addTerminal(terminal);
				}
				// DING为了满足电压测量法，把电阻的另一头的端子设为等电势---结束
				isopo1.detory();
				belongR.addToRemoveIPList(isopo1);
			}
		}
	}

	/**
	 * 
	 */
	public IP getAnotherIP(IP thisIP) {
		if (thisIP == isopo1) {
			return isopo2;
		} else if (thisIP == isopo2) {
			return isopo1;
		}
		return null;
	}

//	@Override
//	public String toString() {
////		return "isopo1" + isopo1 + " --> isopo2" + isopo2;
//		String str = "";
//		if (type == null) {
//			str = "无";
//		} else if (SERIES.equals(type)) {
//			str = "串联";
//		} else if (PARALLEL.equals(type)) {
//			str = "并联";
//		}
//		return "CR(" + hashCode() + "):阻值为" + getValue() + ",子级关系为:" + str + ",内部有" + subCRList.size() + "个子CR";
//	}

//	public void sort() {
//		if (sorted) {
//			return;
//		}
//		System.err.println("unsorted:" + subCRList);
//		Collections.sort(subCRList, new Comparator<CR>() {
//			@Override
//			public int compare(CR o1, CR o2) {
//				if (o1.getIsopo1() == o2.getIsopo2()) {
//					return -1;
//				}
//				if (o1.getIsopo2() == o2.getIsopo1()) {
//					return 1;
//				}
//				return 0;
//			}
//		});
//		sorted = true;
//
//		System.out.println("------ sorted:" + subCRList);
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return "CR(" + hashCode() + ") [" + isopo1 + "-- " + isopo2 + "]";
//	}

//	/**
//	 * @return the resistances
//	 */
//	public List<ResisRelation> getResistances() {
//		return resistances;
//	}

}
