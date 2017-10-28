package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cas.circuit.consts.IOType;
import com.cas.circuit.util.R;
import com.cas.gas.util.G;
import com.cas.gas.vo.BlockRelation;
import com.cas.gas.vo.BlockState;
import com.cas.util.Util;
import com.cas.util.vo.BaseVO;

public class SwitchCtrl<T>extends BaseVO<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7175236186204511820L;
	protected ElecCompDef elecComp;
	protected List<String> resisStateIds = new ArrayList<String>();
	protected int switchIndex = 0;

	protected IOType ioType;
	// 禁止切换电路、气路
	private boolean disable;

	public SwitchCtrl() {
	}

	public SwitchCtrl(T po) {
		super(po);
	}

	public void switchStart(Integer index) {
		if (disable) {
			return;
		}
		if (switchIndex == index) {
			return;
		}
		Set<String> envs = new HashSet<String>();

		if (resisStateIds.size() == 0) {
			return;
		}
//		Terminal terminal = null;

		Set<String> tmpEnvs = null;
		// 关灯
		ResisState oldState = elecComp.getResisStatesMap().get(resisStateIds.get(switchIndex));
//		if (oldState.getLightIO() != null) {
//			oldState.getLightIO().closeLight();
//		}
		if (oldState != null) {
			// 把原来的state控制的relation删除
			List<BaseVO<?>> oldRelationList = oldState.getChildren();
			for (BaseVO<?> baseVO : oldRelationList) {
				if (baseVO instanceof ResisRelation) {
					elecComp.resisRelationRemoved((ResisRelation) baseVO);
//				找出两个连接头上所有的电源环境,用来通知对应的电源电路发生了变化,重新计算电路
					tmpEnvs = R.findEnvsOn(((ResisRelation) baseVO).getTerm1(), ((ResisRelation) baseVO).getTerm2());
//				terminal = ((ResisRelation) baseVO).getTerm1();
//				tmpEnvs.addAll(terminal.getResidualVolt().keySet());
//				terminal = ((ResisRelation) baseVO).getTerm2();
//				tmpEnvs.addAll(terminal.getResidualVolt().keySet());
					envs.addAll(tmpEnvs);
				}
			}
		}

		BlockState oldBlock = elecComp.getBlockStatesMap().get(resisStateIds.get(switchIndex));
		if (oldBlock != null) {
			System.out.println(this);
			List<BaseVO<?>> oldBlockList = oldBlock.getChildren();
			for (BaseVO<?> baseVO : oldBlockList) {
				if (baseVO instanceof BlockRelation) {
					BlockRelation relation = (BlockRelation) baseVO;
					elecComp.blockRelationRemoved(relation);
//				TODO 调用气路逻辑， 重新分配气压
					G.g().refreshGasPressure();
				}
			}
		}

		if (index == null) {
			changeStateIndex(index);
		} else {
			switchIndex = index;
		}
		try {
			for (String env : envs) {
				if (Util.isEmpty(env)) {
					continue;
				}
				R r = R.getR(env);
				if (r != null) {
					r.shareVoltage();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void switchEnd() {
		if (disable) {
			return;
		}
		if (resisStateIds.size() == 0) {
			return;
		}
		Terminal terminal = null;

		Set<String> tmpEnvs = null;
		// 开灯
		ResisState newState = elecComp.getResisStatesMap().get(resisStateIds.get(switchIndex));
		Set<String> envs = new HashSet<String>();
//		if (newState.getLightIO() != null) {
//			newState.getLightIO().openLight();
//		}
		if (newState != null) {
			// 把当前的state控制的relation增加
			List<BaseVO<?>> newRelationList = newState.getChildren();
			R tmpR = null;
			List<Terminal> terminalsInIP = null;

			for (BaseVO<?> baseVO : newRelationList) {
				if (baseVO instanceof ResisRelation) {
					elecComp.resisRelationAdded((ResisRelation) baseVO);
//				找出两个连接头上所有的电源环境,用来通知对应的电源电路发生了变化,重新计算电路
					tmpEnvs = new HashSet<String>();
					terminal = ((ResisRelation) baseVO).getTerm1();
					tmpEnvs.addAll(terminal.getResidualVolt().keySet());
					terminal = ((ResisRelation) baseVO).getTerm2();
					tmpEnvs.addAll(terminal.getResidualVolt().keySet());

//				如果这两个连接头上都没有找到电源环境,则有可能是两端都有电阻,所以电没有到达这两个连接头,但是闭合这两个连接头后会通路。
//				if (tmpEnvs.size() == 0) {
					tmpR = new R("Tmp_REMOVE", ((ResisRelation) baseVO).getTerm1(), ((ResisRelation) baseVO).getTerm2());
					R.findAllIsopotential(tmpR.getStartTerminal(), tmpR, true);
					R.findAllIsopotential(tmpR.getEndTerminal(), tmpR, true);
					List<IP> allIP = tmpR.getAllIsopoList();
					for (IP ip : allIP) {
						terminalsInIP = ip.getTerminals();
						for (Terminal terminal2 : terminalsInIP) {
							tmpEnvs.addAll(terminal2.getIsopotential().keySet());
						}
					}
					tmpEnvs.remove("Tmp_REMOVE");
					tmpR.shutPowerDown();
//				}
					envs.addAll(tmpEnvs);
				}
			}
		}

		BlockState newBlock = elecComp.getBlockStatesMap().get(resisStateIds.get(switchIndex));
		if (newBlock != null) {
			List<BaseVO<?>> newBlockList = newBlock.getChildren();
			for (BaseVO<?> baseVO : newBlockList) {
				if (baseVO instanceof BlockRelation) {
					BlockRelation relation = (BlockRelation) baseVO;
					elecComp.blockRelationAdded(relation);
//				TODO 调用气路逻辑， 重新分配气压
					G.g().refreshGasPressure();
				}
			}
		}

		try {
			for (String env : envs) {
				if (Util.isEmpty(env)) {
					continue;
				}
				R r = R.getR(env);
				if (r != null) {
					r.shareVoltage();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 按钮将其控制的state切换
	 * @param index index不为null则强制切换到某档
	 */
	public void doSwitch(Integer index) {
		if (disable) {
			return;
		}
		if (switchIndex == index) {
//			System.err.println("但是 已经处于这种状态了,不跳变");
			return;
		}
//		System.err.println(this + "跳变 " + switchIndex + " -> " + index);

		Set<String> envs = new HashSet<String>();

		Terminal terminal = null;
		Set<String> tmpEnvs = null;
		if (resisStateIds.size() > 0) {
//			System.err.println(elecComp.getResisStatesMap() + " :: " + resisStateIds +" , " + resisStateIds.get(switchIndex));
			ResisState oldState = elecComp.getResisStatesMap().get(resisStateIds.get(switchIndex));
			if (oldState != null) {
				// 把原来的state控制的relation删除
				List<BaseVO<?>> oldRelationList = oldState.getChildren();
				for (BaseVO<?> baseVO : oldRelationList) {
					if (baseVO instanceof ResisRelation) {
						elecComp.resisRelationRemoved((ResisRelation) baseVO);
//					找出两个连接头上所有的电源环境,用来通知对应的电源电路发生了变化,重新计算电路
						tmpEnvs = new HashSet<String>();
						terminal = ((ResisRelation) baseVO).getTerm1();
						tmpEnvs.addAll(terminal.getResidualVolt().keySet());
						terminal = ((ResisRelation) baseVO).getTerm2();
						tmpEnvs.addAll(terminal.getResidualVolt().keySet());

						envs.addAll(tmpEnvs);
					}
				}
			}

			BlockState oldBlock = elecComp.getBlockStatesMap().get(resisStateIds.get(switchIndex));
			if (oldBlock != null) {
				List<BaseVO<?>> oldBlockList = oldBlock.getChildren();
				for (BaseVO<?> baseVO : oldBlockList) {
					if (baseVO instanceof BlockRelation) {
						BlockRelation relation = (BlockRelation) baseVO;
						elecComp.blockRelationRemoved(relation);

//					TODO 调用气路逻辑， 重新分配气压
						G.g().refreshGasPressure();
					}
				}
			}
		}
		if (index == null) {
			changeStateIndex(index);
		} else {
			switchIndex = index;
		}
		if (resisStateIds.size() > 0) {
			// 开灯
			ResisState newState = elecComp.getResisStatesMap().get(resisStateIds.get(switchIndex));
			if (newState != null) {
				// 把当前的state控制的relation增加
				List<BaseVO<?>> newRelationList = newState.getChildren();
				R tmpR = null;
				List<Terminal> terminalsInIP = null;

				for (BaseVO<?> baseVO : newRelationList) {
					if (baseVO instanceof ResisRelation) {
						elecComp.resisRelationAdded((ResisRelation) baseVO);
//						找出两个连接头上所有的电源环境,用来通知对应的电源电路发生了变化,重新计算电路
						tmpEnvs = new HashSet<String>();
						terminal = ((ResisRelation) baseVO).getTerm1();
						tmpEnvs.addAll(terminal.getResidualVolt().keySet());
						terminal = ((ResisRelation) baseVO).getTerm2();
						tmpEnvs.addAll(terminal.getResidualVolt().keySet());

//						如果这两个连接头上都没有找到电源环境,则有可能是两端都有电阻,所以电没有到达这两个连接头,但是闭合这两个连接头后会通路。
//						if (tmpEnvs.size() == 0) {
						tmpR = new R("Tmp_REMOVE", ((ResisRelation) baseVO).getTerm1(), ((ResisRelation) baseVO).getTerm2());
						R.findAllIsopotential(tmpR.getStartTerminal(), tmpR, true);
						R.findAllIsopotential(tmpR.getEndTerminal(), tmpR, true);
						List<IP> allIP = tmpR.getAllIsopoList();
						for (IP ip : allIP) {
							terminalsInIP = ip.getTerminals();
							for (Terminal terminal2 : terminalsInIP) {
								tmpEnvs.addAll(terminal2.getIsopotential().keySet());
							}
						}
						tmpEnvs.remove("Tmp_REMOVE");
						tmpR.shutPowerDown();
//						}
						envs.addAll(tmpEnvs);
					}
				}
			}

			BlockState newBlock = elecComp.getBlockStatesMap().get(resisStateIds.get(switchIndex));
			if (newBlock != null) {
				List<BaseVO<?>> newBlockList = newBlock.getChildren();
				for (BaseVO<?> baseVO : newBlockList) {
					if (baseVO instanceof BlockRelation) {
						BlockRelation relation = (BlockRelation) baseVO;
						elecComp.blockRelationAdded(relation);
//				TODO 调用气路逻辑， 重新分配气压
						G.g().refreshGasPressure();
					}
				}
			}
		}
//		System.out.println("SwitchCtrl.doSwitch() -----------shareVoltage-----------");
		try {
			for (String env : envs) {
				if (Util.isEmpty(env)) {
					continue;
				}
				R r = R.getR(env);
				if (r != null) {
					r.shareVoltage();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getResisStateIds() {
		return resisStateIds;
	}

	public void setElecComp(ElecCompDef elecComp) {
		this.elecComp = elecComp;
	}

	public ElecCompDef getElecComp() {
		return elecComp;
	}

	public IOType getIoType() {
		return ioType;
	}

	protected void changeStateIndex(Integer index) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#cleanUp()
	 */
	@Override
	protected void cleanUp() {
		super.cleanUp();
		elecComp = null;
		switchIndex = 0;
		ioType = null;
		resisStateIds = new ArrayList<String>();
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#clone()
	 */
	@Override
	protected BaseVO<T> clone() {
		return super.clone();
	}

	public int getSwitchIndex() {
		return switchIndex;
	}

	public void setDisable(boolean disable) {
		this.disable = disable;
	}
}
