package com.cas.sim;

import java.util.function.Consumer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CardShuffleControl extends AbstractControl {

	private float rotated;

	private float speed = 4;

	private Vector3f dist;

	private float wait;

	private Consumer<Void> finish;

	public CardShuffleControl(Vector3f dist, Consumer<Void> finish) {
		this.dist = dist;
		this.finish = finish;
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (rotated < FastMath.PI) {
			tpf += tpf * speed;

			if (FastMath.PI - rotated > tpf) {
				spatial.rotate(0, 0, tpf);
			} else {
				spatial.rotate(0, 0, FastMath.PI - rotated);
			}
			rotated += tpf;
		} else {
			if (wait > 1) {
				if (spatial.getLocalTranslation().distance(dist) > .1f) {
					Vector3f v = FastMath.extrapolateLinear(tpf * speed, spatial.getLocalTranslation(), dist);
					spatial.setLocalTranslation(v);
				} else {
					spatial.removeControl(this);
					
					if(finish != null) {
						finish.accept(null);
					}
				}
			} else {
				wait += tpf;
			}
		}

	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}
}
