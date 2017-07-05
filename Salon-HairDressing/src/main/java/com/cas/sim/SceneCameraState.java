package com.cas.sim;

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

public class SceneCameraState extends BaseState implements ActionListener, AnalogListener {
	protected Quaternion rot = new Quaternion();
	protected Vector3f vector = new Vector3f(0, 0, 5);
	protected Vector3f focus = new Vector3f();

	protected boolean moved = false;
	protected boolean movedR = false;
	protected boolean buttonDownL = false;
	protected boolean buttonDownR = false;
	protected boolean buttonDownM = false;
	protected boolean checkClickL = false;
	protected boolean checkClickR = false;
	protected boolean checkClickM = false;
	protected boolean checkReleaseL = false;
	protected boolean checkReleaseR = false;
	protected boolean checkReleaseM = false;
	protected boolean checkDragged = false;
	protected boolean checkDraggedR = false;
	protected boolean checkReleaseLeft = false;
	protected boolean checkReleaseRight = false;
	protected boolean shiftModifier = false;

	public enum View {
		Front, Left, Right, Top, Bottom, Back, User
	}

	public SceneCameraState() {
	}

	@Override
	public void initializeLocal() {
		// 添加鼠标监听事件
//		inputManager.addMapping("MouseAxisX", new MouseAxisTrigger(MouseInput.AXIS_X, false));
//		inputManager.addMapping("MouseAxisY", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
//		inputManager.addMapping("MouseAxisX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));
//		inputManager.addMapping("MouseAxisY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
//		inputManager.addMapping("MouseWheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
//		inputManager.addMapping("MouseWheel-", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
//		inputManager.addMapping("MouseButtonLeft", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
//		inputManager.addMapping("MouseButtonMiddle", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
//		inputManager.addMapping("MouseButtonRight", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
//		inputManager.addMapping("ShiftModifier", new KeyTrigger(KeyInput.KEY_LSHIFT));
		// 注册监听
//		inputManager.addRawInputListener(this);
//		inputManager.addListener(this, "MouseAxisX", "MouseAxisY", "MouseAxisX-", "MouseAxisY-", "MouseWheel", "MouseWheel-", "MouseButtonLeft", "MouseButtonMiddle", "MouseButtonRight", "ShiftModifier");

//		float aspect = (float) cam.getWidth() / cam.getHeight();
//		cam.setParallelProjection(false);
//		cam.setFrustumPerspective(45f, aspect, 0.01f, 1000);

		switchToView(View.Top);
	}

//	protected boolean doToggleOrthoPerspMode() {
//		float aspect = (float) cam.getWidth() / cam.getHeight();
//		if (!cam.isParallelProjection()) {
//			cam.setParallelProjection(true);
//			float h = cam.getFrustumTop();
//			float w;
//			float dist = cam.getLocation().distance(focus);
//			float fovY = FastMath.atan(h) / (FastMath.DEG_TO_RAD * .5f);
//			h = FastMath.tan(fovY * FastMath.DEG_TO_RAD * .5f) * dist;
//			w = h * aspect;
//			cam.setFrustum(-1000, 1000, -w, w, h, -h);
//			return true;
//		} else {
//			cam.setParallelProjection(false);
//			cam.setFrustumPerspective(45f, aspect, 1, 1000);
//			return false;
//		}
//	}

	@Override
	public void onAnalog(String name, float value, float tpf) {
		if (!isEnabled()) {
			return;
		}
		if ("MouseAxisX".equals(name)) {
			moved = true;
			movedR = true;

			if (buttonDownL || (buttonDownM && !shiftModifier)) {
				doRotateCamera(Vector3f.UNIT_Y, -value * 2.5f);
			}
			if (buttonDownR || (buttonDownM && shiftModifier)) {
				doPanCamera(value * 2.5f, 0);
			}

		} else if ("MouseAxisY".equals(name)) {
			moved = true;
			movedR = true;

			if (buttonDownL || (buttonDownM && !shiftModifier)) {
				doRotateCamera(cam.getLeft(), -value * 2.5f);
			}
			if (buttonDownR || (buttonDownM && shiftModifier)) {
				doPanCamera(0, -value * 2.5f);
			}

		} else if ("MouseAxisX-".equals(name)) {
			moved = true;
			movedR = true;

			if (buttonDownL || (buttonDownM && !shiftModifier)) {
				doRotateCamera(Vector3f.UNIT_Y, value * 2.5f);
			}
			if (buttonDownR || (buttonDownM && shiftModifier)) {
				doPanCamera(-value * 2.5f, 0);
			}

		} else if ("MouseAxisY-".equals(name)) {
			moved = true;
			movedR = true;

			if (buttonDownL || (buttonDownM && !shiftModifier)) {
				doRotateCamera(cam.getLeft(), value * 2.5f);
			}
			if (buttonDownR || (buttonDownM && shiftModifier)) {
				doPanCamera(0, value * 2.5f);
			}

		} else if ("MouseWheel".equals(name)) {
			doZoomCamera(.1f);
		} else if ("MouseWheel-".equals(name)) {
			doZoomCamera(-.1f);
		}
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (!isEnabled()) {
			return;
		}
		if ("ShiftModifier".equals(name)) {
			shiftModifier = isPressed;

//			doToggleOrthoPerspMode();
		}

		if ("MouseButtonLeft".equals(name)) {
			if (isPressed) {
				if (!buttonDownL) { // mouse clicked
					checkClickL = true;
					checkReleaseL = false;
//					@Nullable
//					Vector3f r = JmeUtil.getContactPointFromCursor(rootNode, cam, inputManager);
//					if (r != null) {
//						setCamFocus(r);
//						fairy.setLocalTranslation(r);
//					}
				}
			} else {
				if (buttonDownL) { // mouse released
					checkReleaseL = true;
					checkClickL = false;
				}
			}
			buttonDownL = isPressed;
		}
		if ("MouseButtonRight".equals(name)) {
			if (isPressed) {
				if (!buttonDownR) { // mouse clicked
					checkClickR = true;
					checkReleaseR = false;
				}
			} else {
				if (buttonDownR) { // mouse released
					checkReleaseR = true;
					checkClickR = false;
				}
			}
			buttonDownR = isPressed;
		}
		if ("MouseButtonMiddle".equals(name)) {

			if (isPressed) {
				if (!buttonDownM) { // mouse clicked
					checkClickM = true;
					checkReleaseM = false;
				}
			} else {
				if (buttonDownM) { // mouse released
					checkReleaseM = true;
					checkClickM = false;
				}
			}
			buttonDownM = isPressed;
		}
	}

