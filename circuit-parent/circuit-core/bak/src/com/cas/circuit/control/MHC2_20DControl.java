package com.cas.circuit.control;

import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.control.BaseAppControl;
import com.cas.util.Util;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class MHC2_20DControl extends BaseAppControl {

	// 左边手指模型
	private Spatial left;
	// 右边手指模型
	private Spatial right;
	// 工件加入元器件的节点
	private Node pieceLoc;

	private Spatial start;
	private Spatial end;

	private boolean clamp;

	private float speed;

	private float total;

	private float degree;

	private Spatial magnet;

	public MHC2_20DControl(float degree, float speed) {
		super(Dispatcher.getIns().getMainApp());
		this.degree = degree;
		this.speed = speed;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		// 左边手指模型
		left = ((Node) spatial).getChild("finger_left");
		// 右边手指模型
		right = ((Node) spatial).getChild("finger_right");
		// 碰撞感知点
		start = ((Node) spatial).getChild("start");
		end = ((Node) spatial).getChild("end");
		// 磁块
		magnet = ((Node) spatial).getChild("magnet");
		// 工件摆放节点
		pieceLoc = (Node) ((Node) spatial).getChild("pieceLoc");
	}

	@Override
	protected void controlUpdate(float tpf) {
		float per = tpf * speed;
		if (clamp) {
			left.rotate(0, per, 0);
			right.rotate(0, per, 0);
			total += per;
			if (total >= degree) {
				left.rotate(0, degree - total, 0);
				right.rotate(0, degree - total, 0);
				total = degree;

				magnet.setLocalTranslation(0.003f, 0, 0.02f);

				Vector3f origin = start.getWorldTranslation();
				Vector3f goal = end.getWorldTranslation();

//				float limit = goal.distance(origin);
				Ray ray = new Ray(origin, goal.add(origin.negate()).normalize());
//				ray.setLimit(limit);

				CollisionResults results = new CollisionResults();
				rootNode.collideWith(ray, results);
				for (CollisionResult result : results) {
					Geometry geometry = result.getGeometry();
					if (geometry.getUserData("workpiece") != null) {
						// 获得工件模型
						final Spatial workpiece = geometry.getParent().getParent();
						if (workpiece.getNumControls() != 0) {
							workpiece.setLocalTranslation(Vector3f.ZERO);

							ItemControl control = (ItemControl) workpiece.getControl(0);
							control.setEnabled(false);
							pieceLoc.attachChild(workpiece);
						} else if (Util.notEmpty(geometry.getParent().getUserData("combined"))) {
							Spatial combined = workpiece.getParent();
							if (combined.getNumControls() != 0) {
								combined.setLocalTranslation(Vector3f.ZERO);

								ItemControl control = (ItemControl) combined.getControl(0);
								control.setEnabled(false);
								pieceLoc.attachChild(combined);
							}
						}
						break;
					}
				}
				enabled = false;
			}
		} else {
			left.rotate(0, -per, 0);
			right.rotate(0, -per, 0);
			total -= per;
			if (total <= 0) {
				left.rotate(0, -total, 0);
				right.rotate(0, -total, 0);
				total = 0;

				enabled = false;

				magnet.setLocalTranslation(0.003f, 0, -0.01f);

				final Spatial workpiece = pieceLoc.getChild("workpiece");
				if (workpiece != null) {
					workpiece.setLocalTranslation(workpiece.getWorldTranslation());

					ItemControl control = (ItemControl) workpiece.getControl(0);
					control.setEnabled(true);

					rootNode.attachChild(workpiece);
				}
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

	public void clamp() {
		enabled = true;
		clamp = true;
	}

	public void unclamp() {
		enabled = true;
		clamp = false;
	}

}
