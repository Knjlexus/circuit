package com.cas.circuit.event;

import com.cas.robot.common.MouseEvent;
import com.cas.robot.common.MouseEventAdapter;

/**
 * 传感器鼠标监听事件
 * @功能 TransducersAdapter.java
 * @作者 CWJ
 * @创建日期 2016年6月3日
 * @修改人 CWJ
 */
public class TransducersAdapter extends MouseEventAdapter {

	private TransducersHandler handler;

	public TransducersAdapter(TransducersHandler handler) {
		this.handler = handler;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.robot.common.MouseEventAdapter#mouseEntered(com.cas.robot.common.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
		// TODO 显示这个元器件的名字
		handler.setInfoVisible(true);
//		System.out.println("// TODO 显示这个元器件的名字");
	}

	/*
	 * (non-Javadoc)
	 * @see com.cas.robot.common.MouseEventAdapter#mouseExited(com.cas.robot.common.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
		// TODO 取消显示这个元器件的名字
		handler.setInfoVisible(false);
//		System.out.println("// TODO 取消显示这个元器件的名字");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		handler.showWirePanel();
	}
}
