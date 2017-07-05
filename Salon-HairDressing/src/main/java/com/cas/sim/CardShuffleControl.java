package com.cas.sim;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CardShuffleControl extends AbstractControl {

	private float rotated;

	private Vector3f dist;

	public CardShuffleControl(Vector3f dist) {
		this.dist = dist;
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (rotated < FastMath.PI) {
			if (FastMath.PI - rotated > tpf) {
				spatial.rotate(0, 0, tpf);
			} else {
				spatial.rotate(0, 0, FastMath.PI - rotated);
			}
			rotated += tpf;
		} else {
			if (spatial.getLocalTranslation().distance(dist) > .01f) {
				Vector3f v = FastMath.extrapolateLinear(tpf, spatial.getLocalTranslation(), dist);
				spatial.setLocalTranslation(v);
			}
		}

	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}
}
