package com.cas.circuit.control;

import java.util.List;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class SwingControl extends AbstractControl {

	private boolean b;

	private List<Spatial> driveds;
	private Vector3f dir;
	private float total;
	private float degree;
	private float speed;

	public SwingControl(List<Spatial> driveds, Vector3f dir, float degree, float speed) {
		this.driveds = driveds;
		this.dir = dir;
		this.degree = degree;
		this.speed = speed;
	}

	@Override
	protected void controlUpdate(float tpf) {
		float rad = tpf * speed;
		Vector3f per = dir.mult(rad);
		if (b) {
			total += rad;
//			System.out.println("SwingControl.controlUpdate()" + total + "degree = " + degree);
			for (final Spatial spatial : driveds) {
				spatial.rotate(per.x, per.y, per.z);
			}
			if (total >= degree) {
				for (final Spatial spatial : driveds) {
					spatial.rotate(0, degree - total, 0);
				}
				total = degree;
				enabled = false;
			}
		} else {
			total -= rad;
//			System.out.println("SwingControl.controlUpdate()" + degree);
			for (final Spatial spatial : driveds) {
				spatial.rotate(-per.x, -per.y, -per.z);
			}
			if (total <= 0) {
				for (final Spatial spatial : driveds) {
					spatial.rotate(0, -total, 0);
				}
				total = 0;
				enabled = false;
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public void clamp() {
		enabled = true;
		b = true;
	}

	public void unclamp() {
		enabled = true;
		b = false;
	}

}
