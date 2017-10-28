package com.cas.circuit.control;

import com.cas.robot.common.consts.JmeConst;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class PointerControl extends AbstractControl {
//	private MenuItemExecutor executor;
//	private Callable<Void> call;
//	private boolean notify;
	private float rotateSpeed = 240;
//	private boolean smooth;

	public PointerControl() {
	}

	public void setRotateSpeed(float rotateSpeed) {
		this.rotateSpeed = rotateSpeed;
	}

	@Override
	protected void controlUpdate(float tpf) {
//		String axis = spatial.getUserData(JmeConst.ROTATE_AXIS);
//		axis = axis.toLowerCase();
		Float angle = spatial.getUserData(JmeConst.ROTATE_ANGLE);
		int piDeg = 180;
//		if ((!smooth || (angle == 0 && !notify)) && call != null) {
//			try {
//				call.call();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			notify = true;
//			setEnabled(false);
//		} else 
		if (angle > 0) {
//			notify = false;
//			switch (axis.charAt(0)) {
//			case 'x':
//				spatial.rotate(rotateSpeed * tpf * FastMath.PI / piDeg, 0, 0);
//				break;
//			case 'y':
//				spatial.rotate(0, rotateSpeed * tpf * FastMath.PI / piDeg, 0);
//				break;
//			case 'z':
			spatial.rotate(0, 0, rotateSpeed * tpf * FastMath.PI / piDeg);
//				break;
//			default:
//				break;
//			}
			angle -= rotateSpeed * tpf;
			if (angle <= 0) {
//				switch (axis.charAt(0)) {
//				case 'x':
//					spatial.rotate(angle * FastMath.PI / piDeg, 0, 0);
//					break;
//				case 'y':
//					spatial.rotate(0, angle * FastMath.PI / piDeg, 0);
//					break;
//				case 'z':
				spatial.rotate(0, 0, angle * FastMath.PI / piDeg);
//					break;
//				default:
//					break;
//				}
				angle = 0f;
			}
			spatial.setUserData(JmeConst.ROTATE_ANGLE, angle);
		} else {
//			notify = false;

//			switch (axis.charAt(0)) {
//			case 'x':
//				spatial.rotate(-rotateSpeed * tpf * FastMath.PI / piDeg, 0, 0);
//				break;
//			case 'y':
//				spatial.rotate(0, -rotateSpeed * tpf * FastMath.PI / piDeg, 0);
//				break;
//			case 'z':
			spatial.rotate(0, 0, -rotateSpeed * tpf * FastMath.PI / piDeg);
//				break;
//			default:
//				break;
//			}
			angle += rotateSpeed * tpf;
			if (angle >= 0) {
//				switch (axis.charAt(0)) {
//				case 'x':
//					spatial.rotate(angle * FastMath.PI / piDeg, 0, 0);
//					break;
//				case 'y':
//					spatial.rotate(0, angle * FastMath.PI / piDeg, 0);
//					break;
//				case 'z':
				spatial.rotate(0, 0, angle * FastMath.PI / piDeg);
//					break;
//				default:
//					break;
//				}
				angle = 0f;
			}
			spatial.setUserData(JmeConst.ROTATE_ANGLE, angle);
//		} else {
//			动画结束
//			if (!notify && executor != null) {
//				executor.execute();
//				notify = true;
//			}

		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}
//
//	/**
//	 * @param call
//	 */
//	public void setExecutor(Callable<Void> call, boolean smooth) {
//		this.call = call;
//		this.smooth = smooth;
//	}

//	/**
//	 * @param e
//	 */
//	public void setExecutor(MenuItemExecutor e) {
//		this.executor = e;
//	}

}
