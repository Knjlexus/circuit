package com.cas.circuit.ui.controller;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.cas.circuit.ConnectionHandler;
import com.cas.circuit.ConnectionUtil;
import com.cas.circuit.state.MultimeterState_MF47;
import com.cas.circuit.vo.Cable;
import com.cas.circuit.vo.Wire;
import com.cas.gas.vo.Pipe;
import com.cas.robot.common.BaseController;
import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.ICleanableControl;
import com.cas.robot.common.ui.ITip;
import com.cas.util.ImageUtil;
import com.cas.util.Util;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.RenderImageJme;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.NiftyIdCreator;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.dynamic.CustomControlCreator;
import de.lessvoid.nifty.controls.dynamic.attributes.ControlEffectAttributes;
import de.lessvoid.nifty.controls.dynamic.attributes.ControlEffectOnHoverAttributes;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;

public class ConnectionController extends BaseController {

	private int currentPipeIndex = 0;
	private int currentWireIndex = 0;
	private int currentCableIndex = 0;
	private String[] pipeImageModes = { "subImage:0,128,32,32", "subImage:32,128,32,32" };
	private String[] wireImageModes = { "subImage:64,96,32,32", "subImage:96,96,32,32", "subImage:0,96,32,32", "subImage:32,96,32,32" };
	private String[] pipeLabels = { "橘红色", "蓝色" };
	private String[] wireLabels = { "红色", "绿色", "黑色", "黄色" };
	private String[] cableLabels = null;
	private ColorRGBA[] pipeColors = { new ColorRGBA(0.93f, 0.43f, 0.18f, 1), new ColorRGBA(0.22f, 0.61f, 0.77f, 1) };
	private ColorRGBA[] wireColors = { new ColorRGBA(0.62f, 0.20f, 0.23f, 1), new ColorRGBA(0.11f, 0.43f, 0.36f, 1), new ColorRGBA(0, 0, 0, 1), new ColorRGBA(0.82f, 0.78f, 0.25f, 1) };
	private String BGIMAGEMODE_MOUSEOVER = "sprite-resize:54,56,0,20,20,14,20,20,20,14,16,20,20,14,20";
	private String BGIMAGEMODE_MOUSELEAVE = "resize:20,34,20,20,20,34,20,16,20,34,20,20";

	private boolean mouseVisible;

	private ConnectionHandler connectionHandler;

	private boolean noMult;
	private boolean noPipe;
	private boolean noWire;
	private boolean noCable;

	Timer timer;

	@Override
	protected void bindLocal(Properties parameter, Attributes controlDefinitionAttributes) {
		super.bindLocal(parameter, controlDefinitionAttributes);

		if (Util.notEmpty(parameter.getProperty("noMult"))) {
			noMult = Boolean.valueOf(parameter.getProperty("noMult"));
		}
		if (Util.notEmpty(parameter.getProperty("noPipe"))) {
			noPipe = Boolean.valueOf(parameter.getProperty("noPipe"));
		}
		if (Util.notEmpty(parameter.getProperty("noWire"))) {
			noWire = Boolean.valueOf(parameter.getProperty("noWire"));
		}
		if (Util.notEmpty(parameter.getProperty("noCable"))) {
			noCable = Boolean.valueOf(parameter.getProperty("noCable"));
		}

		if (noMult) {
			Element multimeterPanel = findById("multimeterPanel");
			multimeterPanel.markForRemoval();
		}
		if (noPipe) {
			Element pipePanel = findById("pipePanel");
			pipePanel.markForRemoval();
		} else {
			Element pipeColorsContent = findById("pipeColorsContent");
			updateImageMode("currentPipe#colorImage", pipeImageModes[0]);
			for (int i = 0; i < pipeLabels.length; i++) {
				creatWireAndPipeItem(i, pipeImageModes, pipeLabels, "changePipeColor", pipeColorsContent);
			}
		}
		if (noWire) {
			Element wirePanel = findById("wirePanel");
			wirePanel.markForRemoval();
		} else {
			Element wireColorsContent = findById("wireColorsContent");
			updateImageMode("currentWire#colorImage", wireImageModes[0]);
			for (int j = 0; j < wireLabels.length; j++) {
				creatWireAndPipeItem(j, wireImageModes, wireLabels, "changeWireColor", wireColorsContent);
			}
		}
		if (noCable) {
			Element xianlanPanel = findById("xianlanPanel");
			xianlanPanel.markForRemoval();
		}

		addCleanningControlOnEnd(new ICleanableControl() {
			@Override
			public void onClean() {
				if (timer != null) {
					timer.cancel();
				}
			}

			@Override
			public String getName() {
				if (timer != null) {
					return timer.toString();
				}
				return "null Timer";
			}

		});
	}

