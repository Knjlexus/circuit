package com.cas.sim;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Spatial;

public class MyAppState extends AbstractAppState {
	private SimpleApplication app;

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		this.app = (SimpleApplication) app;

//		1、加载桌面模型
		
		Spatial desk = app.getAssetManager().loadModel("Models/zm.j3o");
		this.app.getRootNode().attachChild(desk );
//		2、卡牌
		

//		

	}
}
