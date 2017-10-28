package com.cas.circuit.event;

import com.cas.circuit.ConnectionHandler;
import com.cas.circuit.ILinkTarget;
import com.cas.circuit.msg.LinkerMsg;
import com.cas.circuit.state.MultimeterState_MF47;
import com.cas.circuit.vo.Terminal;
import com.cas.network.util.ClientMgr;
import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.IMouseEventHandler;
import com.cas.robot.common.ui.ITip;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * 元器件端子事件处理实现类
 * @功能 TerminalEventHandler.java
 * @作者 CWJ
 * @创建日期 2016年5月17日
 * @修改人 CWJ
 */
public class LinkTargetHandler implements IMouseEventHandler {

	private ILinkTarget target;
	private ConnectionHandler handler;
	private AppStateManager stateManager;

	/**
	 * @param maintenanceState
	 * @param target
	 * @param batchNode
	 */
	public LinkTargetHandler(ILinkTarget target, ConnectionHandler handler, AppStateManager stateManager) {
		this.target = target;
		this.handler = handler;
		this.stateManager = stateManager;
	}

	public void select() {
		MultimeterState_MF47 multimeterState = stateManager.getState(MultimeterState_MF47.class);
		if (multimeterState.isEnabled() && target instanceof Terminal) {
			multimeterState.putPenToTerminal((Terminal) target, target.getModel());
			return;
		}
		if (!handler.checkConnect(target)) {
			return;
		}
		LinkerMsg reqMsg = handler.offlineConnect(target);
		// 如果当前为多人联机模式则向服务器发送连线请求
		if (!handler.isAlone()) {
			reqMsg.setType(LinkerMsg.ADD_LINKER);
			ClientMgr.send(reqMsg);
		}
		if (!(target instanceof Terminal)) {
			return;
		}
//		螺丝拧上拧下
		Node parent = (Node) target.getModel();
		final Spatial luosi = parent.getChild("screw");// findCylinder(parent);
		if (luosi == null) {
			throw new RuntimeException("螺丝的模型结构有问题， 没有指出螺杆节点");
		}
		if (luosi.getNumControls() > 0) {
			AbstractControl control = (AbstractControl) luosi.getControl(0);
			control.setEnabled(true);
		}
	}

	public void setInfoVisible(boolean visible) {
		if (visible) {
			Dispatcher.getIns().getTip().showTip(ITip.NORMAL, target.toString());
		} else {
			Dispatcher.getIns().getTip().showTip(ITip.NORMAL, null);
		}
	}
}
