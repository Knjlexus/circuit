package com.cas.circuit.event;

import com.cas.robot.common.IMouseEventHandler;
import com.cas.robot.common.MouseEvent;
import com.cas.robot.common.MouseEventAdapter;

/**
 * 元器件端子鼠标点击事件处理类
 * @功能 TerminalEventAdapter.java
 * @作者 CWJ
 * @创建日期 2016年5月17日
 * @修改人 CWJ
 */
public class LinkTargetEventAdapter extends MouseEventAdapter {
	private LinkTargetHandler handler;

	/**
	 * @param handler
	 */
	public LinkTargetEventAdapter(IMouseEventHandler handler) {
		super(handler);
		this.handler = (LinkTargetHandler) handler;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.robot.common.MouseEventAdapter#mouseClicked(com.cas.robot.common.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		handler.select();
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.robot.common.MouseEventAdapter#mouseEntered(com.cas.robot.common.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
		handler.setInfoVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.robot.common.MouseEventAdapter#mouseExited(com.cas.robot.common.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
		handler.setInfoVisible(false);
	}

}
