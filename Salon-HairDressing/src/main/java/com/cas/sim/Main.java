package com.cas.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Main extends SimpleApplication {

	public Main() {

	}

	@Override
	public void simpleInitApp() {

		assetManager.registerLocator("F:\\JME_SDKPROJ_HOME\\SalonGame\\assets", FileLocator.class);

//		固定相机视角
//		1、禁用飞行相机
		flyCam.setEnabled(false);

		cam.setLocation(new Vector3f(0.002445285f, 0.024510982f, 0.043151572f));
		cam.setRotation(new Quaternion(-0.015038728f, 0.9445305f, -0.32532018f, -0.043676484f));
//		设置相机视锥
		cam.setFrustumPerspective(45, (float) cam.getWidth() / cam.getHeight(), 0.01f, 100f);

		rootNode.addLight(new DirectionalLight());

		SceneCameraState cameraState = new SceneCameraState();
		stateManager.attach(cameraState);
		MyAppState state = new MyAppState();
		stateManager.attach(state);
	}

	public static void main(String[] args) {
		new Main().start();
	}

}