	public void setConnectionHandler(ConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	public void customKeyEvent(NiftyInputEvent inputEvent) {
		if (inputEvent == NiftyInputEvent.MoveCursorLeft) {
			selectPipe();
		} else if (inputEvent == NiftyInputEvent.MoveCursorRight) {
			selectWire();
		} else if (inputEvent == NiftyInputEvent.MoveCursorUp) {
			selectCable();
		} else if (inputEvent == NiftyInputEvent.PrevInputElement) {
			showWireNum();
		} else if (inputEvent == NiftyInputEvent.Paste) {
			multimeterClick();
		}
	}

	public NiftyInputEvent customInputConvert(KeyboardInputEvent inputEvent) {
		if (inputEvent.isKeyDown()) {
			if (inputEvent.getKey() == KeyboardInputEvent.KEY_1 && !noPipe) {
				return NiftyInputEvent.MoveCursorLeft; // 气管
			} else if (inputEvent.getKey() == KeyboardInputEvent.KEY_2 && !noWire) {
				return NiftyInputEvent.MoveCursorRight; // 导线
			} else if (inputEvent.getKey() == KeyboardInputEvent.KEY_3 && !noCable) {
				return NiftyInputEvent.MoveCursorUp; // 线缆
			} else if (inputEvent.getKey() == KeyboardInputEvent.KEY_RETURN) {
				return NiftyInputEvent.PrevInputElement;
			} else if (inputEvent.getKey() == KeyboardInputEvent.KEY_V && !noMult) {
				return NiftyInputEvent.Paste;
			}
		}
		return null;
	}

	/**
	 * 
	 */
	private void selectPipe() {
		if (!connectionHandler.checkChange()) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.CONNECTING_CANNOT_CHANGE);
			return;
		}
		setContentVisible("pipeColorsContent", "true");
		setContentVisible("wireColorsContent", "false");

		if (connectionHandler.getCurrLinker() instanceof Pipe) {
//		当前是气管
			currentPipeIndex = currentPipeIndex + 1 > pipeColors.length - 1 ? 0 : currentPipeIndex + 1;
		}
		List<Element> pipeEles = findById("pipeColorsContent").getElements();
		for (int i = 0; i < pipeEles.size(); i++) {
			Element pipeEle = pipeEles.get(i);
			if (i == currentPipeIndex) {
				pipeEle.startEffect(EffectEventId.onHover);
			} else {
				pipeEle.stopEffect(EffectEventId.onHover);
			}
		}
		changePipeColor(String.valueOf(currentPipeIndex));
//		2秒后隐藏
		selectHideTimer("pipeColorsContent");
	}

	/**
	 * 
	 */
	private void selectWire() {
		if (!connectionHandler.checkChange()) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.CONNECTING_CANNOT_CHANGE);
			return;
		}
		setContentVisible("pipeColorsContent", "false");
		setContentVisible("wireColorsContent", "true");

		if (connectionHandler.getCurrLinker() instanceof Wire) {
//			当前是导线
			currentWireIndex = currentWireIndex + 1 > wireColors.length - 1 ? 0 : currentWireIndex + 1;
		}
		List<Element> colorEles = findById("wireColorsContent").getElements();
		for (int i = 0; i < colorEles.size(); i++) {
			Element colorEle = colorEles.get(i);
			if (i == currentWireIndex) {
				colorEle.startEffect(EffectEventId.onHover);
			} else {
				colorEle.stopEffect(EffectEventId.onHover);
			}
		}
		changeWireColor(String.valueOf(currentWireIndex));
