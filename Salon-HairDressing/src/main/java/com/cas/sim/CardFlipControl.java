package com.cas.sim;

import java.util.function.Consumer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CardFlipControl extends AbstractControl {

	private Vector3f dist;
	private float rotated;
	private Consumer<Void> finish;
	private float speed = 4;

	public CardFlipControl(Vector3f dist, Consumer<Void> finish) {
		this.dist = dist;
		this.finish = finish;
	}

	@Override
	protected void controlUpdate(float tpf) {
		tpf *= speed;

		if (rotated < FastMath.PI) {
			if (FastMath.PI - rotated > tpf) {
				spatial.rotate(0, 0, tpf);
			} else {
				spatial.rotate(0, 0, FastMath.PI - rotated);
			}
			rotated += tpf;
		}

		if (spatial.getLocalTranslation().distance(dist) > 0.1f) {
			spatial.setLocalTranslation(FastMath.interpolateLinear(tpf, spatial.getLocalTranslation(), dist));
		} else {
			spatial.removeControl(this);

//			显示大图
			if (finish != null) {
				finish.accept(null);
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

}
