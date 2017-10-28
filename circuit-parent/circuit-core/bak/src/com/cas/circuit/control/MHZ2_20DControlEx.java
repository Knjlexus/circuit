package com.cas.circuit.control;

import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.control.BaseAppControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class MHZ2_20DControlEx extends BaseAppControl {

	// 左边手指模型
	private Spatial left;
	// 右边手指模型
	private Spatial right;
	// 工件加入元器件的节点
	private Node pieceLoc;

	private Spatial start;
	private Spatial end;
	private boolean clamp;
	private boolean b;

	private float speed;

	private float total;

	private float length;

	private Spatial magnet;

	public MHZ2_20DControlEx(float length, float speed) {
		super(Dispatcher.getIns().getMainApp());
		this.length = length;
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
		Vector3f origin = start.getWorldTranslation();
		Vector3f goal = end.getWorldTranslation();

		Ray ray = new Ray(origin, goal.add(origin.negate()).normalize());

		CollisionResults results = new CollisionResults();
		rootNode.collideWith(ray, results);
		for (CollisionResult result : results) {
			Geometry geometry = result.getGeometry();
			if ("workpiece".equals(geometry.getUserData("type"))) {
				// 获得工件模型
				Spatial workpiece = geometry.getParent().getParent();
				if ("0".equals(geometry.getParent().getUserData("combined"))) {
					Spatial combined = workpiece.getParent();
					if (combined.getNumControls() != 0) {
						combined.setLocalTranslation(Vector3f.ZERO);

						ItemControl control = (ItemControl) combined.getControl(0);
						control.stopPiece(combined);
//						control.setEnabled(false);
						pieceLoc.attachChild(combined);
					}
				}
				break;
			} else if ("material".equals(geometry.getUserData("type"))) {
				// 获得工件模型
				Spatial material = geometry.getParent().getParent();
				if ("1".equals(geometry.getParent().getUserData("combined"))) {
					Spatial combined = material.getParent();
					if (combined.getNumControls() != 0) {
						combined.setLocalTranslation(Vector3f.ZERO);

						ItemControl control = (ItemControl) combined.getControl(0);
						control.stopPiece(combined);
//						control.setEnabled(false);
						pieceLoc.attachChild(combined);
					}
				}
				break;
			}
		}
		if (!clamp) {
			return;
		}
		float per = tpf * speed;
		if (b) {
			left.move(per, 0, 0);
			right.move(-per, 0, 0);
			total += per;
			if (total >= length) {
				left.move(length - total, 0, 0);
				right.move(total - length, 0, 0);
				total = length;
				clamp = false;
				
				magnet.setLocalTranslation(0, -0.015f, 0);
			}
		} else {
			left.move(-per, 0, 0);
			right.move(per, 0, 0);
			total -= per;
			if (total <= 0) {
				left.move(-total, 0, 0);
				right.move(total, 0, 0);
				total = 0;

				magnet.setLocalTranslation(0, 0.015f, 0);
				
				clamp = false;
				if (pieceLoc.getChildren().size() == 0) {
					return;
				}
				final Spatial combined = pieceLoc.getChild(0);
				if (combined != null && combined.getNumControls() != 0) {
					combined.setLocalTranslation(combined.getWorldTranslation());

					Dispatcher.getIns().getMainApp().enqueue(new Runnable() {

						@Override
						public void run() {
							try {
								rootNode.attachChild(combined);

								AbstractControl control = (AbstractControl) combined.getControl(0);
								control.setEnabled(true);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
						}
					});
				}
			}
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

	public void clamp() {
		clamp = true;
		b = true;
	}

	public void unclamp() {
		clamp = true;
		b = false;
	}
}
