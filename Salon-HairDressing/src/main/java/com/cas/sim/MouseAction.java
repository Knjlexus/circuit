package com.cas.sim;

public enum MouseAction {
	// 点击
	MOUSE_CLICK,
	// 按下
	MOUSE_PRESSED,
	// 松开
	MOUSE_RELEASED,
//	// 移动
//	MOUSE_MOVE,
//	// 拖拽
//	MOUSE_DRAG,
	// 滚轮
	MOUSE_WHEEL,
	// 鼠标离开
	MOUSE_EXITED,
	// 鼠标进入
	MOUSE_ENTERED;

	private MouseAction() {
	}

	public static MouseAction parseMouseAction(String actionName) {
		MouseAction[] actions = MouseAction.values();
		for (MouseAction mouseAction : actions) {
			if (mouseAction.name().equals(actionName)) {
				return mouseAction;
			}
		}
		return null;
	}

}
