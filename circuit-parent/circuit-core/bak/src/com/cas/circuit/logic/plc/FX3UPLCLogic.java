package com.cas.circuit.logic.plc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.cas.circuit.Voltage;
import com.cas.circuit.logic.PLCLogic;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.ElecCompDef;
import com.cas.circuit.vo.Signal;
import com.cas.circuit.vo.Terminal;
import com.cas.opc.IOPCClient;
import com.cas.opc.IOPCSignalVisitor;
import com.cas.opc.OPCClientImpl;
import com.cas.robot.common.util.Pool;
import com.cas.util.Util;
import com.jme3.scene.Node;

import javafish.clients.opc.exception.ConnectivityException;
import javafish.clients.opc.variant.Variant;

public class FX3UPLCLogic extends PLCLogic implements IOPCSignalVisitor {
	private Map<String, Boolean> inputSignalMap = new HashMap<String, Boolean>();

	private IOPCClient client;
	private String deviceName;

//	Sink Source
	private Terminal term_SS;
	private Terminal term_24V;
	private Terminal term_0V;
	private boolean initialize;

	private Terminal sda;
	private Terminal sdb;
	private Terminal rda;
	private Terminal rdb;
	protected FX3UPLCLogicAssist assist;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

		assist = new FX3UPLCLogicAssist(this);
		ElecCompDef elecCompDef = elecComp.getDef();

		term_SS = elecCompDef.getTerminal("S/S");

		term_24V = elecCompDef.getTerminal("24V");
		if (term_24V == null) {
			term_24V = elecCompDef.getTerminal("24V_1");
		}
		if (term_24V == null) {
			term_24V = elecCompDef.getTerminal("24V_2");
		}
		term_0V = elecCompDef.getTerminal("0V");
		if (term_0V == null) {
			term_0V = elecCompDef.getTerminal("0V_1");
		}
		if (term_0V == null) {
			term_0V = elecCompDef.getTerminal("0V_2");
		}

//		Jack jack = elecCompDef.getJackMap().get("RS-485");
		sda = elecCompDef.getTerminal("SDA");
		sdb = elecCompDef.getTerminal("SDB");
		rda = elecCompDef.getTerminal("RDA");
		rdb = elecCompDef.getTerminal("RDB");

		SIGNAL_EVN = "PLC" + hashCode();

//		opc中设备名对应的并非工作站，而是针对具体某一个PLC元器件，所以opc相关信息写入
//		1、引用的PLC模型的userdata中，elecCompMdl.getUserdata("device")。注意：并不是elecCompMdl.getChild(0).getUserData("device")。
//		2、在应用PLC元器件配置文件的文本域中，elecComp.getProperty("deivce");
		final String deviceName = elecComp.getProperty("device");
		if (Util.isEmpty(deviceName)) {
			throw new RuntimeException("需要给" + elecComp + "声明opc中的device");
		}
		this.deviceName = deviceName;

		try {
			client = new OPCClientImpl(InetAddress.getLocalHost().getHostAddress());
			client.setDeviceName(deviceName);
			client.addOPCSignalVisitor(this);
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
		}
		initialize = true;
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
//		plc工作电压
		if (term_L == terminal || term_N == terminal || term_GND == terminal) {
			MesureResult volt = R.matchRequiredVolt(Voltage.IS_AC, term_L, term_N, 220, 2);
			boolean tmp = volt != null;

			R r = R.getR(SIGNAL_EVN);
			if (!workable && tmp) {// 开始工作
				workable = true;

//				输出24V电压
				if (r == null) {
					r = R.create(SIGNAL_EVN, Voltage.IS_DC, term_24V, term_0V, 24);
				}
				r.shareVoltage();

				if (client != null) {
					Pool.getOPCPool().submit(new Runnable() {
						@Override
						public void run() {
							try {
								client.connect();
							} catch (ConnectivityException e) {
								log.error("连接OPC server 失败!!" + e.getMessage(), e);
							}
						}
					});
				}
			} else if (workable && !tmp) {// 停止工作
				workable = false;
				if (r != null) {
					r.shutPowerDown();
				}

				if (client != null) {
					Pool.getOPCPool().submit(new Runnable() {
						@Override
						public void run() {
							client.disconnect();
							log.info("OPC 客户端断开连接");
						}
					});
				}
			}
			if (client != null) {
				client.setWorkable(workable);
			}
			return;
		} else if (terminal == rda || terminal == rdb) {

		}

		if (!workable || client == null) {
			return;
		}
//		能有信号的连接头 以X开头
		String prex = String.valueOf(terminal.getPO().getId().charAt(0)).toUpperCase();
		if ("X".equals(prex)) {
			MesureResult volt = R.matchRequiredVolt(Voltage.IS_DC, terminal, term_SS, 24, 2);
			if (volt == null) {
				volt = R.matchRequiredVolt(Voltage.IS_DC, term_SS, terminal, 24, 2);
			}
//			满足电压的输入，将对应的X信号写入PLC
//			FIXME 格式化信号格式
			String signal = Signal.formatSignal(terminal.getName(), 3);
			inputSignalMap.put(signal, volt != null);

			assist.dealWithHSXSignal(signal, volt);

			client.updateItemValue(deviceName + ".X." + signal, new Variant(volt != null));
		}
	}

	public String getDeivceName() {
		return deviceName;
	}

	public void setDeivceName(String deivceName) {
		this.deviceName = deivceName;
	}

	@Override
	public void update(String singalAddress, Variant value) {
		if (!workable) {
			return;
		}
		String sddr = singalAddress.substring(singalAddress.lastIndexOf(".") + 1);
		if (sddr.startsWith("Y")) {
			assist.dealWithYSignal(sddr, value);
		} else if (sddr.startsWith("M")) {
			assist.execute(sddr, value);
//		} else if (sddr.startsWith("C")) {
//		} else if (sddr.startsWith("D")) {
//			assist.execute(sddr, value);
		}
	}

	@Override
	public boolean isInitialize() {
		return initialize;
	}

	@Override
	public void setOPCClient(IOPCClient client) {
		this.client = client;
	}

	@Override
	public void onOPClientConnected() {
	}

	@Override
	public void destroy() {
		super.destroy();
		Pool.getOPCPool().submit(new Runnable() {
			@Override
			public void run() {
				if (client != null) {
					client.disconnect();
					log.info("OPC 客户端断开连接");
				}
			}
		});
	}

	public IOPCClient getOPCClient() {
		return client;
	}

	@Override
	public Variant getItemValue(String itemName) {
		try {
			return client.getItemValue(itemName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return super.getItemValue(itemName);
	}

	public void s_rs485(Map<String, String> data) {
		R r = R.getR("RS-485_PLC");
		if (r == null) {
			r = R.create("RS-485_PLC", Voltage.IS_DC, sda, sdb, 5);
		}
		r.getVoltage().setData(data);
		r.shareVoltage();
	}

	public boolean getInputSignal(String addr) {
		addr = Signal.formatSignal(addr);
		if (inputSignalMap.get(addr) == null) {
			return false;
		}
		return inputSignalMap.get(addr).booleanValue();
	}

}
