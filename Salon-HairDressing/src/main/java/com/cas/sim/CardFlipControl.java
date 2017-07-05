package com.cas.sim;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CardFlipControl extends AbstractControl {

	public static final int bound = 2;

	private Vector3f dist;
	private float rotated;
	private boolean reachBound;

	public CardFlipControl(Vector3f dist) {
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
		}

		if (!reachBound && Math.abs(bound - spatial.getLocalTranslation().x) < 0.01f) {
			reachBound = true;
		}
//
		float x = FastMath.interpolateLinear(tpf, spatial.getLocalTranslation().x, reachBound ? dist.x : bound);
		float y = FastMath.interpolateLinear(tpf, spatial.getLocalTranslation().y, dist.y);
		float z = FastMath.interpolateLinear(tpf, spatial.getLocalTranslation().z, dist.z);
		spatial.setLocalTranslation(x, y, z);

		System.out.println("CardFlipControl.controlUpdate()" + spatial.getLocalTranslation() + "---" + dist);
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

}
