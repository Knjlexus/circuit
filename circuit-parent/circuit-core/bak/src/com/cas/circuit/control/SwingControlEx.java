package com.cas.circuit.control;

import java.util.List;

import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.MouseEventState;
import com.cas.robot.common.control.BaseAppControl;
import com.cas.util.Util;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class SwingControlEx extends BaseAppControl {
	private boolean b;

	private List<Spatial> driveds;
	private Vector3f dir;
	private float total;
	private float degree;
	private float speed;

	private Node pieceLoc1;
	private Node pieceLoc2;

	private Spatial start1;
	private Spatial start2;

	private Spatial end1;
	private Spatial end2;

	private boolean swing;

	private ItemControl control1;

	private ItemControl control2;

	public SwingControlEx(Node elecCompMdl, List<Spatial> driveds, Vector3f dir, float degree, float speed) {
		super(Dispatcher.getIns().getMainApp());
		this.driveds = driveds;
		this.dir = dir;
		this.degree = degree;
		this.speed = speed;

		pieceLoc1 = (Node) elecCompMdl.getChild("pieceLoc1");
		pieceLoc2 = (Node) elecCompMdl.getChild("pieceLoc2");

		start1 = elecCompMdl.getChild("start1");
		start2 = elecCompMdl.getChild("start2");

		end1 = elecCompMdl.getChild("end1");
		end2 = elecCompMdl.getChild("end2");
	}

	@Override
	protected void controlUpdate(float tpf) {
		Vector3f origin = start1.getWorldTranslation();
		Vector3f goal = end1.getWorldTranslation();
		float limit = FastMath.abs(goal.distance(origin));

		Ray ray = new Ray(origin, goal.add(origin.negate()).normalize());

		CollisionResults results = new CollisionResults();
		rootNode.collideWith(ray, results);
		for (CollisionResult result : results) {
			if (result.getDistance() > limit) {
				continue;
			}
			Geometry geometry = result.getGeometry();
			if ("material".equals(geometry.getUserData("type"))) {
				Spatial material = geometry.getParent().getParent();
				if (Util.notEmpty(material.getUserData("owner"))) {
					break;
				}
				if (material.getNumControls() != 0) {
					material.setLocalTranslation(Vector3f.ZERO);
					control1 = (ItemControl) material.getControl(0);
					control1.setEnabled(false);
					pieceLoc1.attachChild(material);
					stateManager.getState(MouseEventState.class).setVipModel(material);
				}
			}
		}

		origin = start2.getWorldTranslation();
		goal = end2.getWorldTranslation();
		limit = FastMath.abs(goal.distance(origin));

		Ray ray2 = new Ray(origin, goal.add(origin.negate()).normalize());

		CollisionResults results2 = new CollisionResults();
		rootNode.collideWith(ray2, results2);
		for (CollisionResult result : results2) {
			if (result.getDistance() > limit) {
				continue;
			}
			Geometry geometry = result.getGeometry();
			if ("material".equals(geometry.getUserData("type"))) {
				Spatial material = geometry.getParent().getParent();
				if (Util.notEmpty(material.getUserData("owner"))) {
					break;
				}
				if (material.getNumControls() != 0) {
					material.setLocalTranslation(Vector3f.ZERO);
					control2 = (ItemControl) material.getControl(0);
					control2.setEnabled(false);
					pieceLoc2.attachChild(material);
					stateManager.getState(MouseEventState.class).setVipModel(material);
				}
			}
		}
		if (!swing) {
			return;
		}
		float rad = tpf * speed;
		Vector3f per = dir.mult(rad);
		if (b) {
			total += rad;
			for (final Spatial spatial : driveds) {
				spatial.rotate(per.x, per.y, per.z);
			}
			if (total >= degree) {
				for (final Spatial spatial : driveds) {
					spatial.rotate(0, degree - total, 0);
				}
				total = degree;
				swing = false;
				// 此处用于通知检测被当前托盘中工件阻塞的工件开始移动，并不真正设置模型
				if (control1 != null) {
					control1.onMoving();
				}
				if (control2 != null) {
					control2.onMoving();
				}
			}
		} else {
			total -= rad;
			for (final Spatial spatial : driveds) {
				spatial.rotate(-per.x, -per.y, -per.z);
			}
			if (total <= 0) {
				for (final Spatial spatial : driveds) {
					spatial.rotate(0, -total, 0);
				}
				total = 0;
				swing = false;
				// 此处用于通知检测被当前托盘中工件阻塞的工件开始移动，并不真正设置模型
				if (control1 != null) {
					control1.onMoving();
				}
				if (control2 != null) {
					control2.onMoving();
				}
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

	public void swing() {
		swing = true;
		b = true;
	}

	public void unswing() {
		swing = true;
		b = false;
	}

}
