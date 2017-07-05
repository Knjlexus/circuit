package com.cas.sim;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * @author 张振宇 Jul 27, 2015 3:34:00 PM
 */
public class MouseEvent {
	public static final int WHEEL_UP = 0;
	public static final int WHEEL_DOWN = 1;

	private MouseAction action;
	private Spatial spatial;
	private Vector3f contactPoint;
	private Vector3f contactNormal;
	private boolean suspend;

	private int wheel = -1;

	public MouseEvent(Spatial spatial, Vector3f contactPoint, Vector3f contactNormal) {
		super();
		this.spatial = spatial;
		this.contactPoint = contactPoint;
		this.contactNormal = contactNormal;
	}

	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
	}

	/**
	 * @return the wheel
	 */
	public int getWheel() {
		return wheel;
	}

	/**
	 * @param wheel the wheel to set
	 */
	public void setWheel(int wheel) {
		this.wheel = wheel;
	}

	public Spatial getSpatial() {
		return spatial;
	}

	public Vector3f getContactPoint() {
		return contactPoint;
	}

	public Vector3f getContactNormal() {
		return contactNormal;
	}

	public void suspended() {
		suspend = true;
	}

	/**
	 * @return the suspend
	 */
	public boolean isSuspend() {
		return suspend;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(MouseAction action) {
		this.action = action;
	}

	/**
	 * @return the action
	 */
	public MouseAction getAction() {
		return action;
	}

}
