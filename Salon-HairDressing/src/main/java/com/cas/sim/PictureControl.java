package com.cas.sim;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class PictureControl extends AbstractControl {

	private float speed = 8;

	private float scale;

	private Vector3f orig = new Vector3f(-10.0f, 1.0f, 0.0f);
	private Vector3f dist = new Vector3f(-1.8f, 1, 6);

	public PictureControl() {
		enabled = false;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		spatial.setLocalTranslation(orig);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (spatial != null) {
			scale = 0;
			spatial.setLocalTranslation(orig);
		}
	}

	@Override
	protected void controlUpdate(float tpf) {
//		缩放
		if (scale < 13) {
			scale += tpf * speed;
			float s = FastMath.interpolateLinear(tpf, scale, 13);
			spatial.setLocalScale(s);
		}
//		平移
		if (spatial.getLocalTranslation().distance(dist) > 0.1f) {
			spatial.setLocalTranslation(FastMath.interpolateLinear(tpf * speed, spatial.getLocalTranslation(), dist));
		} else {
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

}
