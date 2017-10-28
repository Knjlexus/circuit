package com.cas.circuit.control;

import java.util.ArrayList;
import java.util.List;

import com.cas.robot.common.util.JmeUtil;
import com.cas.robot.common.util.UDUtil;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * @author Administrator
 */
public class TrackControl extends AbstractControl {
//	输送方向所在的轴
	private Vector3f axis;
//	输送的方向
	private int dir;
//	输送的速度
	private float speed = .15f;
//	被传送的物体
	private List<Spatial> objs = new ArrayList<Spatial>();

	@Override
	protected void controlUpdate(float tpf) {
		if (objs.size() == 0) {
			return;
		}
		Vector3f max = null;
		Vector3f min = null;
		for (Spatial obj : objs) {
			obj.move(axis.normalize().mult(dir * speed * tpf));
			Vector3f pos = obj.getLocalTranslation();
			max = JmeUtil.getVector3f(UDUtil.getString(obj, "max"));
			min = JmeUtil.getVector3f(UDUtil.getString(obj, "min"));
			if (min != null) {
				if (pos.x < min.x) {
					pos.setX(min.x);
				}
				if (pos.y < min.y) {
					pos.setY(min.y);
				}
				if (pos.z < min.z) {
					pos.setZ(min.z);
				}
			}
			if (max != null) {
				if (pos.x > max.x) {
					pos.setX(max.x);
				}
				if (pos.y > max.y) {
					pos.setY(max.y);
				}
				if (pos.z > max.z) {
					pos.setZ(max.z);
				}
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public boolean addObj(Spatial obj) {
		return objs.add(obj);
	}
	
	public boolean removeObj(Spatial obj){
		return objs.remove(obj);
	}

	public void setAxis(Vector3f axis) {
		this.axis = axis;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
}
