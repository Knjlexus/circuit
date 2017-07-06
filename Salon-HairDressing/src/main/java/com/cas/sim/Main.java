package com.cas.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication {

	public Main() {

	}

	@Override
	public void simpleInitApp() {

		/** A white, directional light source */
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection((new Vector3f(0, -0.5f, 0f)).normalizeLocal());
		sun.setColor(ColorRGBA.White);
		rootNode.addLight(sun);

//		assetManager.registerLocator("F:\\JME_SDKPROJ_HOME\\SalonGame\\assets", FileLocator.class);
//		assetManager.registerLocator("F:\\JME_SDK\\BasicGame\\assets", FileLocator.class);

//		固定相机视角
//		1、禁用飞行相机
		flyCam.setEnabled(false);
		flyCam.setMoveSpeed(15);

		cam.setLocation(new Vector3f(-0.43731692f, 25.972036f, 1.9423999f));
		cam.setRotation(new Quaternion(-5.109252E-4f, 0.7527802f, -0.6582715f, -5.884529E-4f));

//		设置相机视锥
		cam.setFrustumPerspective(45, (float) cam.getWidth() / cam.getHeight(), 0.01f, 100f);

//		SceneCameraState cameraState = new SceneCameraState();
//		stateManager.attach(cameraState);
		MouseEventState mouseEventState = new MouseEventState();
		stateManager.attach(mouseEventState);

		MyAppState state = new MyAppState();
		stateManager.attach(state);

	}

	public static void main(String[] args) {
		Main app = new Main();
		app.setShowSettings(false);
		app.setDisplayFps(false);
		app.setDisplayStatView(false);

		AppSettings s = new AppSettings(true);
		s.setGammaCorrection(true);
		s.setWidth(1200);
		s.setHeight(720);

		app.setSettings(s);
		app.start();
	}

}
