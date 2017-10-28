package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.ElecCompCPU;
import com.cas.circuit.TermTeam;
import com.cas.circuit.po.ElecCompDefPO;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.gas.vo.BlockRelation;
import com.cas.gas.vo.BlockState;
import com.cas.gas.vo.GasPort;
import com.cas.util.Util;
import com.cas.util.parser.ClsMap;
import com.cas.util.vo.BaseVO;

/**
 * key : model【型号】
 */
public class ElecCompDef extends BaseVO<ElecCompDefPO> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3342674267365905254L;

	private ElecComp ref;

//	Key:电缆插孔的名字
//	Value:电缆插孔
	private Map<String, Jack> jackMap = new LinkedHashMap<String, Jack>();

//	Key: id
//	Value:Terminal
//	存放所有的连接头
	private Map<String, Terminal> terminalMap = new LinkedHashMap<String, Terminal>();

//	Key: id
//	Value:Terminal
//	存放所有连接头及插孔中的针脚
	private Map<String, Terminal> termAndStich = new LinkedHashMap<String, Terminal>();

//  Key: id
//	Value: GasPort
//	存放所有的气口
	private Map<String, GasPort> gasPortMap = new LinkedHashMap<String, GasPort>();

	private List<Magnetism> magnetisms = new ArrayList<Magnetism>();
	private List<ResisState> resisStates = new ArrayList<ResisState>();
	private List<BlockState> blockStates = new ArrayList<BlockState>();

	private Map<String, ResisState> resisStatesMap = new LinkedHashMap<String, ResisState>();
	private Map<String, BlockState> blockStatesMap = new LinkedHashMap<String, BlockState>();
