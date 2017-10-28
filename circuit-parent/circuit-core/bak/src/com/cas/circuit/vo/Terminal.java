package com.cas.circuit.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.CfgConst;
import com.cas.circuit.ElecCompCPU;
import com.cas.circuit.ILinkTarget;
import com.cas.circuit.ILinker;
import com.cas.circuit.TermTeam;
import com.cas.circuit.Voltage;
import com.cas.circuit.consts.IOType;
import com.cas.circuit.po.TerminalPO;
import com.cas.shader.FilterUtil;
import com.cas.util.StringUtil;
import com.cas.util.Util;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Spatial;

public class Terminal extends SwitchCtrl<TerminalPO> implements Savable, ILinkTarget {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6891680389471695911L;

//	public static ThreadLocal<CommandSignal> commandLocal = new ThreadLocal<CommandSignal>();

	private List<VoltageIO> voltIOs = new ArrayList<VoltageIO>();

	private Spatial model;

//	搭档
	private TermTeam team;

//	如果该端子是某个插孔上拓展出来的,则在对应插孔中哪一个针脚(编号)
	private int index;
//	如果该端子是在某个插孔中,则当插入电缆后,与之相连接的是哪个端子
	private Terminal contacted;
//	该端子上的连接的导线
	private List<ILinker> wires = new ArrayList<ILinker>();
//	与该连接头之间存在电阻的连接头
	private Map<Terminal, ResisRelation> resisRelationMap = new HashMap<Terminal, ResisRelation>();

//	电势位Key:电源环境，Value：在该电源环境下，该连接头的电势位
	private Map<String, IP> isopotential = new HashMap<String, IP>();
//	清电势后残余的电势值
	private Map<String, Voltage> residualVolt = new HashMap<String, Voltage>();

	private String axis;

	private boolean positive;

	// 可能是布局相关的名称
	private String name;

	private int num;

	public Terminal() {
		super();
	}

	public Terminal(TerminalPO po) {
		super(po);
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
//		positive = "1".equals(po.getIsPositive());
		if (!Util.isEmpty(po.getSwitchIn())) {
			resisStateIds = StringUtil.split(po.getSwitchIn());
		}
		if (!Util.isEmpty(po.getIndex())) {
			index = Integer.parseInt(po.getIndex());
		}
		if ("output".equalsIgnoreCase(po.getType())) {
			ioType = IOType.OUTPUT;
		} else if ("input".equalsIgnoreCase(po.getType())) {
			ioType = IOType.INPUT;
		} else {
			ioType = IOType.BOTH;
		}

		if (!Util.isEmpty(po.getDirection())) {
//			System.out.println(po.getDirection() + "--" + po.getId());
			axis = po.getDirection().substring(0, 1);
			positive = '+' == po.getDirection().charAt(1);
		}
		if (!Util.isEmpty(po.getName())) {
			name = po.getName();
		}
	}

