package com.cas.circuit.event;

import com.cas.circuit.CfgConst;
import com.cas.circuit.vo.ControlIO;
import com.cas.circuit.vo.ElecComp;
import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.MouseEvent;
import com.cas.robot.common.MouseEventAdapter;
import com.cas.robot.common.consts.JmeConst;
import com.cas.robot.common.ui.ITip;
import com.cas.robot.common.util.JmeUtil;
import com.cas.robot.common.util.Pool;
import com.jme3.math.ColorRGBA;

/**
 * @author DING 2015年7月24日 下午2:37:46
 */
public class ButtonIOMouseEvent extends MouseEventAdapter {

	private ControlIO controlIO;

	private ElecComp eleccomp;

	public static final String MOTION_ROTATE = "rotate";
	public static final String MOTION_MOVE = "move";

	public ButtonIOMouseEvent(ControlIO button) {
		super();
		this.controlIO = button;

		eleccomp = button.getElecComp().getRef();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
		Dispatcher.getIns().getTip().showTip(ITip.NORMAL, controlIO.getPO().getName());
		JmeUtil.setSpatialHighLight(e.getSpatial(), JmeConst.YELLOW);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
		Dispatcher.getIns().getTip().showTip(ITip.NORMAL, null);
		JmeUtil.setSpatialHighLight(e.getSpatial(), ColorRGBA.BlackNoAlpha);
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		super.mousePressed(e);
		if (!controlIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_INPUT)) {
			return;
		}

		if (!ControlIO.INTERACT_PRESS.equals(controlIO.getPO().getInteract())) {
			return;
		}

		Pool.getCircuitPool().submit(new Runnable() {
			@Override
			public void run() {
				try {
					eleccomp.getCompLogic().onButtonAction(controlIO, true);
					controlIO.switchStateChanged(null, e);
					controlIO.playMotion(e);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);

		if (!controlIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_INPUT)) {
			return;
		}
		if (!ControlIO.INTERACT_PRESS.equals(controlIO.getPO().getInteract())) {
			return;
		}
//		System.out.println("ButtonIOMouseEvent.mouseReleased()");
		Pool.getCircuitPool().submit(new Runnable() {
			@Override
			public void run() {
				try {
					eleccomp.getCompLogic().onButtonAction(controlIO, false);
					controlIO.switchStateChanged(null, e);
					controlIO.playMotion(e);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		super.mouseReleased(e);
		if (!controlIO.getPO().getType().contains(CfgConst.SWITCH_CTRL_INPUT)) {
			return;
		}
		if (!ControlIO.INTERACT_CLICK.equals(controlIO.getPO().getInteract())) {
			return;
		}

		Pool.getCircuitPool().submit(new Runnable() {
			@Override
			public void run() {
				try {
					controlIO.switchStateChanged(null, e);
					controlIO.playMotion(e);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}
}
