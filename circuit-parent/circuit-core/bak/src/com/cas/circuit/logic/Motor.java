package com.cas.circuit.logic;

import java.util.ArrayList;
import java.util.List;

import com.cas.circuit.BaseElectricCompLogic;
import com.cas.circuit.control.MotorControl;
import com.cas.circuit.control.TrackControl;
import com.cas.circuit.vo.Terminal;
import com.cas.robot.common.util.JmeUtil;
import com.cas.util.MathUtil;
import com.cas.util.StringUtil;
import com.cas.util.Util;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public abstract class Motor extends BaseElectricCompLogic {
	protected MotorControl motorControl;
//	输送带的Control
	protected List<MotionEvent> trackContrls = new ArrayList<MotionEvent>();

	protected TrackControl trackControl;
//	有正负， 负数表示轴承的旋转方向与电机真实方向相反
	protected float slowdown = 1; // 电机的减速比
//	电机每圈 履带上物体移动的距离,单位 mm
	protected float movePerCycle = 1f;

	public Motor() {
	}

	@Override
	public void initialize(Node elecCompMdl) {
		super.initialize(elecCompMdl);

//		电动机带动轴承转动
		String parentPath = elecComp.getProperty("parent");
		List<String> paths = StringUtil.split(parentPath, '/');
		Node parent = elecCompMdl;
		for (String path : paths) {
			if (path.equals(".")) {
				parent = parent.getParent();
			} else {
				parent = (Node) parent.getChild(path);
			}
		}
//		电机减速比
		slowdown = MathUtil.parseFloat(elecComp.getProperty("slowdown"), slowdown);

		String rotateMdls = elecComp.getProperty("rotate");
		List<String> mdlNames = StringUtil.split(rotateMdls, ',');
		List<Spatial> mdls = new ArrayList<Spatial>();
		Spatial sp = null;
		for (String mdlName : mdlNames) {
			sp = parent.getChild(mdlName);
			if (sp == null) {
				throw new RuntimeException("在" + parent.getName() + "没有找到名为" + mdlName + "的模型");
			}
			mdls.add(sp);
		}
		elecCompMdl.addControl(motorControl = new MotorControl(mdls));
		int axis = MathUtil.parseInt(elecComp.getProperty("axis"), 0);
		motorControl.setAxis(axis);
		motorControl.setEnabled(false);

//		找到输送带，并加入输送带的Control，设置一些基本的参数
		String trackProperty = elecComp.getProperty("track");
		if (Util.isEmpty(trackProperty)) {
			return;
		}
		String trackInfos[] = trackProperty.split("\\|");
		Spatial track = parent.getChild(trackInfos[0]);
		track.addControl(trackControl = new TrackControl());
		trackControl.setEnabled(false);

		String str_tracked = elecComp.getProperty("tracked");
		if (str_tracked != null) {
			Spatial tracked = parent.getChild(str_tracked);
			if (tracked != null) {
				trackControl.addObj(tracked);
			}
		}
		trackControl.setAxis(JmeUtil.getVector3f(trackInfos[1]));
//		电机转过一圈，履带上物体移动的距离
		movePerCycle = MathUtil.parseFloat(elecComp.getProperty("movePerCycle"), movePerCycle);
	}

	@Override
	protected void onReceivedLocal(Terminal terminal) {
		super.onReceivedLocal(terminal);
	}

	public void addTrackControl(MotionEvent motionClone) {
		if (trackContrls == null) {
			trackContrls = new ArrayList<MotionEvent>();
		}
		trackContrls.add(motionClone);
	}

}
