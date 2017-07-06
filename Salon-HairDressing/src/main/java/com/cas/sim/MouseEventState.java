package com.cas.sim;

import java.util.List;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.util.SafeArrayList;

public class MouseEventState extends BaseState {
	private static final String TO_MOUSE_VISIBLE = "ToMouseVisible";

	private static final int CANNOT_FOUND = -1;
	private static final String MOUSE_EVENTS = "MouseEvent";
	/**
	 * 能看成一次点击事件(click)的最大划过距离
	 */
	protected static final float CLICK_MAX_AXIS_DISTANCE = 0.003f;
	protected static final String MOUSE_BUTTON = "MOUSE_CLICK";
	protected static final String MOUSE_AXIS_LEFT = "MOUSE_AXIS_LEFT";
	protected static final String MOUSE_AXIS_RIGHT = "MOUSE_AXIS_RIGHT";
	protected static final String MOUSE_AXIS_UP = "MOUSE_AXIS_UP";
	protected static final String MOUSE_AXIS_DOWN = "MOUSE_AXIS_DOWN";

//	private static final String[] MapNames = { MOUSE_BUTTON, MOUSE_AXIS_LEFT, MOUSE_AXIS_RIGHT, MOUSE_AXIS_UP, MOUSE_AXIS_DOWN };

	private SafeArrayList<Spatial> candidates = new SafeArrayList<Spatial>(Spatial.class);

	protected CollisionResults results = new CollisionResults();
	protected MouseEvent e = null;
	protected Spatial picked = null;
	protected float axis_distance = 0f;

	protected boolean mouseButtonPressed; // 标记鼠标状态_鼠标是否按下
//	private ArrayList<Spatial> ignorModelList = new ArrayList<Spatial>(); // 要忽略的模型

	protected Spatial pressed;

