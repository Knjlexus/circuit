package com.cas.circuit.control;

import java.util.List;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class MotorControl extends AbstractControl {

	private List<Spatial> rotates;
//	1：表示顺时针
//	-1：表示逆时针
	private int rotateDir;
	private float speed = 1;
//	0：X轴, 1：Y轴，2：Z轴
	private int axis = 0;

	public MotorControl(List<Spatial> rotates) {
		this.rotates = rotates;
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (axis == 0) {
			for (Spatial spatial : rotates) {
				spatial.rotate(tpf * speed * rotateDir, 0, 0);
			}
		} else if (axis == 1) {
			for (Spatial spatial : rotates) {
				spatial.rotate(0, tpf * speed * rotateDir, 0);
			}
		} else if (axis == 2) {
			for (Spatial spatial : rotates) {
				spatial.rotate(0, 0, tpf * speed * rotateDir);
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public void setRotateDir(int rotateDir) {
		this.rotateDir = rotateDir;
		for (Spatial rotate : rotates) {
			rotate.setUserData("dir", rotateDir);
		}
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setAxis(int axis) {
		this.axis = axis;
	}

}
