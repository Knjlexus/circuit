package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.List;

import com.cas.circuit.CfgConst;
import com.cas.circuit.po.MagnetismPO;
import com.cas.util.vo.BaseVO;

public class Magnetism extends BaseVO<MagnetismPO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -679322081832666039L;
	private boolean effect;
	private List<VoltageIO> voltageIOs = new ArrayList<VoltageIO>();
	private List<ControlIO> controlIOs = new ArrayList<ControlIO>();
	private List<LightIO> lightIOs = new ArrayList<LightIO>();

	private List<VoltageIO> inputVoltageIOs = new ArrayList<VoltageIO>();
	private List<VoltageIO> outputVoltageIOs = new ArrayList<VoltageIO>();
	private List<ControlIO> outputControlIOs = new ArrayList<ControlIO>();
	private List<SensorIO> sensorIOs = new ArrayList<SensorIO>();

	@Override
	protected void addChild(BaseVO<?> child) {
		super.addChild(child);
		if (child instanceof ControlIO) {
			ElecCompDef elecComp = (ElecCompDef) parent;
			ControlIO control = (ControlIO) child;
			control.setElecComp(elecComp);
			controlIOs.add((ControlIO) child);
			if (control.getPO().getType().contains(CfgConst.SWITCH_CTRL_OUTPUT)) {
				outputControlIOs.add(control);
			}
		} else if (child instanceof VoltageIO) {
			ElecCompDef elecComp = (ElecCompDef) parent;
			VoltageIO voltIO = (VoltageIO) child;
			voltIO.setElecComp(elecComp);
			voltageIOs.add(voltIO);
			if (voltIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_OUTPUT)) {
				outputVoltageIOs.add(voltIO);
			}
			if (voltIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_INPUT)) {
				inputVoltageIOs.add(voltIO);
			}
		} else if (child instanceof SensorIO) {
			ElecCompDef elecComp = (ElecCompDef) parent;
			SensorIO sensorIO = (SensorIO) child;
			sensorIO.setElecComp(elecComp);
			sensorIOs.add(sensorIO);
		} else if (child instanceof LightIO) {
			LightIO lightIO = (LightIO) child;
			lightIOs.add(lightIO);
		}
	}

	/**
	 * 
	 */
	public Magnetism() {
		super();
	}

	/**
	 * @param po
	 */
	public Magnetism(MagnetismPO po) {
		super(po);
	}

	public boolean isEffect() {
		return effect;
	}

	public void setEffect(boolean effect) {
		this.effect = effect;
	}

	public List<VoltageIO> getVoltageIOs() {
		return voltageIOs;
	}

	public List<ControlIO> getControlIOs() {
		return controlIOs;
	}

	public List<LightIO> getLightIOs() {
		return lightIOs;
	}

	public List<VoltageIO> getInputVoltageIOs() {
		return inputVoltageIOs;
	}

	public List<VoltageIO> getOutputVoltageIOs() {
		return outputVoltageIOs;
	}

	public List<ControlIO> getOutputControlIOs() {
		return outputControlIOs;
	}

	public List<SensorIO> getSensorIOs() {
		return sensorIOs;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#cleanUp()
	 */
	@Override
	protected void cleanUp() {
		voltageIOs = new ArrayList<VoltageIO>();
		controlIOs = new ArrayList<ControlIO>();
		lightIOs = new ArrayList<LightIO>();
		effect = false;

		inputVoltageIOs = new ArrayList<VoltageIO>();
		outputVoltageIOs = new ArrayList<VoltageIO>();
		outputControlIOs = new ArrayList<ControlIO>();
		sensorIOs = new ArrayList<SensorIO>();

//		ioGroups = new ArrayList<List<VoltageIO>>();// [[0--1, 1--2], [11--12, 12--13], [14--15, 15--16, 16--17], [18--19]]
//		termGroups = new ArrayList<List<String>>();// [[0, 1, 2], [11, 12, 13], [14, 15, 16, 17], [18, 19]]
//		Key: 连接头ID , 与该连接头有关系的VoltageIOs
//		termVoltageIOGroupMap = new HashMap<String, List<VoltageIO>>();
	}
}
