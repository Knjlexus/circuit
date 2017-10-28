package com.cas.circuit.event;

import com.cas.circuit.ConnectionHandler;
import com.cas.circuit.ILinker;
import com.cas.circuit.vo.Cable;
import com.cas.robot.common.IMouseEventHandler;

/**
 * 接线头处理事件实现类
 * @功能 LineTermEventHandler.java
 * @作者 CWJ
 * @创建日期 2016年5月17日
 * @修改人 CWJ
 */
public class LinkerEventHandler implements IMouseEventHandler {

	private ILinker linker;
	private ConnectionHandler handler;
	private boolean modifiable;

	public LinkerEventHandler(ILinker linker, ConnectionHandler handler, boolean modifiable) {
		this.linker = linker;
		this.handler = handler;
		this.modifiable = modifiable;
	}

	/**
	 * 选中接线头
	 */
	public void select() {
		if (linker.isBothBinded()) {
			handler.selectLinker(linker);
			handler.setCurrSelectedModifiable(modifiable);
		}
	}

	public void modifyNum() {
		if (linker instanceof Cable) {
			return;
		}
		handler.getUI().setNumDialogueVisible(true);
		handler.setModifyLinker(linker);
		handler.selectLinker(linker);
	}

}
