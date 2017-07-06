package com.cas.sim;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Texture;

public class MyAppState extends AbstractAppState {
	private SimpleApplication app;

	private Node rootNode;

	private ScheduledExecutorService executor;

	private Object currentNumSpatial;

	private ScheduledFuture<?> countTask;

	private int numberSpatialIndex;

	private ScheduledFuture<?> shuffleTask;

	protected boolean flipEnable = false;

	protected Spatial currentCard;

	protected boolean okEnable;

	protected boolean stampEnable;

	protected int cardIndex;

	public MyAppState() {
		executor = Executors.newScheduledThreadPool(2);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		this.app = (SimpleApplication) app;

		this.rootNode = this.app.getRootNode();

		MouseEventState mouseEventState = app.getStateManager().getState(MouseEventState.class);

//		1、加载桌面模型
		Node desk = (Node) app.getAssetManager().loadModel("Models/zm.j3o");
		rootNode.attachChild(desk);

//		2、卡牌
		Node sp = (Node) app.getAssetManager().loadModel("Models/kapai.j3o");

		Node[] card = new Node[7];
//		[-2.82292, 0.26, 1.8574524]

		for (int i = 0; i < card.length; i++) {
			Node c = (Node) sp.clone();
			c.setUserData("Number", i);
			rootNode.attachChild(c);

//			换成对应的贴图
			Geometry frontGeo = (Geometry) c.getChild("front");
			Texture texture = app.getAssetManager().loadTexture("ka-bei-" + (i + 1) + ".jpg");
			frontGeo.getMaterial().setTexture("DiffuseMap", texture);
			card[i] = c;
		}

//		3、大图
		Geometry picGeo = (Geometry) desk.getChild("Picture");
		picGeo.addControl(new PictureControl());
		picGeo.scale(0);

//		设置卡牌默认位置,正面朝上，依次摆放
		for (int i = 0; i < card.length; i++) {
			card[i].rotate(0, 0, FastMath.PI);
			card[i].setLocalTranslation(4.5f * i - 14f, 2f, -9f);
			card[i].setUserData("DefLoc", card[i].getLocalTranslation().clone());
		}

		Spatial takeButton = desk.getChild("ButtonTake");
//		隐藏模型
		takeButton.setCullHint(CullHint.Always);

//		3、queding
		Spatial okModel = desk.getChild("Confirm");
		okModel.setCullHint(CullHint.Always);
		mouseEventState.addCandidate(okModel, new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);

//				当前正在查看的卡牌
				if (currentCard == null) {
					return;
				}

				okModel.setCullHint(CullHint.Always);
				picGeo.scale(0);

//				点击确定后，贴到顶部
				currentCard.addControl(new StampControl(new Consumer<Void>() {
					@Override
					public void accept(Void t) {
//						铁道顶部后，允许再抽一张牌
						flipEnable = true;

						if (cardIndex == card.length) {
//							结束
							executor.schedule(() -> {
								for (int i = 0; i < card.length; i++) {

//									card[i].setLocalTranslation();
									card[i].addControl(new EndControl(i));
								}
							}, 1, TimeUnit.SECONDS);
						}
					}
				}));

				currentCard = null;
			}
		});

		mouseEventState.addCandidate(takeButton, new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!flipEnable) {
					return;
				}
				flipEnable = false;

				if (cardIndex == card.length) {
					return;
				}

//				抽一张牌
				currentCard = card[cardIndex];
				currentCard.addControl(new CardFlipControl(new Vector3f(-4, 18, .8f), new Consumer<Void>() {
					@Override
					public void accept(Void t) {
//						翻拍结束后 允许贴到顶部
						stampEnable = true;

						okModel.setCullHint(CullHint.Dynamic);
					}
				}));
//				牌对应的大图
				Texture text = app.getAssetManager().loadTexture((cardIndex + 1) + ".jpg");
				picGeo.getMaterial().setTexture("DiffuseMap", text);
				picGeo.getControl(PictureControl.class).setEnabled(true);
				picGeo.getControl(PictureControl.class).setEnabled(true);

//				Spatial sp = currentCard;
//				mouseEventState.addCandidate(sp, new MouseEventAdapter() {
//					@Override
//					public void mouseClicked(MouseEvent e) {
//						if (!stampEnable) {
//							return;
//						}
//						stampEnable = false;
//						sp.addControl(new StampControl(new Consumer<Void>() {
//							@Override
//							public void accept(Void t) {
//								stampEnable = true;
//							}
//						}));
////						Texture text = app.getAssetManager().loadTexture((cardIndex + 1) + ".jpg");
////						picGeo.getMaterial().setTexture("DiffuseMap", text);
////						picGeo.getControl(PictureControl.class).setEnabled(true);
//					}
//				});

				cardIndex++;
			}
		});

//		4s洗牌(从当前位置移动到左下角。并叠摞)
		shuffleTask = executor.schedule(() -> {
			for (int i = 0; i < card.length; i++) {
				card[i].addControl(new CardShuffleControl(new Vector3f(-0.3f * i, 0.2f * i, 0).add(-12.531378f, 1.4854153f, 4.962077f), new Consumer<Void>() {
					@Override
					public void accept(Void t) {
						flipEnable = true;
					}
				}));
			}
		}, 2, TimeUnit.SECONDS);
		
		
//		executor.schedule(() -> {
//			for (int i = 0; i < card.length; i++) {
//
////				card[i].setLocalTranslation();
//				card[i].addControl(new EndControl(i));
//			}
//		}, 1, TimeUnit.SECONDS);

//		4s抽牌(从当前位置移动到左下角。并叠摞)
//		shuffleTask = executor.schedule(() -> {
////			for (int i = 0; i < card.length; i++) {
//			card[6].addControl(new CardFlipControl(new Vector3f(0, 6.0f, -0.2061972f)));
////			}
//		}, 10, TimeUnit.SECONDS);
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
