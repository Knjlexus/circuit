package com.cas.sim;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class MyAppState extends AbstractAppState {
	private SimpleApplication app;

	private Node rootNode;

	private ScheduledExecutorService executor;

	private Object currentNumSpatial;

	private ScheduledFuture<?> countTask;

	private int numberSpatialIndex;

	private ScheduledFuture<?> shuffleTask;

	public MyAppState() {
		executor = Executors.newScheduledThreadPool(2);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		this.app = (SimpleApplication) app;

		this.rootNode = this.app.getRootNode();
//		1、加载桌面模型

		Node desk = (Node) app.getAssetManager().loadModel("Models/zm.j3o");
		rootNode.attachChild(desk);
//		2、卡牌
		Spatial sp = app.getAssetManager().loadModel("Models/kapai.j3o");

		Spatial[] card = new Spatial[7];
//		[-2.82292, 0.26, 1.8574524]

		for (int i = 0; i < card.length; i++) {
			Spatial c = (Spatial) sp.clone();
			c.setUserData("Number", i);
			rootNode.attachChild(c);
			card[i] = c;
		}

//		设置卡牌默认位置,正面朝上，依次摆放
		for (int i = 0; i < card.length; i++) {
			card[i].rotate(0, 0, FastMath.PI);

			card[i].setLocalTranslation(1.5f * i - 3f, 0.74f, -3f);
		}

////		加载模型数字
//		String[] numSpatialKe = new String[] { "", "" };
//		Spatial[] number = new Spatial[numSpatialKe.length];
//		for (int i = 0; i < numSpatialKe.length; i++) {
//			number[i] = app.getAssetManager().loadModel(numSpatialKe[i]);
//		}
////		提示用户三秒后开始洗牌
//		countTask = executor.scheduleAtFixedRate(() -> {
//			if (numberSpatialIndex < number.length) {
//				number[numberSpatialIndex].addControl(new NumberControl());
//			} else {
//				countTask.cancel(false);
//			}
//		}, 1, 1, TimeUnit.SECONDS);
//		
		Spatial takeButton = desk.getChild("ButtonTake");

//		4s洗牌(从当前位置移动到左下角。并叠摞)
//		shuffleTask = executor.schedule(() -> {
//			for (int i = 0; i < card.length; i++) {
//				card[i].addControl(new CardShuffleControl(new Vector3f(takeButton.getLocalTranslation().add(0.04f * i, 0, 0))));
//			}
//		}, 1, TimeUnit.SECONDS);

//		4s洗牌(从当前位置移动到左下角。并叠摞)
		shuffleTask = executor.schedule(() -> {
//			for (int i = 0; i < card.length; i++) {
				card[6].addControl(new CardFlipControl(new Vector3f(0, 6.0f, -0.2061972f)));
//			}
		}, 2, TimeUnit.SECONDS);
	}

	@Override
	public void cleanup() {
		super.cleanup();

		executor.shutdown();
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
	}
}
