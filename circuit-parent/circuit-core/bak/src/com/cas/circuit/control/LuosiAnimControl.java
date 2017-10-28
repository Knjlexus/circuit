package com.cas.circuit.control;

import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.clone.Cloner;

public class LuosiAnimControl extends AbstractControl implements Cloneable {
//	/**
//	 * 模型沿轴移动和旋转
//	 */
//	private String axis;
//	/**
//	 * 模型沿轴移动的正负方向
//	 */
//	private int moveDirection;
	/**
	 * 模型移动速度
	 */
	private float speed = 0.015f;
	/**
	 * 模型移动相对位移
	 */
	private float offset = 0.0025f;
	/**
	 * 当前目标模型已经移动的位移
	 */
	private float tmpOffset;
//	/**
//	 * 模型沿轴旋转的顺时针或逆时针
//	 */
//	private int rotateDirection;
//
//	/**
//	 * 旋转到位后执行内容
//	 */
//	private Runnable finish;

	private boolean moveUp = true;

	public LuosiAnimControl() {
		enabled = false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	protected void controlUpdate(float tpf) {
//		float eachRad = 30 * FastMath.DEG_TO_RAD;
		float per = tpf * speed;
		if (tmpOffset >= offset) {
			moveUp = false;
		}
		if (moveUp) {
			tmpOffset += per;
			perMoveAndRotate(per);
		} else {
			tmpOffset -= per;
			perMoveAndRotate(-per);
		}
		if (tmpOffset <= 0) {
			perMoveAndRotate(-tmpOffset);
			tmpOffset = 0;
			moveUp = true;
			enabled = false;
		}
	}

	private void perMoveAndRotate(float per) {
//		if (JmeConst.AXIS_X == axis) {
//			spatial.move(per, 0, 0);
//			spatial.rotate(eachRad, 0, 0);
//		} else if (JmeConst.AXIS_Y == axis) {
		spatial.move(0, per, 0);
		spatial.rotate(0, 30 * FastMath.DEG_TO_RAD, 0);
//		} else if (JmeConst.AXIS_Z == axis) {
//			spatial.move(0, 0, per);
//			spatial.rotate(0, 0, eachRad);
//		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

//	public void moveAndRotate(int rotateDirection, int moveDirection, float offset) {
//		this.rotateDirection = rotateDirection;
//		this.moveDirection = moveDirection;
//		this.offset = FastMath.abs(offset);
//		this.finish = null;
//		this.enabled = true;
//	}

//	public void moveAndRotate(int rotateDirection, int moveDirection, float offset, Runnable finish) {
//		this.rotateDirection = rotateDirection;
//		this.moveDirection = moveDirection;
//		this.offset = FastMath.abs(offset);
//		this.finish = finish;
//		this.enabled = true;
//	}

//	public void setSpeed(float speed) {
//		this.speed = speed;
//	}

	@Override
	public void cloneFields(Cloner cloner, Object original) {
		super.cloneFields(cloner, original);
		enabled = false;
	}

}
