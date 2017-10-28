package com.cas.circuit.control;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.clone.Cloner;

public class Luosi4AnimControl extends AbstractControl implements Cloneable {

	/**
	 * 当前目标模型已经移动的变量
	 */
	private float tmpOffset;
	/**
	 * 模型移动速度
	 */
	private float speed = 0.015f;
	/**
	 * 总移动量
	 */
	private float offset = 0.0005f;
	private boolean moveDown = true;

	public Luosi4AnimControl() {
		enabled = false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	protected void controlUpdate(float tpf) {
		float per = tpf * speed;
		if (moveDown) {
			spatial.move(0, per, 0);
			tmpOffset = tmpOffset + per;
		} else {
			spatial.move(0, -per, 0);
			tmpOffset = tmpOffset - per;
		}
		if (tmpOffset >= offset) {
			moveDown = false;
		}
//		System.out.println(tmpOffset);
		if (tmpOffset <= 0) {
			spatial.move(0, - tmpOffset, 0);
			tmpOffset = 0;
			moveDown = true;
			enabled = false;
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

	@Override
	public void cloneFields(Cloner cloner, Object original) {
		super.cloneFields(cloner, original);
		enabled = false;
	}

}
