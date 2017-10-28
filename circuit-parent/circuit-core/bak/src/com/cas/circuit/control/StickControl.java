package com.cas.circuit.control;

import java.util.List;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class StickControl extends AbstractControl {
	private float speed;

	private Vector3f total = new Vector3f();
	private float stick_out_length;
	private Vector3f max;
	private Vector3f dir;

	private List<Spatial> driveds;

	private boolean b;

	public StickControl(List<Spatial> driveds, Vector3f dir, float length, float speed) {
		this.driveds = driveds;
		this.dir = dir;
		this.speed = speed;

		max = dir.normalize().mult(length);
	}

	@Override
	protected void controlUpdate(float tpf) {
		Vector3f per = dir.mult(tpf * speed);
		for (Spatial drived : driveds) {
			drived.setUserData("stick_out", b);
		}
		if (b) {
//		杆子伸出
			float delta = per.length();
//			System.out.println("StickControl.controlUpdate(杆子伸出)");
			if (total.length() < max.length()) {
				total.addLocal(per);

				stick_out_length += delta;
				spatial.move(0, 0, delta);
				for (final Spatial spatial : driveds) {
					spatial.move(per);
				}
			} else {
				setEnabled(false);
				spatial.move(0, 0, max.length() - stick_out_length);
				for (final Spatial spatial : driveds) {
					spatial.move(max.add(total.negate()));
				}
				total.set(max);
				stick_out_length = max.length();
			}
		} else {
//		杆子收回
			float delta = per.length();
//			System.out.println("StickControl.controlUpdate(杆子收回)");
			if (total.length() >= per.length()) {
				total.addLocal(per.negate());

				stick_out_length -= delta;
				spatial.move(0, 0, -delta);
				for (final Spatial spatial : driveds) {
					spatial.move(per.negate());
				}
			} else {
				setEnabled(false);

				spatial.move(0, 0, -stick_out_length);

				for (final Spatial spatial : driveds) {
					spatial.move(total.negate());
				}
				stick_out_length = 0;
				total.set(0, 0, 0);
			}
		}
	}

	public void stickOut() {
		enabled = true;
		b = true;
//		total.set(0, 0, 0);
	}

	public void stickIn() {
		enabled = true;
		b = false;
//		total.set(max);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}
}
