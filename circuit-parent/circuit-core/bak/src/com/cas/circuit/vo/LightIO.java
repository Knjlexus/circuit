package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.List;

import com.cas.circuit.po.LightIOPO;
import com.cas.robot.common.util.JmeUtil;
import com.cas.util.StringUtil;
import com.cas.util.Util;
import com.cas.util.vo.BaseVO;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

public class LightIO extends BaseVO<LightIOPO> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4227479609709727765L;

	private Spatial model;
	private ColorRGBA glowColor;

	/**
	 * 
	 */
	public LightIO() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param po
	 */
	public LightIO(LightIOPO po) {
		super(po);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
		List<String> glowColorStr = null;
		List<Float> glowColorList = new ArrayList<Float>();
		if (!Util.isEmpty(po.getGlowColor())) {
			glowColorStr = StringUtil.split(po.getGlowColor());
		}
		if (glowColorStr != null) {
			for (String string : glowColorStr) {
				if (Util.isNumeric(string)) {
					glowColorList.add(Float.parseFloat(string));
				}
			}
		}
		if (glowColorList.size() == 4) {
			glowColor = new ColorRGBA(glowColorList.get(0), glowColorList.get(1), glowColorList.get(2), glowColorList.get(3));
		} else {
			log.error("模型名为" + po.getMdlName() + "的灯颜色配置错误");
		}
	}

	public void openLight() {
		JmeUtil.setSpatialHighLight(model, glowColor);
	}

	public void closeLight() {
		JmeUtil.setSpatialHighLight(model, ColorRGBA.BlackNoAlpha);
	}

	public Spatial getModel() {
		return model;
	}

	public void setModel(Spatial model) {
		if (model == null) {
			log.error("没有找到指示灯名为" + po.getName() + "的模型");
		}
		this.model = model;
	}

	@Override
	protected LightIO clone() {
		LightIO cloned = new LightIO();
//		model 是否要克隆
		cloned.setPO(po);
		return cloned;
	}
}
