package com.cas.circuit.event;

import com.cas.robot.common.MouseEvent;
import com.cas.robot.common.MouseEventAdapter;

/**
 * 接线头鼠标点击事件处理类
 * @功能 LineTermEventAdapter.java
 * @作者 CWJ
 * @创建日期 2016年5月17日
 * @修改人 CWJ
 */
public class LinkerEventAdapter extends MouseEventAdapter {

	private LinkerEventHandler handler;

	public LinkerEventAdapter(LinkerEventHandler handler) {
		this.handler = handler;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		handler.select();
	}

	@Override
	public void mouseRightClicked(MouseEvent e) {
		super.mouseRightClicked(e);
		handler.modifyNum();
	}

}