	@Override
	protected void initializeLocal() {
		addMapping(MOUSE_BUTTON, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		addListener(new ActionListener() {
			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				if (!MOUSE_BUTTON.equals(name)) {
					return;
				}

				mouseButtonPressed = isPressed;
				pickModel();
				if (isPressed) {
					axis_distance = 0;
					if (MOUSE_BUTTON.equals(name)) {
//						记录当前鼠标按下时，所选中模型
						pressed = picked;
						if (pressed != null) {
//							触发这个模型的鼠标按下的事件
							e.setAction(MouseAction.MOUSE_PRESSED);
							notifyEventTrigged(e);
						}
					}
				} else {
//					鼠标松开
					Spatial oldPressed = pressed;
					pressed = null;
//					如果两次选择的不一样，则选择无效
					if (picked == null || oldPressed != picked) {
						return;
					}
					if (Math.abs(axis_distance) < CLICK_MAX_AXIS_DISTANCE) {
						e.setAction(MouseAction.MOUSE_CLICK);
						notifyEventTrigged(e);
					}
					e.setAction(MouseAction.MOUSE_RELEASED);
					notifyEventTrigged(e);
				}
			}
		}, MOUSE_BUTTON);

		addMapping(MOUSE_AXIS_LEFT, new MouseAxisTrigger(MouseInput.AXIS_X, true));
		addMapping(MOUSE_AXIS_RIGHT, new MouseAxisTrigger(MouseInput.AXIS_X, false));
		addMapping(MOUSE_AXIS_UP, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		addMapping(MOUSE_AXIS_DOWN, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
		addListener(new AnalogListener() {
			@Override
			public void onAnalog(String name, float value, float tpf) {
				if (mouseButtonPressed) {
					if (MOUSE_AXIS_LEFT.equals(name)) {
						axis_distance += value;
					} else if (MOUSE_AXIS_RIGHT.equals(name)) {
						axis_distance -= value;
					} else if (MOUSE_AXIS_UP.equals(name)) {
						axis_distance += value;
					} else if (MOUSE_AXIS_DOWN.equals(name)) {
						axis_distance -= value;
					}
				}
			}
		}, MOUSE_AXIS_LEFT, MOUSE_AXIS_RIGHT, MOUSE_AXIS_UP, MOUSE_AXIS_DOWN);
	}

	@Override
	public void cleanup() {
		for (Spatial spatial : candidates) {
			spatial.setUserData(MOUSE_EVENTS, null);
		}
		candidates.clear();

		super.cleanup();
	}

	public void addCandidate(Spatial spatial, MouseEventListener listener) {
		synchronized (candidates) {
			if (spatial != null) {
//				获取节点的鼠鼠标监听
				List<MouseEventListener> listeners = spatial.getUserData(MOUSE_EVENTS);
				if (listeners == null) {
					listeners = new SavableArrayList<MouseEventListener>(MouseEventListener.class);
					spatial.setUserData(MOUSE_EVENTS, listeners);
				}
				if (!listeners.contains(listener)) {
					listeners.add(listener);
				}

				if (candidates.indexOf(spatial) == CANNOT_FOUND) {
					Spatial candidate = null;
//					默认添加到集合末尾
					int insertIndex = candidates.size();
					for (int i = 0; i < candidates.size(); i++) {
						candidate = candidates.get(i);
//						查找集合中是否有node的父节点,如果有,则插入第一个父节点的前面
						if (candidate instanceof Node) {
							if (((Node) candidate).hasChild(spatial)) {
								insertIndex = i;
								break;
							}
						}
					}
					candidates.add(insertIndex, spatial);
				}
			} else {
//				LOG.warn("模型为null, 不能加监听");
			}
		}
	}

	public void removeCandidate(Spatial node) {
		synchronized (candidates) {
			if (candidates.contains(node)) {
				node.setUserData(MOUSE_EVENTS, null);
				candidates.remove(node);
			}
		}
	}

	public void removeListener(Spatial node, MouseEventListener eventListener) {
		synchronized (candidates) {
			if (candidates.contains(node)) {
				List<MouseEventListener> listeners = node.getUserData(MOUSE_EVENTS);
				if (!listeners.contains(eventListener)) {
//					LOG.warn("这个模型没有加过指定的鼠标监听");
				} else {
					if (listeners.remove(eventListener)) {
//						LOG.warn("删除指定的鼠标监听 成功！");
						mouseButtonPressed = false;
					}
				}

				if (listeners.size() == 0) {
					removeCandidate(node);
				}
			} else {
//				LOG.warn("这个模型没有加过鼠标监听");
			}
		}
	}

	// 选择几何体
	protected void pickModel() {
		Spatial tmpPicked = null;
		Vector3f contactPoint = null;
		Vector3f contactNormal = null;

		rootNode.collideWith(getRay(), results);
		int resultsize = results.size();
		if (resultsize > 0) {
			Geometry geometry = null;
			CollisionResult collision = null;
			for (int i = 0; i < resultsize; i++) {
//				从近到远依次判断被选中的对象
				collision = results.getCollision(i);
				geometry = collision.getGeometry();
//				验证被选中模型的有效性
				if (!valiedate(geometry)) {
					continue;
				}
				SafeArrayList<Spatial> candicates = getCandidates();
				for (Spatial node : candicates) {
					if (node == geometry) {
						tmpPicked = node;
					} else if (node instanceof Node) {
						if (((Node) node).hasChild(geometry)) {
							tmpPicked = node;
						}
					}
					if (tmpPicked != null) {
						contactPoint = collision.getContactPoint(); // 绝对坐标
						contactNormal = collision.getContactNormal(); // 相对坐标
						break;
					}
				}
				if (tmpPicked != null) {
					break;
				}
			}
		}
//		记录本次选中的模型

		Spatial oldValue = this.picked;
		Spatial newValue = tmpPicked;
		this.picked = tmpPicked;
		e = new MouseEvent(picked, contactPoint, contactNormal);

		if (oldValue == null && newValue != null) {
//			Enter
			e.setAction(MouseAction.MOUSE_ENTERED);
			notifyEventTrigged(e);
		} else if (oldValue != null && newValue == null) {
//			Exit
			e.setAction(MouseAction.MOUSE_EXITED);
			e.setSpatial(oldValue);
			notifyEventTrigged(e);
		}
	}

	protected boolean valiedate(Geometry geometry) {
//		模型被剔除，视为不可选中
//		if (geometry.getCullHint() == CullHint.Always) {
//			return false;
//		}
////		模型虽然显示，但已经加入黑名单中，视为不可选中。
//		if (this.ignorModelList.contains(geometry)) {
//			return false;
//		}
//		漏网之鱼，对鼠标不可见，但是不在黑名单中的，也视为不可选中。
		Boolean toMouseVisible = geometry.getUserData(TO_MOUSE_VISIBLE);
//		有这个属性，并且值是false，表示对鼠标不可见
		if (toMouseVisible != null && !toMouseVisible.booleanValue()) {
//			模型对鼠标不可见
			return false;
		}
//		
		return true;
	}

	protected Ray getRay() {
		Vector2f click2d = null;
		if (inputManager.isCursorVisible()) {
			click2d = inputManager.getCursorPosition();
			System.err.println(click2d);
		} else {
			System.err.println("从屏幕中央获取");
			click2d = new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2);
		}
//		System.out.println("MouseEventState.getRay()" + click2d);
		Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtract(click3d).normalizeLocal();

		results.clear();
		Ray ray = new Ray(click3d, dir);
		return ray;
	}

	protected void notifyEventTrigged(MouseEvent event) {
		if (!isEnabled() || event == null) {
			return;
		}

		MouseAction action = event.getAction();
		if (action == null) {
			return;
		}

		synchronized (this) {
			try {
				Spatial sp = event.getSpatial();
				if (!getCandidates().contains(sp)) {
					return;
				}
				List<MouseEventListener> listeners = sp.getUserData(MOUSE_EVENTS);
				for (MouseEventListener listener : listeners) {
					// 事件被消耗了,就没有此事件,就不会在有响应了
					if (event.isSuspend()) {
						break;
					}
					switch (action) {
					case MOUSE_ENTERED:
						listener.mouseEntered(event);
						break;
					case MOUSE_EXITED:
						listener.mouseExited(event);
						break;
					case MOUSE_PRESSED:
						listener.mousePressed(event);
						break;
					case MOUSE_RELEASED:
						listener.mouseReleased(event);
						break;
					case MOUSE_CLICK:
						listener.mouseClicked(event);
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public SafeArrayList<Spatial> getCandidates() {
		synchronized (candidates) {
			return candidates;
		}
	}

	public void setToMouseTransprent(Spatial... ignor) {
		if (ignor == null) {
			return;
		}
		for (Spatial spatial : ignor) {
			transparent(spatial);
		}

//		if (Util.isEmpty(ignor)) {
//			return;
//		}
//		for (int i = 0; i < ignor.length; i++) {
//			if (this.ignorModelList.contains(ignor[i])) {
//				continue;
//			}
//			this.ignorModelList.add(ignor[i]);
//		}
	}

	private void transparent(Spatial spatial) {
		if (spatial instanceof Node) {
			((Node) spatial).getChildren().forEach(child -> transparent(child));
		} else {
			spatial.setUserData(TO_MOUSE_VISIBLE, false);
		}
	}

}
