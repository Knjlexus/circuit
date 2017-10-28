package com.cas.circuit.vo;

import java.util.List;

import com.cas.circuit.Voltage;
import com.cas.circuit.consts.IOType;
import com.cas.circuit.po.VoltageIOPO;
import com.cas.util.StringUtil;
import com.cas.util.Util;

public class VoltageIO extends SwitchCtrl<VoltageIOPO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5178970830659911977L;

	private Terminal term1;
	private Terminal term2;
	private int voltType;
	private float requireVolt;
	private float deviation;

	/**
	 * 
	 */
	public VoltageIO() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param po
	 */
	public VoltageIO(VoltageIOPO po) {
		super(po);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void changeStateIndex(Integer index) {

	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
		if (!Util.isEmpty(po.getSwitchIn())) {
			resisStateIds = StringUtil.split(po.getSwitchIn());
		}
		if (po.getValue().startsWith("+") || po.getValue().startsWith("-")) {
			String value = po.getValue().substring(1);
			if (Util.isNumeric(value)) {
				voltType = Voltage.IS_DC;
				requireVolt = Float.parseFloat(value);
			}
		} else if (Util.isNumeric(po.getValue())) {
			voltType = Voltage.IS_AC;
			requireVolt = Float.parseFloat(po.getValue());
		} else {
			System.err.println("配置文件voltageIO中的电压值不合法 error:" + po.getValue());
		}
		if (Util.isNumeric(po.getDeviation())) {
			deviation = Float.parseFloat(po.getDeviation());
		}
		if ("output".equalsIgnoreCase(po.getType())) {
			ioType = IOType.OUTPUT;
		} else if ("input".equalsIgnoreCase(po.getType())) {
			ioType = IOType.INPUT;
		} else {
			ioType = IOType.BOTH;
		}
	}

	public boolean relateWith(List<VoltageIO> voltIos) {
		boolean contain = false;
		for (VoltageIO voltIO : voltIos) {
			if (voltIO.getPO().getTerm1Id().equals(getPO().getTerm1Id()) || voltIO.getPO().getTerm1Id().equals(getPO().getTerm2Id()) || voltIO.getPO().getTerm2Id().equals(getPO().getTerm1Id()) || voltIO.getPO().getTerm2Id().equals(getPO().getTerm2Id())) {
				contain = true;
			}
		}
		return contain;
	}

	public boolean contains(Terminal term) {
		ElecCompDef elecComp = (ElecCompDef) parent.getParent();
		if (!elecComp.getChildren().contains(term)) {
			return false;
		}
		return po.getTerm1Id().equals(term.getPO().getId()) || po.getTerm2Id().equals(term.getPO().getId());
	}

	public Terminal getAnotherTerm(Terminal term) {
		ElecCompDef elecComp = (ElecCompDef) parent.getParent();
		if (po.getTerm1Id().equals(term.getPO().getId())) {
			return elecComp.getTerminal(po.getTerm2Id());
		} else if (po.getTerm2Id().equals(term.getPO().getId())) {
			return elecComp.getTerminal(po.getTerm1Id());
		} else {
			return null;
		}
	}

	public float getRequireVolt() {
		return requireVolt;
	}

	public float getDeviation() {
		return deviation;
	}

	public Terminal getTerm1() {
		return term1;
	}

	public void setTerm1(Terminal term1) {
		this.term1 = term1;
	}

	public Terminal getTerm2() {
		return term2;
	}

	public void setTerm2(Terminal term2) {
		this.term2 = term2;
	}

	public int getVoltType() {
		return voltType;
	}

	@Override
	public String toString() {
		return po.getTerm1Id() + "--" + po.getTerm2Id();
	}
}