//		2秒后隐藏
		selectHideTimer("wireColorsContent");
	}

	/**
	 * 
	 */
	public void selectCable() {
		setContentVisible("pipeColorsContent", "false");
		setContentVisible("wireColorsContent", "false");
		if (cableLabels == null) {
			if (!connectionHandler.checkChange()) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.CONNECTING_CANNOT_CHANGE);
				return;
			}
			connectionHandler.setCurrLinker(ConnectionUtil.CONNECT_CABLE, null);
			return;
		}
		if (connectionHandler.getCurrLinker() instanceof Cable) {
			currentCableIndex = currentCableIndex + 1 > cableLabels.length - 1 ? 0 : currentCableIndex + 1;
			List<Element> colorEles = findById("xianlanColorsContent").getElements();
			Cable cable = (Cable) connectionHandler.getCurrLinker();
			for (int i = 0; i < colorEles.size(); i++) {
				Element colorEle = colorEles.get(i);
				if (i == currentCableIndex) {
					colorEle.startEffect(EffectEventId.onActive);
					cable.setNowConnectingWire(cableLabels[currentCableIndex]);
				} else {
					colorEle.stopEffect(EffectEventId.onActive);
				}
			}
		}
	}

	private void selectHideTimer(final String hideEleId) {
//		2秒后隐藏
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				findById(hideEleId).hide();
				timer.cancel();
			}
		}, 2000);
	}

	public void linkTransduscers(String indexStr) {
		if (Util.isEmpty(indexStr)) {
			return;
		}
		// 创建传感器接线
		Cable cable = (Cable) connectionHandler.getCurrLinker();
		cable.setNowConnectingWire(indexStr);
		int index = Arrays.asList(cableLabels).indexOf(indexStr);
		showBorder(index);
	}

	private void creatWireAndPipeItem(final int index, final String[] imageModes, final String[] colorStrs, final String clickMethodName, Element content) {
		new CustomControlCreator(NiftyIdCreator.generate(), "wireAndPipeItem") {
			{
				set("mode", imageModes[index]);
				set("label", colorStrs[index]);
				setInteractOnClick(clickMethodName + "(" + index + ")");
				if (index == 0) {
					ControlEffectAttributes ctrlEffectAttributes = new ControlEffectAttributes();
					ctrlEffectAttributes.setName("border");
					ctrlEffectAttributes.setAttribute("border", "0,0,0,1");
					ctrlEffectAttributes.setAttribute("color", "#333333");
					addEffectsOnActive(ctrlEffectAttributes);
				}
				ControlEffectOnHoverAttributes ctrlEffectAttributes = new ControlEffectOnHoverAttributes();
				ctrlEffectAttributes.setName("border");
				ctrlEffectAttributes.setAttribute("border", "2");
				ctrlEffectAttributes.setAttribute("color", "#009900");
				ctrlEffectAttributes.setAttribute("inset", "3");
				addEffectsOnHover(ctrlEffectAttributes);
			}
		}.create(nifty, screen, content);
	}

	/**
	 * 初始化线缆面板
	 */
	public void initXianLanPanel(List<Wire> wires, boolean visible) {
		setContentVisible("xianlanColorsContent", String.valueOf(visible));
		if (!visible) {
			cableLabels = null;
			return;
		}
		Element xianlanColorsContent = findById("xianlanColorsContent");
		if (xianlanColorsContent == null || wires == null) {
			return;
		}
//		线缆导线排序
		Comparator<Wire> comparator = new Comparator<Wire>() {
			@Override
			public int compare(Wire wire1, Wire wire2) {
				return wire1.getPO().getStitch1().compareTo(wire2.getPO().getStitch1());
			}
		};
		Collections.sort(wires, comparator);

		List<Element> eles = xianlanColorsContent.getElements();
		for (int i = 0; i < eles.size(); i++) {
			removeXianLanItem(i);
		}
		List<String> xianLanStrs = new ArrayList<String>();
		for (int i = 0; i < wires.size(); i++) {
			String xianLanStr = wires.get(i).getPO().getMark();
			xianLanStrs.add(xianLanStr);
			Element ele = createXianLanItem(xianLanStr, wires.get(i).getPO().getColor(), i);
			if (i != 0) {
				ele.stopEffect(EffectEventId.onActive);
			}
		}
		refreshxianlanColorsContent();
		currentCableIndex = 0;
		cableLabels = new String[xianLanStrs.size()];
		xianLanStrs.toArray(cableLabels);
	}

	private Element createXianLanItem(final String itemText, String colorParam, final int index) {
		Element parent = findById("xianlanColorsContent");
		Element ele = new CustomControlCreator(NiftyIdCreator.generate(), "wireAndPipeItem") {
			{
				set("label", itemText);
				setInteractOnClick("linkTransduscers(" + itemText + ")");
				if (index == 0) {
					ControlEffectAttributes ctrlEffectAttributes = new ControlEffectAttributes();
					ctrlEffectAttributes.setName("border");
					ctrlEffectAttributes.setAttribute("border", "0,0,0,1");
					ctrlEffectAttributes.setAttribute("color", "#333333");
					addEffectsOnShow(ctrlEffectAttributes);
				}
				ControlEffectOnHoverAttributes ctrlEffectAttributes = new ControlEffectOnHoverAttributes();
				ctrlEffectAttributes.setName("border");
				ctrlEffectAttributes.setAttribute("border", "2");
				ctrlEffectAttributes.setAttribute("color", "#009900");
				ctrlEffectAttributes.setAttribute("inset", "3");
				addEffectsOnHover(ctrlEffectAttributes);

				ControlEffectAttributes effect = new ControlEffectAttributes();
				effect.setName("border");
				effect.setAttribute("border", "2");
				effect.setAttribute("color", "#009900");
				effect.setAttribute("inset", "3");
				addEffectsOnActive(effect);
			}
		}.create(nifty, screen, parent);
		ImageRenderer imageRender = ele.findElementByName("#colorImage").getRenderer(ImageRenderer.class);
		InputStream resource = getClass().getClassLoader().getResourceAsStream("com/cas/circuit/ui/resorces/wire32.png");
		BufferedImage img = ImageUtil.getColorImage("0,0,1,1", colorParam, resource);
		Image image = load(img, true);
		Texture2D texture2D = new Texture2D(image);
		RenderImage createImage = new RenderImageJme(texture2D);
		imageRender.setImage(new NiftyImage(nifty.getRenderEngine(), createImage));
		return ele;
	}

	private com.jme3.texture.Image load(BufferedImage img, boolean flipY) {
		int width = img.getWidth();
		int height = img.getHeight();
		ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);
		// no alpha
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int ny = y;
				if (flipY) {
					ny = height - y - 1;
				}
				int rgb = img.getRGB(x, ny);
				byte a = (byte) ((rgb & 0xFF000000) >> 24);
				byte r = (byte) ((rgb & 0x00FF0000) >> 16);
				byte g = (byte) ((rgb & 0x0000FF00) >> 8);
				byte b = (byte) ((rgb & 0x000000FF));
				data.put(r).put(g).put(b).put(a);
			}
		}
		data.flip();
		return new com.jme3.texture.Image(com.jme3.texture.Image.Format.RGBA8, width, height, data, ColorSpace.sRGB);
	}

	private void removeXianLanItem(int index) {
		final Element parent = findById("xianlanColorsContent");
		Element item = parent.getElements().get(index);
		item.markForRemoval(new EndNotify() {
			@Override
			public void perform() {
				refreshxianlanColorsContent();
				currentCableIndex = currentCableIndex == parent.getElements().size() ? 0 : currentCableIndex;
			}
		});
	}

	private void refreshxianlanColorsContent() {
		Element xianlanColorsContent = findById("xianlanColorsContent");
		int size = xianlanColorsContent.getElements().size();
		xianlanColorsContent.setConstraintWidth(SizeValue.px(size * 50));
		Element parent = xianlanColorsContent.getParent();
		parent.setConstraintWidth(SizeValue.px(size * 50));
		parent.getParent().layoutElements();
	}

	/**
	 * 显示线缆线框
	 * @param index
	 */
	public void showBorder(int index) {
		Element xianlanColorsContent = findById("xianlanColorsContent");
		List<Element> eles = xianlanColorsContent.getElements();
		for (int i = 0; i < eles.size(); i++) {
			if (i == index) {
				eles.get(i).startEffect(EffectEventId.onActive);
			} else {
				eles.get(i).stopEffect(EffectEventId.onActive);
			}
		}
	}

	/**
	 * 鼠标hover到气管或导线时，设置内容界面的显示或隐藏
	 * @param ContentId
	 * @param visbleStr
	 */
	public void setContentVisible(String ContentId, String visbleStr) {
		Element content = findById(ContentId);
		boolean visible = Boolean.parseBoolean(visbleStr);
		if (content == null || content.isVisible() == visible) {
			return;
		}
		content.setVisible(visible);
	}

	/**
	 * 当导线或气管内容显示时 改变当前导线或气管ELement的背景图片参数
	 * @param currentEleId
	 * @param ContentId
	 */
	public void onContentVisibleChanged(String currentEleId, String ContentId) {
		Element content = findById(ContentId);
		if (content == null) {
			return;
		}
		if (content.isVisible()) {
			updateImageMode(currentEleId, BGIMAGEMODE_MOUSEOVER);
		} else {
			updateImageMode(currentEleId, BGIMAGEMODE_MOUSELEAVE);
		}
	}

	public void changePipeColor(String indexStr) {
		if (indexStr == null || !Util.isNumeric(indexStr)) {
			return;
		}
		int index = Integer.parseInt(indexStr);
		currentPipeIndex = index;
		updateImageMode("currentPipe#colorImage", pipeImageModes[currentPipeIndex]);
		connectionHandler.setCurrLinker(ConnectionUtil.CONNECT_PIPE, pipeColors[currentPipeIndex]);
	}

	public void changeWireColor(String indexStr) {
		if (indexStr == null || !Util.isNumeric(indexStr)) {
			return;
		}
		int index = Integer.parseInt(indexStr);
		currentWireIndex = index;
		updateImageMode("currentWire#colorImage", wireImageModes[currentWireIndex]);
		connectionHandler.setCurrLinker(ConnectionUtil.CONNECT_WIRE, wireColors[currentWireIndex]);
	}

	public void setNumDialogueVisible(boolean visible) {
		Element numDialogue = nifty.findPopupByName("numDialoguePopup");
		if (numDialogue == null) {
			numDialogue = nifty.createPopupWithId("numDialoguePopup", "numDialoguePopup");
		}
		if (visible) {
//			SimpleApplication app = Dispatcher.getIns().getMainApp();
//			InputManager inputManager = app.getInputManager();
//			mouseVisible = inputManager.isCursorVisible();
			mouseVisible = nifty.isMouseVisible();
			TextField wireNumTextfield = numDialogue.findElementByName("wireNum").getNiftyControl(TextField.class);
			wireNumTextfield.setText("");
			nifty.showPopup(screen, "numDialoguePopup", null);
//			if (!inputManager.isCursorVisible()) {
			if (!nifty.isMouseVisible()) {
				nifty.setMouseVisible(true);
			}
		} else {
			nifty.closePopup("numDialoguePopup");
			if (!mouseVisible) {
				nifty.setMouseVisible(false);
			}
		}
	}

	public void showWireNum() {
		Element numDialogue = nifty.findPopupByName("numDialoguePopup");
		if (numDialogue == null || !numDialogue.equals(nifty.getTopMostPopup())) {
			return;
		}
		TextField wireNumTextfield = numDialogue.findElementByName("wireNum").getNiftyControl(TextField.class);
		String wireNum = wireNumTextfield.getRealText();
		connectionHandler.modifyWireNum(wireNum);
		setNumDialogueVisible(false);
	}

//	private void setMouseVisible(boolean visible) {
////		鼠标显示或隐藏
//		SimpleApplication app = Dispatcher.getIns().getMainApp();
//		InputManager inputManager = app.getInputManager();
//		app.getFlyByCamera().setEnabled(!visible);
//		app.getStateManager().getState(AimPointState.class).setVisible(!visible);
//		inputManager.setCursorVisible(visible);
//	}

	public void multimeterClick() {
		SimpleApplication app = Dispatcher.getIns().getMainApp();
		MultimeterState_MF47 multimeterState = app.getStateManager().getState(MultimeterState_MF47.class);
		if (multimeterState != null) {
			multimeterState.setEnabled(!multimeterState.isEnabled());
		}
	}

	public void setViewAreaVisible(boolean visible) {
		Element border = findById("border");
		border.setVisible(visible);
	}
}
