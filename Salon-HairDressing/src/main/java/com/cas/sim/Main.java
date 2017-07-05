package com.cas.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;

public class Main extends SimpleApplication {

	public Main() {

	}

	@Override
	public void simpleInitApp() {

		/** A white, directional light source */
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
		sun.setColor(ColorRGBA.White);
		rootNode.addLight(sun);

//		assetManager.registerLocator("F:\\JME_SDKPROJ_HOME\\SalonGame\\assets", FileLocator.class);
		assetManager.registerLocator("F:\\JME_SDK\\BasicGame\\assets", FileLocator.class);

//		固定相机视角
//		1、禁用飞行相机
		flyCam.setEnabled(false);
//		flyCam.setMoveSpeed(15);

		cam.setLocation(new Vector3f(1.5851243f, 10.278198f, 0.9547823f));
		cam.setRotation(new Quaternion(-0.0058893226f, 0.7535492f, -0.6573304f, -0.0067553786f));
//		设置相机视锥
		cam.setFrustumPerspective(45, (float) cam.getWidth() / cam.getHeight(), 0.01f, 100f);

//		SceneCameraState cameraState = new SceneCameraState();
//		stateManager.attach(cameraState);
		MyAppState state = new MyAppState();
		stateManager.attach(state);
	}

	public static void main(String[] args) {
		new Main().start();
	}

}
