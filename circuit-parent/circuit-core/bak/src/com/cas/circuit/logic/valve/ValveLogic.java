package com.cas.circuit.logic.valve;

import java.util.List;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.Voltage;
import com.cas.circuit.util.MesureResult;
import com.cas.circuit.util.R;
import com.cas.circuit.vo.ControlIO;
import com.cas.circuit.vo.ElecCompDef;
import com.cas.circuit.vo.Jack;
import com.cas.gas.util.G;
import com.cas.gas.vo.BlockRelation;
import com.cas.gas.vo.BlockState;
import com.cas.util.vo.BaseVO;
import com.jme3.scene.Node;

public class ValveLogic extends BaseElectricCompLogic {

	private ElecCompDef def;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);
		def = getElecComp().getDef();
	}

	@Override
	public void onButtonAction(ControlIO button, boolean press) {
		// 判断Jack是否满足电压
		String jackName = button.getProperties().get("jackName");
		Jack jack = def.getJackMap().get(jackName);
		MesureResult result = R.matchRequiredVolt(Voltage.IS_DC, jack.getStitch().get("1"), jack.getStitch().get("2"), 24, 2);
		// 如果电磁阀上没有满足条件的电压，则允许切换
		if (result != null) {
			return;
		}
		String switchFrom = button.getProperties().get("switchFrom");
		String switchTo = button.getProperties().get("switchTo");
		if (press) {
			changeBlockState(switchFrom, switchTo);
		} else {
			changeBlockState(switchTo, switchFrom);
		}
	}

	private void changeBlockState(String oldId, String newId) {
		BlockState oldBlock = def.getBlockStatesMap().get(oldId);
		if (oldBlock != null) {
			List<BaseVO<?>> oldBlockList = oldBlock.getChildren();
			for (BaseVO<?> baseVO : oldBlockList) {
				if (baseVO instanceof BlockRelation) {
					BlockRelation relation = (BlockRelation) baseVO;
					def.blockRelationRemoved(relation);
					G.g().refreshGasPressure();
				}
			}
		}
		BlockState newBlock = def.getBlockStatesMap().get(newId);
		if (newBlock != null) {
			List<BaseVO<?>> newBlockList = newBlock.getChildren();
			for (BaseVO<?> baseVO : newBlockList) {
				if (baseVO instanceof BlockRelation) {
					BlockRelation relation = (BlockRelation) baseVO;
					def.blockRelationAdded(relation);
					G.g().refreshGasPressure();
				}
			}
		}
	}
}
