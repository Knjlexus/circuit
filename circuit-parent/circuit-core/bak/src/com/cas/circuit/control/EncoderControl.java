package com.cas.circuit.control;

import com.cas.circuit.logic.Encoder;
import com.cas.robot.common.util.UDUtil;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class EncoderControl extends AbstractControl {
	private Encoder encoder;
	private Spatial detection;
	private float[] angles = { -1, -1, -1 };

	boolean rotating = false;

	public EncoderControl(Encoder encoder, Spatial detection) {
		this.encoder = encoder;
		this.detection = detection;
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (detection == null) {
			return;
		}

//		上一次转到的角度
		float old = angles[0];
//		计算获得当前转过的角度
		detection.getLocalRotation().toAngles(angles);
		angles[0] = angles[0] + FastMath.PI;

		if (old == -1) {
			return;
		}

//		当前转到的角度
		float current = angles[0];
//		System.out.println("EncoderControl.controlUpdate()" + (current - old));
		if (current == old) {
			setRotating(false);
//			没有转动，继续等着
//			System.err.println("没有转动，编码器继续等着");
		} else if (!rotating) {
//			一旦物体转动了， 就反馈脉冲
//			if (old > current && old + current > 360) {
//				old -= 360;
//			} else if (old < current && old + current > 360) {
//
//			}
//			说明：程序中一旦模型转动了，速度是确定的，一致的
//			这里计算发脉冲的速度，并不做真正的发脉冲的操作
//			这里和PLC测约定好，PLC测根据发脉冲的速度和时间记录脉冲数量
//			获得物体当前的旋转方向（1：正转，-1：反转）
			int dir = UDUtil.getInteger(detection, "dir");
//			本次转过的角度
			float minus = current - old;
			if (minus < 0 && dir > 0) {
//				如果正转时出现上一个转过的角度大于当前，则说明新的一圈开始
				minus = current + FastMath.TWO_PI - old;
			} else if (minus > 0 && dir < 0) {
//				如果反转时出现上一个转过的角度小于当前，则说明新的一圈开始
				minus = current - old - FastMath.TWO_PI;
			}
//			System.out.println("EncoderControl.controlUpdate()" + minus);
//			告诉编码器， 1毫秒内转过多少个角度
			encoder.setDegPerMillis(Math.abs(minus * FastMath.RAD_TO_DEG / tpf / 1000));

//			模型的正转方向是现实中编码器的反方向
			encoder.setDir(-dir);
			angles[0] = -1;
			setRotating(true);

			System.out.println("编码器判断【" + detection + "】转过" + (minus / tpf / 1000) + "当前为" + (dir > 0 ? "正转" : "反转"));
		}
	}

	public void setRotating(boolean rotating) {
		if (this.rotating && !rotating) {
//			停止转动
			encoder.onTargetStop();
			this.rotating = false;
		} else if (!this.rotating && rotating) {
//			开始转动
			encoder.onTargetStart();
			this.rotating = true;
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (!enabled) {
			setRotating(false);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

}
