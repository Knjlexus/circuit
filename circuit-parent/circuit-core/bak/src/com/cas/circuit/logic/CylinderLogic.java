package com.cas.circuit.logic;

import java.util.ArrayList;
import java.util.List;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.gas.vo.GasPort;
import com.cas.util.StringUtil;
import com.cas.util.Util;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 气缸逻辑类
 */
public abstract class CylinderLogic extends BaseElectricCompLogic {
	protected GasPort portA;
	protected GasPort portB;

	protected List<Spatial> driveds = new ArrayList<Spatial>();

	/**
	 * 气缸运动一次所用时间（单位：毫秒）
	 */
	protected long time = 1000;

	protected Node rootNode;

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

//		找到AB两个气嘴
		portA = elecComp.getDef().getGasPort("A");
		portB = elecComp.getDef().getGasPort("B");

		String drivedMdlPath = elecComp.getProperty("drive");
		if (drivedMdlPath != null) {
			List<String> pathArr = StringUtil.split(drivedMdlPath,',');
			for(String path : pathArr){
				List<String> paths = StringUtil.split(path, '/');
				Node drived = elecCompMdl;
				for (String str : paths) {
					if (str.equals(".")) {
						drived = drived.getParent();
					} else {
						drived = (Node) drived.getChild(str);
						break;
					}
				}
				driveds.add(drived);
			}
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// 获得气缸运动一次的总时长
		String timeStr = elecComp.getProperty("time");
		if (Util.isNumeric(timeStr)) {
			time = Long.parseLong(timeStr);
		}
	}

	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}
}