	@Override
	public void update(float tpf) {
		if (moved) {
			// moved, check for drags
			if (checkReleaseL || checkReleaseR || checkReleaseM) {
				// drag released
				checkReleaseL = false;
				checkReleaseR = false;
				checkReleaseM = false;
			}
			moved = false;
		} else {
			// not moved, check for just clicks
			if (checkClickL) {
				checkClickL = false;
			}
			if (checkReleaseL) {
				checkReleaseL = false;
			}
			if (checkClickR) {
				checkClickR = false;
			}
			if (checkReleaseR) {
				checkReleaseR = false;
			}
			if (checkClickM) {
				checkClickM = false;
			}
			if (checkReleaseM) {
				checkReleaseM = false;
			}
		}
	}

	/**
	 * 沿轴移动相机
	 * 
	 * @param axis
	 * @param amount
	 */
	protected void doRotateCamera(Vector3f axis, float amount) {
		if (axis.equals(cam.getLeft())) {
			float elevation = -FastMath.asin(cam.getDirection().y);
			amount = Math.min(Math.max(elevation + amount, -FastMath.HALF_PI), FastMath.HALF_PI) - elevation;
		}
		rot.fromAngleAxis(amount, axis);
		cam.getLocation().subtract(focus, vector);
		rot.mult(vector, vector);
		focus.add(vector, cam.getLocation());

		Quaternion curRot = cam.getRotation().clone();
		cam.setRotation(rot.mult(curRot));
	}

	/**
	 * 摇动镜头
	 * 
	 * @param left
	 * @param up
	 */
	protected void doPanCamera(float left, float up) {
		cam.getLeft().mult(left, vector);
		vector.scaleAdd(up, cam.getUp(), vector);
		vector.multLocal(cam.getLocation().distance(focus));
		cam.setLocation(cam.getLocation().add(vector));
		focus.addLocal(vector);
	}

	protected void doMoveCamera(float forward) {
		cam.getDirection().mult(forward, vector);
		cam.setLocation(cam.getLocation().add(vector));
	}

	/**
	 * 镜头放大缩小
	 * 
	 * @param amount
	 */
	protected void doZoomCamera(float amount) {
//		Vector3f offset = focus.add(cam.getLocation().negate()).normalize().mult(amount);
//		cam.setLocation(cam.getLocation().add(offset));
//		
//		System.out.println("SceneCameraState.doZoomCamera()" + offset);

		amount = cam.getLocation().distance(focus) * amount;
		float dist = cam.getLocation().distance(focus);
		amount = dist - Math.max(0f, dist - amount);

		Vector3f loc = cam.getLocation().clone();
		loc.scaleAdd(amount, cam.getDirection(), loc);
		cam.setLocation(loc);
	}

	/**
	 * 切换视角
	 * 
	 * @param view
	 */
	public void switchToView(final View view) {
		float dist = cam.getLocation().distance(focus);
		switch (view) {
		case Front:
			cam.setLocation(new Vector3f(focus.x, focus.y, focus.z + dist));
			cam.lookAt(focus, Vector3f.UNIT_Y);
			break;
		case Left:
			cam.setLocation(new Vector3f(focus.x + dist, focus.y, focus.z));
			cam.lookAt(focus, Vector3f.UNIT_Y);
			break;
		case Right:
			cam.setLocation(new Vector3f(focus.x - dist, focus.y, focus.z));
			cam.lookAt(focus, Vector3f.UNIT_Y);
			break;
		case Back:
			cam.setLocation(new Vector3f(focus.x, focus.y, focus.z - dist));
			cam.lookAt(focus, Vector3f.UNIT_Y);
			break;
		case Top:
			cam.setLocation(new Vector3f(focus.x, focus.y + dist, focus.z));
			cam.lookAt(focus, Vector3f.UNIT_Z.mult(-1));

			break;
		case Bottom:
			cam.setLocation(new Vector3f(focus.x, focus.y - dist, focus.z));
			cam.lookAt(focus, Vector3f.UNIT_Z);
			break;
		case User:
		default:
		}
	}

	public void setCamFocus(final Vector3f focus, final boolean moveCamera) {
		if (moveCamera) {
			cam.setLocation(cam.getLocation().add(focus.subtract(this.focus)));
		}
		this.focus.set(focus);
	}

	public void setCamFocus(Vector3f focus) {
		setCamFocus(focus, false);
	}

	public void moveCamera(Vector3f focus) {
		cam.setLocation(cam.getLocation().add(focus.subtract(this.focus)));
	}

	@Override
	public void cleanup() {
		super.cleanup();
	}
}
