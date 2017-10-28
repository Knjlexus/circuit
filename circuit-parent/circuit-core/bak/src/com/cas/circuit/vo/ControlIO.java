
package com.cas.circuit.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cas.circuit.CfgConst;
import com.cas.circuit.consts.IOType;
import com.cas.circuit.po.ControlIOPO;
import com.cas.robot.common.MouseEvent;
import com.cas.robot.common.util.ModelMotionUtil;
import com.cas.robot.common.util.Pool;
import com.cas.util.StringUtil;
import com.cas.util.Util;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * 包括:按钮(button) 和 旋钮(switch)
 */
public class ControlIO extends SwitchCtrl<ControlIOPO> {
	private static final long serialVersionUID = -8337555306092970185L;

	public static final String INTERACT_UNIDIR = "unidir";
	public static final String INTERACT_CLICK = "click";
	public static final String INTERACT_PRESS = "press";
	public static final String INTERACT_ROTATE = "rotate";

//	ReadOnly
	private List<Float> motionParams = new ArrayList<Float>();

	private Spatial model;
	private float speed = 4;
	private boolean smooth;

	private Vector3f zeroIndexLocation;

	private int motionIndex = 0;

	private Map<String, String> properties;

	public ControlIO() {
		super();
//		if (Dispatcher.getIns().getMainState() != null) {
//			Dispatcher.getIns().getMainState().getNowMachShell().controlIOs.add(this);
//		}
	}

	/**
	 * @param po
	 */
	public ControlIO(ControlIOPO po) {
		super(po);
	}

	@Override
	protected void toValueObject() {
		super.toValueObject();
		this.properties = getStringMap(po.getElementText());
		if (!Util.isEmpty(po.getSwitchIn())) {
			resisStateIds = StringUtil.split(po.getSwitchIn());
		}
		if (!Util.isEmpty(po.getMotionParams())) {
			List<String> rotateAngleStrs = StringUtil.split(po.getMotionParams());
			for (String string : rotateAngleStrs) {
				if (Util.isNumeric(string)) {
					motionParams.add(Float.parseFloat(string));
				}
			}
		}
		if (Util.isInteger(po.getDefSwitch())) {
			int defIndex = Integer.parseInt(po.getDefSwitch());
			switchIndex = defIndex;
			motionIndex = defIndex;
		}
		if (resisStateIds.size() > 0 && motionParams.size() != resisStateIds.size()) {
			System.err.println("名字为" + po.getName() + "模型名为" + po.getMdlName() + "的按钮的运动点个数为：" + motionParams.size() + "个运动点，但电阻状态点个数为：" + resisStateIds.size());
		}
		if (!Util.isEmpty(po.getSpeed()) && Util.isNumeric(po.getSpeed())) {
			speed = Float.parseFloat(po.getSpeed());
		}
		ioType = IOType.parseIOType(po.getType());
		smooth = "1".equals(po.getSmooth());
	}

	@Override
	protected void changeStateIndex(Integer index) {
		switchIndex = 1 - switchIndex;
	}

//	@Override
//	public void doSwitch(Integer index) {
//		super.doSwitch(index);
//	}

	/**
	 * 对ROTATE类型：index 0：向上滚， 1：向下滚. 对非ROTATE类型：index 为null
	 */
	public void switchStateChanged(final Integer wheel, final MouseEvent e) {
//		if (INTERACT_CLICK.equals(po.getInteract())) {
//			switchStart(1 - switchIndex);
//		} else if (INTERACT_PRESS.equals(po.getInteract())) {
//			if (switchIndex == 0 || (switchIndex == 1 && !((Magnetism) parent).isEffect())) {
//				switchStart(1 - switchIndex);
//			}
////		} else if (INTERACT_UNIDIR.equals(po.getInteract())) {
////			if (switchIndex == 1) {
////				switchStart(0);
////				// 磁生力
////				for (ControlIO otherButton : ((Magnetism) parent).getControlIOs()) {
////					if (otherButton != ControlIO.this && otherButton.getPO().getType().contains(CfgConst.SWITCH_CTRL_OUTPUT)) {
////						otherButton.doSwitch(1);
////					}
////				}
////			}
//		}
////		}
//		
		switchStart(1 - switchIndex);
	}

//	FIXME
	/**
	 * 对ROTATE类型：index 0：向上滚， 1：向下滚. 对非ROTATE类型：index 0：弹起， 1：按下
	 */
	public void playMotion(/* AssetManager assetManager, */MouseEvent e) {
		if (CfgConst.BUTTON_MOTION_ROTATE.equals(po.getMotion())) {
			// 迅速动
			ModelMotionUtil.rotateModel(model, po.getAxis(), motionParams.get(1 - motionIndex) - motionParams.get(motionIndex));
			switchEnd();
		} else if (CfgConst.BUTTON_MOTION_MOVE.equals(po.getMotion())) {
			if (smooth) {
			} else if (motionIndex == 0) {// 如果是从
				zeroIndexLocation = model.getLocalTranslation();
			} else {
				model.setLocalTranslation(zeroIndexLocation);
			}

			float dist = motionParams.get(1 - motionIndex) - motionParams.get(motionIndex);
			if (smooth) {
				ModelMotionUtil.moveModelSmooth(model, po.getAxis(), dist, speed, new Runnable() {
					@Override
					public void run() {
						Pool.getCircuitPool().submit(new Runnable() {
							@Override
							public void run() {
								try {
									switchEnd();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
				}, null);
			} else {
				ModelMotionUtil.moveModel(model, po.getAxis(), dist);
				switchEnd();
			}
		}
		motionIndex = 1 - motionIndex;
	}

	public void setModel(Spatial model) {
		if (model == null) {
			log.error("没有找到按钮名为" + po.getName() + "的模型");
		}
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.cfg.vo.BaseVO#cleanUp()
	 */
	@Override
	protected void cleanUp() {
		model = null;
		motionParams = new ArrayList<Float>();
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public List<Float> getMotionParams() {
		return motionParams;
	}

	@Override
	public String toString() {
		return "ControlIO []";
	}

}