	@Override
	protected void changeStateIndex(Integer index) {
		// 往复切换state
		if (switchIndex == 0) {
			switchIndex = 1;
		} else {
			switchIndex = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#getLocalKey()
	 */
	@Override
	protected String getLocalKey() {
		return po.getId();
	}

	@Override
	public String getDirection() {
		return po.getDirection();
	}

	@Override
	public IOType getIoType() {
		return ioType;
	}

	@Override
	public Spatial getModel() {
		return model;
	}

	@Override
	public TerminalPO getPO() {
		return super.getPO();
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#cleanUp()
	 */
	@Override
	protected void cleanUp() {
		voltIOs = new ArrayList<VoltageIO>();
		model = null;
		contacted = null;
		wires = new ArrayList<ILinker>();
		resisRelationMap = new HashMap<Terminal, ResisRelation>();
		isopotential = new HashMap<String, IP>();
		residualVolt = new HashMap<String, Voltage>();
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.circuit.ILinkTarget#getLinkers()
	 */
	@Override
	public List<ILinker> getLinkers() {
		return wires;
	}

	public int getIndex() {
		return index;
	}

	public Terminal getContacted() {
		return contacted;
	}

	public void setContacted(Terminal contacted) {
		this.contacted = contacted;
	}

	public boolean isPositive() {
		return positive;
	}

	public String getAxis() {
		return axis;
	}

	public void setModel(Spatial model) {
		if (model == null) {
			log.error("元器件" + parent + "没有找到连接头ID为" + po.getId() + "的模型");
		}
		this.model = model;

//		if (model != null) {
//			model.setUserData(JmeConst.AXIS_NAME, getAxis());
//			model.setUserData(JmeConst.AXIS_DIR, isPositive());
//			model.setUserData(UDKConsts.OBJECT, this);
//		}
	}

	public List<VoltageIO> getVoltIOs() {
		return voltIOs;
	}

	public List<VoltageIO> getInputVoltIOs() {
		List<VoltageIO> ret = new ArrayList<VoltageIO>();
		for (VoltageIO voltIO : voltIOs) {
			if (voltIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_INPUT)) {
				ret.add(voltIO);
			}
		}
		return ret;
	}

	public boolean hasInputVoltageIO() {
		for (VoltageIO voltIO : voltIOs) {
			if (voltIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_INPUT)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasOutputVoltageIO() {
		for (VoltageIO voltIO : voltIOs) {
			if (voltIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_OUTPUT)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the resisRelationMap
	 */
	public Map<Terminal, ResisRelation> getResisRelationMap() {
		return resisRelationMap;
	}

	/**
	 * @return the isopotential
	 */
	public Map<String, IP> getIsopotential() {
		return isopotential;
	}

	/**
	 * @return the isopotential
	 */
	public IP getIsopotential(String env) {
		return isopotential.get(env);
	}

	/**
	 * @param env
	 * @return
	 */
	public boolean hasIsopotential(String env) {
		return isopotential.containsKey(env) && isopotential.get(env) != null;
	}

	/**
	 * @param env
	 */
	public void addIsopotetial(String env, IP isopo) {
		if (!this.isopotential.containsKey(env)) {
			this.isopotential.put(env, isopo);
//			System.out.println("Terminal.removeIsopotetial()");
//		} else {
//			System.err.println("Terminal.removeIsopotetial() Failure: cause already had" + env);
		}
	}

	/**
	 * @param env
	 */
	public void removeIsopotetial(String env) {
//		System.out.println("Terminal.removeIsopotetial()");

		isopotential.remove(env);
//		if (clearVolt) {
//			removeVolt(env);
//		}
	}

	/**
	 * @param env
	 */
	public void removeVolt(String env) {
		if (residualVolt.containsKey(env)) {
			Voltage voltage = residualVolt.remove(env);
			if (voltage != null) {
				voltage.removeTerminal(this);
			}

//			if (elecComp != null) {
//				System.err.println("---清理 env={" + env + "}" + (elecComp.getRef().getPO().getTagName() + "上的 id=" + po.getId()) + ",后剩余电压: env=" + residualVolt + ">");
//			} else {
//				if (parent != null && parent instanceof Jack) {
//					System.out.println("---清理 env={" + env + "}" + (parent + "上的 id=" + po.getId()) + ",后剩余电压: env=" + residualVolt + ">");
//				}
//			}
			calculateVoltage();
		}
	}

	/**
	 * @param env
	 * @param voltage
	 */
	public void addVolt(Voltage voltage) {
		boolean changed = !residualVolt.containsKey(voltage.getEnv()) || voltage != residualVolt.get(voltage.getEnv());
		residualVolt.put(voltage.getEnv(), voltage);
		voltage.addTermianl(this);
		if (changed) {
			calculateVoltage();
		}
	}

	private void calculateVoltage() {
		boolean hasVolt = residualVolt.size() > 0;
//		for (Voltage volt : residualVolt.values()) {
//			hasVolt = hasVolt || volt.getValue() > 0;
//		}

		if (model != null) {
			if (hasVolt) {
//						JmeUtil.setMaterialEffect(app, HightLightType.WIREFRAME, model, ColorRGBA.Red, true);
//				JmeUtil.setSpatialHighLight(model, new ColorRGBA(1, 0, 0, 0.5f));
				FilterUtil.setSpatialElectrical(model, true);
//						System.err.println("showPowerOn-> " + Terminal.this);
			} else {
//						JmeUtil.setMaterialEffect(app, HightLightType.WIREFRAME, model, ColorRGBA.Red, false);
//				JmeUtil.setSpatialHighLight(model, ColorRGBA.BlackNoAlpha);
				FilterUtil.setSpatialElectrical(model, false);
//						System.out.println("showPowerOff-> " + Terminal.this);
			}
//		} else {
//			System.err.println(this + "...应该是针脚.... 否则就有问题了");
		}
		for (ILinker wire : wires) {
			((Wire) wire).voltageChanged(hasVolt);
		}
		if (parent != null) {
//			线缆中的连接头
			if (parent instanceof ElecCompDef) {
				elecComp.getRef().getCompLogic().onReceived(this);
			} else if (parent instanceof Jack) {
				ElecCompDef stitchElecComp = (ElecCompDef) parent.getParent();
				stitchElecComp.getRef().getCompLogic().onReceived(this);
			}
		}
	}

	/**
	 * 
	 */
	public void pulse() {
		if (parent != null) {
			BaseElectricCompLogic bec = null;
			if (parent instanceof ElecCompDef) {
				bec = elecComp.getRef().getCompLogic();
			} else if (parent instanceof Jack) {
				ElecCompDef stitchElecComp = (ElecCompDef) parent.getParent();
				bec = stitchElecComp.getRef().getCompLogic();
			}
			if (bec != null) {
				if (bec instanceof ElecCompCPU) {
					((ElecCompCPU) bec).onPulse(this);
				}
			}
		}
	}

	/**
	 * @return the residualVolt
	 */
	public Map<String, Voltage> getResidualVolt() {
		return residualVolt;
	}

	/**
	 * @return the isopotential
	 */
	public Voltage getResidualVolt(String env) {
		return residualVolt.get(env);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	/**
	 * @return the team
	 */
	public TermTeam getTeam() {
		if (team == null) {
			team = new TermTeam(po.getTeam(), this);
		}
		return team;
	}

	/**
	 * @param team the team to set
	 */
	public void setTeam(TermTeam team) {
		this.team = team;
	}

	/*
	 * (non-Javadoc)
	 * @see com.jme3.export.Savable#write(com.jme3.export.JmeExporter)
	 */
	@Override
	public void write(JmeExporter ex) throws IOException {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jme3.export.Savable#read(com.jme3.export.JmeImporter)
	 */
	@Override
	public void read(JmeImporter im) throws IOException {
	}

	@Override
	public String getElecCompKey() {
		return elecComp.getRef().getLocalKey();
	}

	@Override
	public String getTargetKey() {
		return po.getId();
	}

//	@Override
//	public String getTargetName() {
//		return po.getName();
//	}

	@Override
	public String toString() {
//		return po.toString();

		try {
			if (elecComp != null) {
				return elecComp.getRef().getPO().getTagName() + "上的端子：" + po.getName();// + residualVolt.values();
			} else if (parent instanceof Jack) {
				return ((ElecCompDef) parent.getParent()).getRef().getPO().getTagName() + "上的端子：" + po.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "未知";
	}
}
