package com.cas.gas.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cas.circuit.ILinkTarget;
import com.cas.circuit.ILinker;
import com.cas.circuit.consts.IOType;
import com.cas.circuit.vo.SwitchCtrl;
import com.cas.gas.po.GasPortPO;
import com.cas.robot.common.util.JmeUtil;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

/**
 * 气口信息封装对象
 * @功能 GasPort.java
 * @作者 CWJ
 * @创建日期 2016年5月18日
 * @修改人 CWJ
 */
public class GasPort extends SwitchCtrl<GasPortPO> implements Savable, ILinkTarget {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2034783604384287728L;
//	气嘴模型
	private Spatial model;
//	// 气口上连接的气管
//	private Pipe pipe;
	// 当前是否有气流
	private boolean pressure;

//	一个气孔上只可能连接一根气管
	private List<ILinker> linkers = new ArrayList<ILinker>(1);
//	private List<Spatial> pipeMdls = new ArrayList<Spatial>(1);

	private Map<GasPort, BlockRelation> blockRelationMap = new HashMap<GasPort, BlockRelation>();

	private GasPort prev;
	private List<GasPort> nexts = new ArrayList<GasPort>();

	/**
	 * 
	 */
	public GasPort() {
	}

	/**
	 * @param po
	 */
	public GasPort(GasPortPO po) {
		super(po);
	}

	@Override
	protected void toValueObject() {
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
	protected void cleanUp() {
//		pipe = null;
		pressure = false;
		nexts = new ArrayList<GasPort>();
		linkers = new ArrayList<ILinker>();
		blockRelationMap = new HashMap<GasPort, BlockRelation>();
	}

	private void onReceivedGas() {
		if (model != null) {
			if (pressure) {
				JmeUtil.setSpatialHighLight(model, new ColorRGBA(0, 1, 1, 0.1f));
			} else {
				JmeUtil.setSpatialHighLight(model, ColorRGBA.BlackNoAlpha);
			}
		}
		if (parent != null) {
//			线缆中的连接头
			elecComp.getRef().getCompLogic().onReceivedGP(this);
		}
	}

	public void detory() {
		removePressure();
		blockRelationMap.clear();
	}

	@Override
	public String getDirection() {
		return po.getDirection();
	}

	@Override
	public Spatial getModel() {
		return model;
	}

	@Override
	public IOType getIoType() {
		return ioType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.circuit.ILinkTarget#getLinkers()
	 */
	@Override
	public List<ILinker> getLinkers() {
		return linkers;
	}

	public void setModel(Spatial model) {
		if (model == null) {
			log.error("元器件" + parent + "没有找到气嘴ID为" + po.getId() + "的模型");
		}
		this.model = model;
	}

//	public ILinker getPipe() {
//		return pipe;
//	}
//
//	public void setPipe(Pipe pipe) {
//		linkers.add(pipe);
//	}

	public boolean isPressure() {
		return pressure;
	}

	public void setPressure(boolean pressure) {
		this.pressure = pressure;
	}

	public GasPort getPrev() {
		return prev;
	}

	public List<GasPort> getNexts() {
		return nexts;
	}

	/**
	 * 新增气压
	 * @param prev 上一个输气口
	 */
	public void addPressure(GasPort prev) {
		removePressure();
		if (pressure) {
			return;
		}
		this.prev = prev;
		pressure = true;
		if (nexts.contains(prev)) {
			return;
		}
		// 找到所有与该气口相同的气口的气路
		// 同一元器件上的气口
		for (BlockRelation relation : blockRelationMap.values()) {
			if (!relation.isActivated()) {
				continue;
			}
			GasPort next = elecComp.getGasPort(relation.getAnotherPortId(this.getLocalKey()));
			if (next != null && !next.equals(prev) && !next.isPressure()) {
				next.addPressure(this);
				nexts.add(next);
			}
		}
		if (linkers.size() == 1) {
			Pipe pipe = (Pipe) linkers.get(0);
			// 同一气管上的气口
			if (pipe != null) {
				GasPort next = pipe.getAnother(this);
				if (next != null && !next.equals(prev) && !next.isPressure()) {
					next.addPressure(this);
					nexts.add(next);
				}
			}
		}
//		2.通知元器件,我的气压有变化, 你看着办
		onReceivedGas();
	}

	public void removePressure() {
		pressure = false;
		for (GasPort next : nexts) {
			next.setPressure(false);
			next.removePressure();
		}
		nexts.clear();
//		气压发生了变化
		onReceivedGas();
	}

	public Map<GasPort, BlockRelation> getBlockRelationMap() {
		return blockRelationMap;
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

	public String toString() {
		if (elecComp != null) {
			return elecComp.getPO().getName() + "[" + elecComp.getRef().getPO().getTagName() + "]" + "上的气管：" + po.getName();
		}
		return "未知";
	}
}
