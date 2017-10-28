package com.cas.circuit.event;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 * 螺钉行为的控制
 */
public class BoltControl extends AbstractControl {
	public static final float rate = 4f;

	/**
	 * 拧起的距离
	 */
	private float distance;

	/**
	 * 移动的方向
	 */
	@Deprecated
	private Vector3f dir;

	@Override
	protected void controlUpdate(float tpf) {

	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

}
