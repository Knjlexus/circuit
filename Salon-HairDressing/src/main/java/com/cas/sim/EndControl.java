package com.cas.sim;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class EndControl extends AbstractControl {

	private int index;
	private float len;
	private Vector3f dist;

	public EndControl(int i) {
		this.index = i;

		len = (3 - index) * 1.3f;

	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		if (spatial != null) {
			dist = new Vector3f(spatial.getLocalTranslation().add(len, 8, 8));
		}
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (spatial.getLocalTranslation().distance(dist) > 0.1f) {
			spatial.setLocalTranslation(FastMath.interpolateLinear(tpf, spatial.getLocalTranslation(), dist));
		} else {
			spatial.removeControl(this);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

}
