package com.cas.circuit.vo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cas.circuit.po.SignalPO;
import com.cas.util.Util;
import com.cas.util.vo.BaseVO;

public class Signal extends BaseVO<SignalPO> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4892187159820488638L;

	public static final Map<String, Signal> AMAP = new ConcurrentHashMap<String, Signal>();
	public static final Map<String, Signal> GMAP = new ConcurrentHashMap<String, Signal>();
	public static final Map<String, Signal> YMAP = new ConcurrentHashMap<String, Signal>();
	public static final Map<String, Signal> XMAP = new ConcurrentHashMap<String, Signal>();
	public static final Map<String, Signal> FMAP = new ConcurrentHashMap<String, Signal>();

	public static final String VALUE_0 = "0";
	public static final String VALUE_1 = "1";

	public static final String STATE_UP = "up";
	public static final String STATE_DN = "down";
	public static final String STATE_BT = "both";

	public static final Signal getSignal(String head, int no, int index) {
		head = head.toUpperCase();
		String addr = head + no + "." + index;
		addr = formatSignal(addr);
		if ("G".equalsIgnoreCase(head)) {
			return GMAP.get(addr);
		} else if ("F".equalsIgnoreCase(head)) {
			return FMAP.get(addr);
		} else if ("X".equalsIgnoreCase(head)) {
			return XMAP.get(addr);
		} else if ("Y".equalsIgnoreCase(head)) {
			return YMAP.get(addr);
		} else if ("A".equalsIgnoreCase(head)) {
			return AMAP.get(addr);
		}
		return null;
	}

	/**
	 * 格式化信号地址
	 */
	public static String formatSignal(String addr) {
		return formatSignal(addr, 4);
	}

	/**
	 * 格式化信号地址
	 */
	public static String untiFormatSignal(String addr) {
		String bit = addr.substring(1);
		while (bit.startsWith("0")) {
			bit = bit.substring(1);
		}
		if ("".equals(bit)) {
			bit = "0";
		}
		return addr.charAt(0) + bit;
	}

	public static String formatSignal(String addr, int bit) {
		if (Util.isEmpty(addr)) {
			return null;
		}
		addr = addr.trim();
		addr = addr.replace(" ", "");
		while (addr.charAt(0) == 65279) {
			addr = addr.substring(1);
		}

//		X0.1
		String head = addr.substring(0, 1);
//		0.1
		addr = addr.substring(1);
		String main = null;
		String param = null;
		boolean hasPot;

		if (hasPot = addr.contains(".")) {
			String[] entry = addr.split("\\.");
//			0
			main = entry[0];
//			1
			param = entry[1];
			if (param.length() != 1) {
				throw new RuntimeException("错误的信号地址");
			}
		} else {
//			1
			main = addr;
		}
		int len = main.length();
		if (len < bit) {
			for (int i = 0; i < bit - len; i++) {
				main = "0" + main;
			}
		}
		addr = head + main;
		if (hasPot) {
			addr += "." + param;
		}
		return addr;
	}

//	默认为上升沿
	private String state;

	private String address;

	@Override
	protected void toValueObject() {
		super.toValueObject();
		String addr = po.getAddr();

//		FIXME 
		address = formatSignal(addr);

		if (address.startsWith("G")) {
			GMAP.put(address, this);
		} else if (address.startsWith("F")) {
			FMAP.put(address, this);
		} else if (address.startsWith("X")) {
			XMAP.put(address, this);
		} else if (address.startsWith("Y")) {
			YMAP.put(address, this);
		} else if (address.startsWith("A")) {
			AMAP.put(address, this);
		}

		String act = po.getActive();
		if (Util.isEmpty(act) || "both".equals(act)) {
			state = STATE_BT;
		} else if ("down".equals(act) || "up".equals(act)) {
			state = po.getActive();
		} else {
			System.err.println("Signal.toValueObject() 未定义的上升沿或下降沿状态 :[" + po.getActive() + "] up、down or both");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#getLocalKey()
	 */
	@Override
	protected Object getLocalKey() {
		return po.getAddr();
	}

	/**
	 * 如 X0000.0 或 X0000
	 */
	public String getAddress() {
		return address;
	}

	public String getState() {
		return state;
	}

}
