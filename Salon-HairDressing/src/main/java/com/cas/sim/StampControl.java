package com.cas.sim;

import java.util.function.Consumer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class StampControl extends AbstractControl {
	private float speed = 4;
	private Consumer<Void> finish;
	private Vector3f dist;

	public StampControl(Consumer<Void> finish) {
		this.finish = finish;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		if (spatial != null) {
			dist = spatial.getUserData("DefLoc");
		}
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (spatial.getLocalTranslation().distance(dist) > 0.1f) {
			spatial.setLocalTranslation(FastMath.interpolateLinear(tpf * speed, spatial.getLocalTranslation(), dist));
		} else {
			spatial.removeControl(this);

			if (finish != null) {
				finish.accept(null);
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

}