//	key: terminal ID
	private Map<String, List<ResisRelation>> nowResisRelations = new LinkedHashMap<String, List<ResisRelation>>();
	private List<LightIO> lightIOs = new ArrayList<LightIO>();

	private Map<String, String> properties;

	private Set<String> createdEnv = null;

	private boolean isCable;

	public ElecCompDef() {
//		System.err.println("def:::" + this);
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
		if ("1".equals(po.getIsCable())) {
			isCable = true;
		}
	}

	@Override
	protected void addChild(BaseVO<?> child) {
		super.addChild(child);
		if (child instanceof Jack) {
			Jack jack = (Jack) child;
			jackMap.put(jack.getPO().getId(), jack);
		} else if (child instanceof Terminal) {
			Terminal terminal = (Terminal) child;
			terminal.setElecComp(this);
			terminalMap.put(terminal.getLocalKey(), terminal);
		} else if (child instanceof Magnetism) {
			magnetisms.add((Magnetism) child);
		} else if (child instanceof ResisState) {
			ResisState resisState = (ResisState) child;
			resisStates.add(resisState);
			resisStatesMap.put(resisState.getId(), resisState);
		} else if (child instanceof LightIO) {
			lightIOs.add((LightIO) child);
		} else if (child instanceof GasPort) {
			GasPort port = (GasPort) child;
			port.setElecComp(this);
			gasPortMap.put(port.getPO().getId(), port);
		} else if (child instanceof BlockState) {
			BlockState blockState = (BlockState) child;
			blockStates.add(blockState);
			blockStatesMap.put(blockState.getId(), blockState);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#getLocalKey()
	 */
	@Override
	protected String getLocalKey() {
		return po.getModel();
	}

	@Override
	protected void onAllChildAdded() {
		super.onAllChildAdded();
//		搜集所有的连接头和针脚
//		搜集连接头
		termAndStich.putAll(terminalMap);

//		搜集所有的针脚
		Map<String, Terminal> stitchMap = null;
		Iterator<Entry<String, Terminal>> it = null;
		Entry<String, Terminal> entry = null;
		for (BaseVO<?> baseVO : children) {
			if (baseVO instanceof Jack) {
				stitchMap = ((Jack) baseVO).getStitch();
				it = stitchMap.entrySet().iterator();
				while (it.hasNext()) {
					entry = it.next();
					termAndStich.put(entry.getValue().getLocalKey(), entry.getValue());
				}
			}
		}

		for (Magnetism mag : magnetisms) {
			for (VoltageIO voltIO : mag.getVoltageIOs()) {
				Terminal term1 = getTerminal(voltIO.getPO().getTerm1Id());
				Terminal term2 = getTerminal(voltIO.getPO().getTerm2Id());
				if (term1 == null) {
					throw new NullPointerException(po.getName() + "里的" + voltIO.getPO().getTerm1Id() + "端子没找到");
				} else {
					term1.getVoltIOs().add(voltIO);
					voltIO.setTerm1(term1);
				}
				if (term2 == null) {
					throw new NullPointerException(po.getName() + "里的" + voltIO.getPO().getTerm2Id() + "端子没找到");
				} else {
					term2.getVoltIOs().add(voltIO);
					voltIO.setTerm2(term2);
				}
			}
		}
		for (ResisState resisState : resisStates) {
			for (BaseVO<?> baseVO : resisState.getChildren()) {
				ResisRelation resis = (ResisRelation) baseVO;
				resis.setTerm1(getTerminal(resis.getPO().getTerm1Id()));
				resis.setTerm2(getTerminal(resis.getPO().getTerm2Id()));
				if (resisState.isDef()) {
					resisRelationAdded(resis);
				}
			}
		}
//		
		Map<String, List<Terminal>> teamTerminalMap = new LinkedHashMap<String, List<Terminal>>();
		String teamName = null;
		for (Terminal terminal : termAndStich.values()) {
			teamName = terminal.getPO().getTeam();
			if (Util.notEmpty(teamName)) {
				if (!teamTerminalMap.containsKey(teamName)) {
					teamTerminalMap.put(teamName, new ArrayList<Terminal>());
				}
				teamTerminalMap.get(teamName).add(terminal);
			}
		}

		for (Entry<String, List<Terminal>> entry2 : teamTerminalMap.entrySet()) {
			new TermTeam(teamName, entry2.getValue());
		}

		for (BlockState blockState : blockStates) {
			for (BaseVO<?> baseVO : blockState.getChildren()) {
				BlockRelation block = (BlockRelation) baseVO;
				block.setPort1(getGasPort(block.getPO().getPortId1()));
				block.setPort2(getGasPort(block.getPO().getPortId2()));
				if (blockState.isDef()) {
					blockRelationAdded(block);
				}
			}
		}
	}

	// 电生磁->磁生电或力3版
	public final void doMagnetism() {
		if (magnetisms.size() == 0) {
			return;
		}
//		System.out.println(ref.getPO().getTagName());
//		if ("VC1".equals(ref.getPO().getTagName())) {
//			System.out.println(this + "@" + hashCode() + ref.getPO().getTagName() + " createdEnv=" + createdEnv);
//		}
		String env_prefix = "";// ref.getCompState().getEquipmentState().getEquipment().getNumber() + ref.getPO().getTagName() + ref.hashCode();
		for (Magnetism magnetism : magnetisms) {
			float bili = 0;
			VoltageIO effectVoltageIO = null;
			for (VoltageIO voltageIO : magnetism.getInputVoltageIOs()) {
//				String termIds = voltageIO.getPO().getTerm1Id() + "-" + voltageIO.getPO().getTerm2Id();
				// 需求电压值和类型
				float requireVolt = voltageIO.getRequireVolt();
				int requireType = voltageIO.getVoltType();
				MesureResult realVolt = R.matchRequiredVolt(requireType, voltageIO.getTerm1(), voltageIO.getTerm2(), requireVolt, voltageIO.getDeviation(), ElecCompCPU.Power_Evn_Filter);
				// 电生磁成功-- 不是自己创建的点才符合接入条件
				if (realVolt != null && (createdEnv == null || !createdEnv.contains(realVolt.getEvn()))) {
					bili = realVolt.getVolt() / requireVolt;
					effectVoltageIO = voltageIO;
					if (!magnetism.isEffect() && voltageIO.getResisStateIds().size() == 2) {
						voltageIO.doSwitch(1);
					}
					break;
				}
			}
			R r = null;
			if (bili > 0 && !magnetism.isEffect()) {
				// 磁生电 -- 找出output的VoltageIO
				List<VoltageIO> outputVoltIOs = new ArrayList<VoltageIO>();
				outputVoltIOs.addAll(magnetism.getOutputVoltageIOs());
				outputVoltIOs.remove(effectVoltageIO);
				for (VoltageIO outputVoltIO : outputVoltIOs) {
					MesureResult checkVolt = R.matchRequiredVolt(outputVoltIO.getVoltType(), outputVoltIO.getTerm1(), outputVoltIO.getTerm2(), outputVoltIO.getRequireVolt() * bili, outputVoltIO.getDeviation(), ElecCompCPU.Power_Evn_Filter);
					if (checkVolt == null) {
						String env = env_prefix + outputVoltIO.getPO().getTerm1Id() + "-" + outputVoltIO.getPO().getTerm2Id();
//						System.out.println("ElecCompDef.doMagnetism()");
						addCreatedEnv(env);
						r = R.create(env, outputVoltIO.getVoltType(), outputVoltIO.getTerm1(), outputVoltIO.getTerm2(), outputVoltIO.getRequireVolt() * bili);
						r.shareVoltage();
//					}else{
//						if("VC1".equals(ref.getPO().getTagName())){
//							System.err.println(outputVoltIO.getVoltType() +":"+outputVoltIO.getTerm1().getResidualVolt() +","+ outputVoltIO.getTerm2().getResidualVolt() +" volt=" + outputVoltIO.getRequireVolt() * bili +"Deviation="+  outputVoltIO.getDeviation());
//						}
					}
				}
				// 磁生力
				for (final ControlIO outputControlIO : magnetism.getOutputControlIOs()) {
					if (ControlIO.INTERACT_PRESS.equals(outputControlIO.getPO().getInteract())) {
						if (!magnetism.isEffect() && outputControlIO.getSwitchIndex() == 0) {
							outputControlIO.switchStateChanged(null, null);
//							FIXME outputControlIO.playMotion(Dispatcher.getIns().getMainApp().getAssetManager(), null);
						}
					}
				}
				// 指示灯
				for (LightIO lightIO : magnetism.getLightIOs()) {
					lightIO.openLight();
				}
				magnetism.setEffect(true);
			} else if (bili == 0 && magnetism.isEffect()) {
				// 不符合接入条件
				// 无磁去力
//				System.err.println(ref.getPO().getTagName() + "上不满足输入条件");
				for (final ControlIO outputControlIO : magnetism.getOutputControlIOs()) {
					if (ControlIO.INTERACT_PRESS.equals(outputControlIO.getPO().getInteract())) {
						if (magnetism.isEffect() && outputControlIO.getSwitchIndex() == 1) {
//								outputControlIO.doSwitch(0);
							outputControlIO.switchStateChanged(null, null);
//							FIXME outputControlIO.playMotion(Dispatcher.getIns().getMainApp().getAssetManager(), null);
						}
					}
				}
				if (magnetism.isEffect()) {
					for (VoltageIO inputVoltIO : magnetism.getInputVoltageIOs()) {
						if (inputVoltIO.getResisStateIds().size() == 2) {
							inputVoltIO.doSwitch(0);
						}
					}
				}
				for (VoltageIO outputVoltIO : magnetism.getOutputVoltageIOs()) {
					String powerEnv = env_prefix + outputVoltIO.getPO().getTerm1Id() + "-" + outputVoltIO.getPO().getTerm2Id();
//					System.out.println(this + "@" + hashCode() + createdEnv);
					if (createdEnv != null && createdEnv.contains(powerEnv)) {
//						if ("VC1".equals(ref.getPO().getTagName())) {
//							System.out.println("清除电源 " + powerEnv + "上输出的电压" + powerEnv + System.nanoTime());
//						}
						r = R.getR(powerEnv);
						if (r != null) {
							r.shutPowerDown();
							removeCreatedEvn(powerEnv);
						}
					}
				}
				for (LightIO lightIO : magnetism.getLightIOs()) {
					lightIO.closeLight();
				}
				magnetism.setEffect(false);
			}
		}
	}

	/**
	 * 元器件内部电阻状态改变
	 * @param resisState 发生变化的电阻状态
	 */
	public void resisRelationAdded(ResisRelation relation) {
		relation.setActivated(true);
		Terminal term1 = relation.getTerm1();
		Terminal term2 = relation.getTerm2();
		try {
			term1.getResisRelationMap().put(term2, relation);
			term2.getResisRelationMap().put(term1, relation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void resisRelationRemoved(ResisRelation relation) {
		relation.setActivated(false);
		Terminal term1 = relation.getTerm1();
		Terminal term2 = relation.getTerm2();
		term1.getResisRelationMap().remove(term2);
		term2.getResisRelationMap().remove(term1);
	}

	public void brokenResisRelationAdded(ResisRelation relation) {
		if (!relation.isActivated()) {
			Terminal term1 = relation.getTerm1();
			Terminal term2 = relation.getTerm2();
			term1.getResisRelationMap().put(term2, relation);
			term2.getResisRelationMap().put(term1, relation);
		}
	}

	public void brokenResisRelationRemoved(ResisRelation relation) {
		if (!relation.isActivated()) {
			Terminal term1 = relation.getTerm1();
			Terminal term2 = relation.getTerm2();
			term1.getResisRelationMap().remove(term2);
			term2.getResisRelationMap().remove(term1);
		}
	}

	public void blockRelationAdded(BlockRelation relation) {
		relation.setActivated(true);
		GasPort port1 = relation.getPort1();
		GasPort port2 = relation.getPort2();
		port1.getBlockRelationMap().put(port2, relation);
		port2.getBlockRelationMap().put(port1, relation);
	}

	public void blockRelationRemoved(BlockRelation relation) {
		relation.setActivated(false);
		GasPort port1 = relation.getPort1();
		GasPort port2 = relation.getPort2();
		port1.getBlockRelationMap().remove(port2);
		port2.getBlockRelationMap().remove(port1);
	}

	public Map<String, Jack> getJackMap() {
		return jackMap;
	}

	public Map<String, Terminal> getTerminalMap() {
		return terminalMap;
	}

	public Terminal getTerminal(String key) {
		if (termAndStich.containsKey(key)) {
			return termAndStich.get(key);
		}
		return null;
	}

	public Map<String, ResisState> getResisStatesMap() {
		return resisStatesMap;
	}

	public Map<String, List<ResisRelation>> getNowResisRelations() {
		return nowResisRelations;
	}

	public Map<String, BlockState> getBlockStatesMap() {
		return blockStatesMap;
	}

	public List<Magnetism> getMagnetisms() {
		return magnetisms;
	}

	public List<LightIO> getLightIOs() {
		return lightIOs;
	}

	public Map<String, GasPort> getGasPortMap() {
		return gasPortMap;
	}

	public GasPort getGasPort(String key) {
		if (gasPortMap.containsKey(key)) {
			return gasPortMap.get(key);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#clone()
	 */
	@Override
	public ElecCompDef clone() {
		return (ElecCompDef) super.clone();
	}

	/*
	 * 只在clone方法中使用,请不要在其他任何地方调用此方法
	 * @see com.cas.cfg.vo.BaseVO#cleanUp()
	 */
	@Override
	protected void cleanUp() {
//		Key:电缆插孔的名字
//		Value:电缆插孔
		jackMap = new LinkedHashMap<String, Jack>();

//		Key: id
//		Value:Terminal
//		存放所有的连接头
		terminalMap = new LinkedHashMap<String, Terminal>();

//		Key: id
//		Value:Terminal
//		存放所有的连接头
		termAndStich = new LinkedHashMap<String, Terminal>();

		gasPortMap = new LinkedHashMap<String, GasPort>();
//		存放所有连接头及插孔中的针脚

		magnetisms = new ArrayList<Magnetism>();
		resisStates = new ArrayList<ResisState>();
		resisStatesMap = new LinkedHashMap<String, ResisState>();
		blockStates = new ArrayList<BlockState>();
		blockStatesMap = new LinkedHashMap<String, BlockState>();
//		key: terminal ID
		nowResisRelations = new LinkedHashMap<String, List<ResisRelation>>();
		lightIOs = new ArrayList<LightIO>();

		createdEnv = null;
	}

	public List<ResisState> getResisStates() {
		return resisStates;
	}

	/**
	 * @param elecComp
	 */
	public void setRef(ElecComp elecComp) {
		this.ref = elecComp;
	}

	/**
	 * @return the ref
	 */
	public ElecComp getRef() {
		return ref;
	}

	/**
	 * @return
	 */
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = getStringMap(po.getElementText());
		}
		return properties;
	}

	/**
	 * @return the createdEnv
	 */
	public Set<String> getCreatedEnv() {
		if (createdEnv == null) {
			createdEnv = new HashSet<String>();
		}
		return createdEnv;
	}

	public void addCreatedEnv(String env) {
		getCreatedEnv().add(env);
//		if ("VC1".equals(ref.getPO().getTagName())) {
//			System.err.println(this + "ElecCompDef.addCreatedEnv()" + createdEnv);
//		}
	}

	public void removeCreatedEvn(String env) {
		getCreatedEnv().remove(env);
//		if ("VC1".equals(ref.getPO().getTagName())) {
//			System.err.println(this + "ElecCompDef.removeCreatedEvn()" + createdEnv);
//		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (ref != null) {
			return ref.getPO().getTagName();
		}
		return getPO().getModel();
	}

	/**
	 * @param endTerminal
	 * @param startTerminal
	 */
	public void powerShorted(Terminal startTerminal, Terminal endTerminal) {
//		将对应的短路现象转发给对应的state处理
//		ref.getCompState().powerShorted(startTerminal, endTerminal);
	}

	/**
	 * @return
	 */
	public BaseElectricCompLogic buildCompLogic() {
		String appStateCls = po.getAppStateCls();
		if (Util.isEmpty(appStateCls)) {
			return new BaseElectricCompLogic();
		} else {
			return ClsMap.getInstance(appStateCls);
		}
	}

	public boolean isCable() {
		return isCable;
	}
}
