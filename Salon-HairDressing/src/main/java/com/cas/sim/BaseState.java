package com.cas.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.Trigger;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

public abstract class BaseState extends AbstractAppState {

	protected SimpleApplication app;

	protected InputManager inputManager;

	protected AssetManager assetManager;

	protected AppStateManager stateManager;

	protected Camera cam;

	protected Node rootNode;

	protected AppSettings settings;

	protected MouseEventState mouseEventState;

//	记录state中用到的映射名，退出清除
	private Map<String, List<Trigger>> inputMappings = new HashMap<>();
	private List<InputListener> inputListeners = new ArrayList<>();

	private int frame;

	@Override
	public void stateAttached(AppStateManager stateManager) {
		super.stateAttached(stateManager);
	}

	@Override
	public final void initialize(AppStateManager stateManager, Application app) {
		this.app = (SimpleApplication) app;
		inputManager = app.getInputManager();
		assetManager = app.getAssetManager();
		cam = app.getCamera();
		this.stateManager = this.app.getStateManager();
		rootNode = this.app.getRootNode();
		settings = app.getContext().getSettings();
		mouseEventState = stateManager.getState(MouseEventState.class);

		initializeLocal();
		super.initialize(stateManager, app);
	}

	protected abstract void initializeLocal();

	@Override
	public void update(float tpf) {
		if (frame == 2) {
			try {
				onceUpdate();
			} catch (Exception e) {
//				LOG.error("onceUpdate :" + e.getMessage());
				e.printStackTrace();
			}
		}
		if (frame < 3) {
			frame++;
		}
		super.update(tpf);
	}

	/**
	 * state第一次刷新
	 */
	protected void onceUpdate() {
	};

	@Override
	public void stateDetached(AppStateManager stateManager) {
//		LOG.info(getClass() + ".stateDetached()");
//		AnnotationProcessor.unprocess(this);
		super.stateDetached(stateManager);
	}

	@Override
	public void cleanup() {
//		LOG.info(getClass() + ".cleanup(清除本类中添加的事件映射)");
		inputMappings.entrySet().stream().forEach(entry -> {
			List<Trigger> triggers = entry.getValue();
			if (inputManager.hasMapping(entry.getKey())) {
				triggers.forEach(t -> inputManager.deleteTrigger(entry.getKey(), t));
			}
		});
		inputMappings.clear();

//		LOG.info(getClass() + ".cleanup(清除本类中添加的事件监听)");
		inputListeners.stream().forEach(l -> {
			inputManager.removeListener(l);
		});
		inputListeners.clear();

		super.cleanup();
	}

	/**
	 * @param string
	 * @param mouseButtonTrigger
	 */
	protected void addMapping(String mappingName, Trigger... triggers) {
		if (triggers == null) {
//			LOG.warn("必须给映射名称配上触发器");
			return;
		}
		inputManager.addMapping(mappingName, triggers);
		List<Trigger> triggerList = inputMappings.get(mappingName);
		if (triggerList == null) {
			triggerList = new ArrayList<>();
			inputMappings.put(mappingName, triggerList);
		}
		for (Trigger trigger : triggers) {
			triggerList.add(trigger);
		}
	}

	protected void addListener(InputListener listener, String... mappingNames) {
		if (mappingNames == null) {
//			LOG.warn("必须给监听对象配上映射名称");
			return;
		}
		inputManager.addListener(listener, mappingNames);
		inputListeners.add(listener);
	}
}
