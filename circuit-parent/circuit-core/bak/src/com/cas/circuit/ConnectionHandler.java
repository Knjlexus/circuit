package com.cas.circuit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.cas.circuit.consts.ColorConsts;
import com.cas.circuit.event.LinkerEventAdapter;
import com.cas.circuit.event.LinkerEventHandler;
import com.cas.circuit.event.TransducersAdapter;
import com.cas.circuit.event.TransducersHandler;
import com.cas.circuit.msg.LinkerMsg;
import com.cas.circuit.ui.controller.ConnectionController;
import com.cas.circuit.vo.Cable;
import com.cas.circuit.vo.ElecComp;
import com.cas.circuit.vo.ElecCompDef;
import com.cas.circuit.vo.Format;
import com.cas.circuit.vo.Jack;
import com.cas.circuit.vo.Terminal;
import com.cas.circuit.vo.Wire;
import com.cas.ftp.FTPUtils;
import com.cas.gas.vo.GasPort;
import com.cas.gas.vo.Pipe;
import com.cas.network.util.ClientMgr;
import com.cas.robot.common.Dispatcher;
import com.cas.robot.common.IMouseEventListener;
import com.cas.robot.common.MouseEventState;
import com.cas.robot.common.ui.ITip;
import com.cas.robot.common.util.JmeUtil;
import com.cas.shader.FilterUtil;
import com.cas.util.FileUtil;
import com.cas.util.Util;
import com.jme3.app.Application;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import jme3tools.savegame.SaveGame;

public class ConnectionHandler {
	protected static final Logger log = Logger.getLogger(ConnectionHandler.class);

	private ColorRGBA color;

	private ILinker currLinker;

	private ILinker currSaveLinker;

	private ILinker selected;

	private ILinker modifyLinker;

	private Application app;
//	private BorderFilter borderFilter;
//	private LineRenderFilter lineRenderFilter;

	private ViewPort port;
	private Node viewRoot;

	private Node parent;

	private LinkerData linkerData = new LinkerData();
	private LinkerData standardData;

	private String savePath;
	private String tempPath;

	// 当前连线模式是否为单机模式
	private boolean alone;
	private boolean modifiable;

//	记录state中创建的鼠标监听事件，退出清除
	private List<Spatial> eventCandidates = new ArrayList<Spatial>();

	private Map<Object, ElecComp> eleCompMap = new HashMap<Object, ElecComp>();
	private Map<Object, Cable> cableMap = new HashMap<Object, Cable>();
	/**
	 * 联机接线
	 */
	private HashMap<Integer, ILinker> onlineAndSaves = new HashMap<Integer, ILinker>();

	private MouseEventState eventState;
	private ConnectionController ui;

	public ConnectionHandler(Application app, String savePath, String tempPath, boolean alone) {
		this.app = app;
		this.savePath = savePath;
		this.tempPath = tempPath;
		this.alone = alone;

		FilterUtil.initLineRenderFilter();

		eventState = app.getStateManager().getState(MouseEventState.class);
	}

	public void setStandard(String standardPath, String serverName) {
		FTPUtils ftpUtils = FTPUtils.getInstance();
		// 从服务器上下载接线存档
		boolean exists = ftpUtils.checkFile(serverName, standardPath, "standard.wire");
		if (!exists) {
			log.warn("未在服务器上找到" + standardPath + "standard.wire");
		}
		// 下载存档到本地
		InputStream in = ftpUtils.downloadFile(serverName, standardPath, "standard.wire");
		try {
			standardData = (LinkerData) SaveGame.loadGame(in, app.getAssetManager());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public void initViewPort() {
		Camera camClone = app.getCamera().clone();
		camClone.setLocation(new Vector3f(0, 0, 10));
		camClone.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
		camClone.setViewPort(0.75f, 1, 0.0f, 0.25f);

		port = app.getRenderManager().createPostView("LineViewPort", camClone);
		port.setClearFlags(false, true, true);
//		边框
//		if (fpp.getFilter(BorderFilter.class) == null) {
//			borderFilter = new BorderFilter(1.0f, JmeUtil.getJmeColor("#706f6b"), new Vector2f(0.15f, 0.25f), new Vector2f(20f, 20f));
//			borderFilter.setEnabled(false);
//			fpp.addFilter(borderFilter);
//		}
//		FilterUtil.initBorderFilter(1.0f, JmeUtil.getJmeColor("#706f6b"), new Vector2f(0.15f, 0.25f), new Vector2f(20f, 20f));
		viewRoot = new Node();
		viewRoot.addLight(new DirectionalLight(Vector3f.UNIT_XYZ));
		viewRoot.addLight(new DirectionalLight(Vector3f.UNIT_XYZ.negate()));

		app.enqueue(new Runnable() {
			@Override
			public void run() {
				try {
					port.attachScene(viewRoot);
					viewRoot.updateGeometricState();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	public void refreshViewPort(ILinker wireOrPipe) {
		Spatial viewMdl = null;
		if (wireOrPipe instanceof Wire) {
			viewMdl = ConnectionUtil.getWireTerm();
			viewMdl.scale(500);
			viewMdl.move(4.8f, -4, -5);
		} else if (wireOrPipe instanceof Pipe) {
			viewMdl = ConnectionUtil.getPipeTerm();
			viewMdl.scale(300);
			viewMdl.move(4.2f, -3.6f, -5);
		} else if (wireOrPipe instanceof Cable) {
			viewMdl = ConnectionUtil.getJackTerm(null);
			viewMdl.scale(100);
			viewMdl.rotate(FastMath.HALF_PI, FastMath.HALF_PI, 0);
			viewMdl.move(4, -3f, -3);
		}
		viewRoot.detachAllChildren();
		if (wireOrPipe == null) {
//			隐藏边框
//			FilterUtil.getBorderFilter().setEnabled(false);
			if (ui != null) {
				ui.setViewAreaVisible(false);
			}
		} else {
			JmeUtil.changeColor(((Node) viewMdl).getChild("model"), wireOrPipe.getColor());
			viewRoot.attachChild(viewMdl);
//			显示边框
//			FilterUtil.getBorderFilter().setEnabled(true);
			if (ui != null) {
				ui.setViewAreaVisible(true);
			}
		}
		viewRoot.updateGeometricState();
	}

	/**
	 * 验证当前是否可以进行换线
	 * @return
	 */
	public boolean checkChange() {
		if (currLinker == null || currLinker.isBothBinded() || (currLinker.getLinkTarget1() == null && currLinker.getLinkTarget2() == null)) {
			return true;
		}
		return false;
	}

	/**
	 * 绑定端点
	 * @param termOrPort 点击接线的端点信息对象，或者气口信息对象
	 */
	private void bindWireOrPipe(ILinkTarget termOrPort, Spatial mdl) {
		setHighLightLinker(selected, false);
		if (currLinker instanceof Cable && termOrPort instanceof Terminal) {
			Cable cable = ((Cable) currLinker);
			// 如果当前绑定为线缆上的导线
			cable.bindTerm(termOrPort, mdl);
			// 接完线 显示号码管对话框
			ui.setNumDialogueVisible(true);
			modifyLinker = currLinker;
			List<Wire> wires = new ArrayList<Wire>(cable.getMark_wire().values());
//			线缆导线排序
			for (Wire w : cable.getBindWires()) {
				wires.remove(w);
			}
			Comparator<Wire> comparator = new Comparator<Wire>() {
				@Override
				public int compare(Wire wire1, Wire wire2) {
					return wire1.getPO().getStitch1().compareTo(wire2.getPO().getStitch1());
				}
			};
			Collections.sort(wires, comparator);
			if (wires.size() == 0) {
				cable.setNowConnectingWire(null);
			} else {
				cable.setNowConnectingWire(wires.get(0).getPO().getMark());
			}
		} else {
			// 其他普通情况绑定
			currLinker.bind(termOrPort);
			if (currLinker.isBothBinded() && currLinker instanceof Wire) {
				// 接完线 显示号码管对话框
				ui.setNumDialogueVisible(true);
				modifyLinker = currLinker;
			}
		}
		currLinker.getModels().put(mdl, termOrPort);
		// 显示或隐藏与鼠标连接的直线
		setLineVisible(true);
		if (!currLinker.isBothBinded()) {
			return;
		}
		setLineVisible(false);

		ui.initXianLanPanel(null, false);

		// 取消接线完成的元器件高亮
		if (currLinker instanceof Cable && ((Cable) currLinker).isElecComp()) {
			Map<Spatial, ILinkTarget> models = currLinker.getModels();
			for (Spatial spatial : models.keySet()) {
//				JmeUtil.setMaterialEffect(Dispatcher.getIns().getMainApp(), HightLightType.GLOW, spatial, ColorConsts.TRANSDUCERS, false);
				JmeUtil.setSpatialHighLight(spatial, ColorRGBA.BlackNoAlpha);
			}
		}

		if (termOrPort instanceof Terminal) {
			if (currLinker instanceof Cable) {
				Cable cable = new Cable();
				currLinker = cable;
			} else {
				Wire wire = new Wire(1, color);
				currLinker = wire;
			}
		} else if (termOrPort instanceof GasPort) {
			Pipe pipe = new Pipe(1, color);
			currLinker = pipe;
		} else if (termOrPort instanceof Jack) {
			Cable cable = new Cable();
			currLinker = cable;
		}
	}

	/**
	 * 设置当前用户选择的连接物
	 * @param linker
	 */
	public void setCurrLinker(ILinker linker) {
		currLinker = linker;
		if (linker != null) {
			this.color = linker.getColor();
		}
		refreshViewPort(currLinker);
	}

	/**
	 * 设置当前用户选择的导线或者气管
	 * @param wireOrPipe
	 */
	public void setCurrLinker(int connectType, ColorRGBA color) {
		this.color = color;

		if (ConnectionUtil.CONNECT_WIRE == connectType) {
			currLinker = new Wire(1, color);
		} else if (ConnectionUtil.CONNECT_PIPE == connectType) {
			currLinker = new Pipe(1, color);
		} else if (ConnectionUtil.CONNECT_CABLE == connectType) {
			currLinker = new Cable();
		} else {
			currLinker = null;
		}
		refreshViewPort(currLinker);
	}

	/**
	 * 选中模型
	 * @param wireOrPipe
	 * @param modify
	 */
	public void selectLinker(ILinker wireOrPipe) {
		// 如果当前在接线过程中，则不可选中其他线
		if (currLinker != null && !(currLinker.getLinkTarget1() == null && currLinker.getLinkTarget2() == null)) {
			return;
		}
		// 取消上一次选中的高亮
		setHighLightLinker(selected, false);
		// 删除上一次的连线
		setLineVisible(false);
		// 隐藏上一次选择的线缆面板
		ui.initXianLanPanel(null, false);
		// 如果当前选中与上一次选中不是同一条导线、气管
		if (!wireOrPipe.equals(selected)) {
			// 高亮选中导线、气管
			setHighLightLinker(wireOrPipe, true);
			// 显示连接线
			showLineBetweenLinker(wireOrPipe);
			selected = wireOrPipe;
		} else {
			selected = null;
		}
	}

	/**
	 * 高亮显示指定连线
	 * @param linker 要高亮处理的连线
	 * @param isHighLight 是否高亮
	 */
	public void setHighLightLinker(ILinker linker, boolean isHighLight) {
		if (linker == null) {
			return;
		}
		for (Spatial linkMdl : linker.getModels().keySet()) {
//			JmeUtil.setMaterialEffect(app, HightLightType.GLOW, linkMdl, ColorConsts.LINKER_SELECTED, isHighLight);
			if (isHighLight) {
				JmeUtil.setSpatialHighLight(linkMdl, ColorConsts.LINKER_SELECTED);
			} else {
				JmeUtil.setSpatialHighLight(linkMdl, ColorRGBA.BlackNoAlpha);
			}
		}
	}

	/**
	 * 发送删除连线的消息给小组其他成员
	 * @param linker 要删除的连线
	 * @param type
	 * @param wireMark
	 */
	private void sendDeleteLinkerMsg(ILinker linker, byte type, String wireMark) {
		int key = -1;
		// 如果当前删除线是存档连线则从集合中移除
		if (onlineAndSaves.containsValue(linker)) {
			for (Entry<Integer, ILinker> set : onlineAndSaves.entrySet()) {
				if (linker.equals(set.getValue())) {
					onlineAndSaves.remove(set);
					key = set.getKey();
					break;
				}
			}
		} else {
			key = linker.hashCode();
		}
		// 联机模式下发送删除导线请求信息
		LinkerMsg req = new LinkerMsg();
		req.setKey(key);
		req.setType(type);
		req.setWireMark(wireMark);
		ClientMgr.send(req);
	}

	/**
	 * 删除选中的导线、气管，联机状态下会发送连线消息
	 */
	public void localDeleteLinker() {
		if (selected == null || !modifiable) {
			log.info("当前未选中接线，或选中接线不可修改！");
			return;
		}
		deleteLinker(selected, true);
		if (alone) {
			return;
		}
		sendDeleteLinkerMsg(selected, LinkerMsg.DELETE_LINKER, null);
	}

	/**
	 * 本地用户删除临时接线，联机状态下会发送连线消息
	 */
	public void localDeleteTmpLinker() {
		byte result = deleteTmpLinker(currLinker, true);
		if (alone) {
			return;
		}
		if (result == LinkerMsg.DELETE_LINKER) {
			sendDeleteLinkerMsg(currLinker, LinkerMsg.DELETE_LINKER, null);
		} else if (result == LinkerMsg.DELETE_SINGLE_TARGET) {
			Cable cable = (Cable) currLinker;
			Wire wire = cable.getPrevWire();
			cable.setNowConnectingWire(wire.getPO().getMark());
			if (wire != null) {
				sendDeleteLinkerMsg(currLinker, LinkerMsg.DELETE_SINGLE_TARGET, wire.getPO().getMark());
			}
		}
	}

	/**
	 * 删除临时接线处理
	 * @param linker 临时接线对象
	 * @param modifiable 可以被当前用户修改的临时接线对象
	 * @return
	 */
	private byte deleteTmpLinker(ILinker linker, boolean modifiable) {
		if (linker == null || linker.isBothBinded() || (linker.getLinkTarget1() == null && linker.getLinkTarget2() == null)) {
			return LinkerMsg.NONE_MSG;
		}
		if (!(linker instanceof Cable) || !((Cable) linker).isSinglePlug()) {
			deleteLinker(linker, modifiable);
			return LinkerMsg.DELETE_LINKER;
		}
		Cable cable = (Cable) linker;
		List<Wire> bindWires = cable.getBindWires();
		if (cable.getBindWires().size() == 0) {
			deleteLinker(linker, modifiable);
			ui.initXianLanPanel(null, false);
			return LinkerMsg.DELETE_LINKER;
		}
		Wire wire = bindWires.get(bindWires.size() - 1);
		cable.getBindWires().remove(wire);
		cable.setPrevWire(wire);
		cable.setNowConnectingWire(wire.getPO().getMark());
		Map<Spatial, ILinkTarget> mdls = wire.getModels();
		for (Spatial linkTerm : mdls.keySet()) {
			JmeUtil.detachModel(linkTerm);
			wire.getModels().remove(linkTerm);
			cable.getModels().remove(linkTerm);
		}
		wire.unbind(wire.getLinkTarget2());
		if (!modifiable) {
			return LinkerMsg.DELETE_SINGLE_TARGET;
		}
		List<Wire> wires = new ArrayList<Wire>(cable.getMark_wire().values());
		for (Wire w : cable.getBindWires()) {
			wires.remove(w);
		}
		if (wires.size() == 0) {
			ui.initXianLanPanel(null, false);
			setLineVisible(false);
		} else {
			ui.initXianLanPanel(wires, true);
			ILinkTarget target = null;
			if (bindWires.size() == 0) {
				target = cable.getLinkTarget1();
			} else {
				Wire currWire = bindWires.get(bindWires.size() - 1);
				target = currWire.getLinkTarget2();
			}
			showLineBetweenLinker(cable);
			Map<Spatial, ILinkTarget> termMdls = cable.getModels();
			for (Entry<Spatial, ILinkTarget> set : termMdls.entrySet()) {
				if (set.getValue().equals(target)) {
					refreshLineBetweenLinker(set.getKey());
					break;
				}
			}
		}
		return LinkerMsg.DELETE_SINGLE_TARGET;
	}

	private void deleteLinker(ILinker linker, boolean modifiable) {
		if (linker == null) {
			return;
		}
		// 去除模型高亮选中效果
		setHighLightLinker(linker, false);
		// 重新整理端子上的接线头
		ILinkTarget term1 = linker.getLinkTarget1();
		ILinkTarget term2 = linker.getLinkTarget2();
		// 删除线缆上的导线绑定模型
		Map<Spatial, ILinkTarget> models = linker.getModels();
		if (linker instanceof Cable && ((Cable) linker).isElecComp()) {
			for (Entry<String, Wire> set : ((Cable) linker).getMark_wire().entrySet()) {
				Map<Spatial, ILinkTarget> mdls = set.getValue().getModels();
				for (Spatial linkTerm : mdls.keySet()) {
					JmeUtil.detachModel(linkTerm);
				}
			}
			if (term2 != null) {
				JmeUtil.detachModel(linker.getLinkMdlByTarget(term2).get(0));
			}
			if (modifiable) {
				// 给模型未接线状态（只有传感器接线才会受到影响）
				for (Spatial linkMdl : models.keySet()) {
					JmeUtil.setSpatialHighLight(linkMdl, ColorConsts.TRANSDUCERS);
				}
			}
		} else {
			for (Spatial linkTerm : models.keySet()) {
				JmeUtil.detachModel(linkTerm);
			}
		}
		// 记录准备存档的信息
		linkerData.removeLinker(linker);
		linker.unbind();
		resetLinks(term1);
		resetLinks(term2);
		setLineVisible(false);

		if (currLinker instanceof Wire) {
			Wire wire = new Wire(1, color);
			currLinker = wire;
		} else if (currLinker instanceof Pipe) {
			Pipe pipe = new Pipe(1, color);
			currLinker = pipe;
		} else if (currLinker instanceof Cable) {
			Cable cable = new Cable();
			currLinker = cable;
		}
	}

	private void resetLinks(ILinkTarget termOrPort) {
		if (termOrPort == null /* || termOrPort.getPO() == null */) {
			return;
		}
		// 获得当前端子/气口的接入轴方向
		List<ILinker> linkers = termOrPort.getLinkers();
		List<Spatial> termMdls = new ArrayList<Spatial>();
		for (ILinker linker : linkers) {
			termMdls.addAll(linker.getLinkMdlByTarget(termOrPort));
			if (termOrPort instanceof Jack) {
				Jack jack = (Jack) termOrPort;
				ConnectionUtil.rotateJack(termMdls, jack.getRotation(), jack.getPO().getJackDirection());
			} else if (termOrPort instanceof Terminal) {
				ConnectionUtil.moveTerms(termMdls, (Terminal) termOrPort);
			} else if (termOrPort instanceof GasPort) {
				ConnectionUtil.rotatePipe(termMdls, (GasPort) termOrPort);
			}
		}
	}

	public void modifyWireNum(String wireNum) {
//		保存导线号码
		List<LinkerMsg> reqMsgs = linkerData.modifyWireNum(modifyLinker, wireNum);
		if (!alone) {
			for (LinkerMsg reqMsg : reqMsgs) {
				reqMsg.setType(LinkerMsg.MARK_LINKER);
				ClientMgr.send(reqMsg);
			}
		}
		modifyWireNum(modifyLinker, wireNum);
	}

	private void modifyWireNum(ILinker linker, String wireNum) {
		if (Util.isEmpty(wireNum)) {
			return;
		}
		if (linker instanceof Cable && ((Cable) linker).getBindWires().size() != 0) {
			List<Wire> bindWires = ((Cable) linker).getBindWires();
			linker = bindWires.get(bindWires.size() - 1);
		}
		for (Spatial model : linker.getModels().keySet()) {
			Spatial parent = ((Node) model).getChild("Label");
			if (parent == null) {
				continue;
			}
			for (Spatial bitmapText : ((Node) parent).getChildren()) {
				if (bitmapText instanceof BitmapText) {
					((BitmapText) bitmapText).setText(wireNum.toUpperCase());
////					有的号码三个英文字母  塞不下  所以过大的 做一下压缩
//					float lineWidth = ((BitmapText) bitmapText).getLineWidth();
//					if (lineWidth > 80) {
//						bitmapText.scale(80f / lineWidth, 1, 1);
//					} else {
					bitmapText.setLocalScale(bitmapText.getLocalScale().y);
//					}
				}
			}
		}
	}

	public void saveGame(boolean toUpload, String userid, String dataName, String serverName) {
		if (currLinker != null && !(currLinker.getLinkTarget1() == null && currLinker.getLinkTarget2() == null)) {
			linkerData.removeLinker(currLinker);
		}

		linkerData.setUserid(userid);
		// 保存到临时文件加
		SaveGame.saveGame(FileUtil.getBaseDir(), tempPath + userid, dataName, linkerData);
		// 保存到本地存档文件夹
		SaveGame.saveGame(FileUtil.getBaseDir(), savePath + userid, dataName, linkerData);

		if (!toUpload) {
			return;
		}

//		String serverName = session.get(PLConsts.FTP_SERVER);
		FTPUtils ftpUtils = FTPUtils.getInstance();
		// 获得存档文件
		File file = new File(savePath + userid + File.separatorChar + dataName);
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		// 上传到FTP上
		boolean uploaded = ftpUtils.uploadFile(serverName, savePath + userid, dataName, is);
		// 如果上传成功删除临时目录下的存档
		if (uploaded) {
			File tempFile = new File(FileUtil.getBaseDir() + tempPath + userid + File.separatorChar + dataName);
			tempFile.delete();
		} else {
			log.warn("上传" + dataName + "文件到FTP失败！");
		}
	}

	public boolean checkStandard(ILinkTarget target, ILinker linker) {
//		List<LinkerMsg> checks = standardData.getLinkerMapForCheck().get(target.getElecCompKey() + target.getTargetKey());
//		if (checks == null || checks.size() == 0) {
//			return false;
//		}
		ILinkTarget anotherTarget = linker.getLinkTarget1();
		if (anotherTarget == null) {
			anotherTarget = linker.getLinkTarget2();
		}
		if (anotherTarget == null) {
			return true;
		}
		List<LinkerMsg> checks = standardData.getLinkerMapForCheck().get(anotherTarget.getElecCompKey() + anotherTarget.getTargetKey());
		if (checks == null) {
			return false;
		}
		for (LinkerMsg check : checks) {
			List<LinkerMsg> checkAnothers = standardData.getLinkerMapForRead().get(check.getKey());
			for (LinkerMsg anothers : checkAnothers) {
				if (anothers.equals(check)) {
					continue;
				}
				if (linker instanceof Cable && ((Cable) linker).isSinglePlug()) {
					Cable cable = (Cable) linker;
					Wire wire = cable.getNowConnectingWire();
					if (anothers.getElecCompKey().equals(target.getElecCompKey()) && anothers.getTargetKey().equals(target.getTargetKey()) && anothers.getWireMark().equals(wire.getPO().getMark())) {
						return true;
					}
				} else if (anothers.getElecCompKey().equals(target.getElecCompKey()) && anothers.getTargetKey().equals(target.getTargetKey())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 读取指定名称的接线存档文件
	 * @return
	 */
	public void loadLinkData(String userid, String dataName, String serverName, boolean local) {
		try {
			if (local) {
				LinkerData linkerData = loadLocalLinkerData(userid, dataName, serverName);
				if (linkerData == null) {
					this.linkerData = new LinkerData();
					return;
				}
				this.linkerData = linkerData;
				connectBySaveData(linkerData, true);
			} else {
				LinkerData linkerData = loadServerLinkerData(userid, dataName, serverName);
				if (linkerData == null) {
					return;
				}
				connectBySaveData(linkerData, false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 加载本地存档
	 * @param userid 用户编号
	 * @param dataName 存档名称
	 * @return 接线存档信息
	 */
	private LinkerData loadLocalLinkerData(String userid, String dataName, String serverName) {
		FTPUtils ftpUtils = FTPUtils.getInstance();
		// 获得FTP服务器IP地址
//		Session session = Session.getSession();
//		String serverName = session.get(PLConsts.FTP_SERVER);
		// 拼接本地文件夹地址
		String localPath = savePath + userid + File.separatorChar;
		// 读取本地文件夹中的文件
		File local = new File(FileUtil.getBaseDir() + localPath + dataName);
		// 检测本地文件夹中是否存在存档
		if (!local.exists()) {
			// 从服务器上下载接线存档
			boolean exists = ftpUtils.checkFile(serverName, localPath, dataName);
			if (exists) {
				// 下载存档到本地
				InputStream in = ftpUtils.downloadFile(serverName, localPath, dataName);
				BufferedInputStream bin = new BufferedInputStream(in);
				try {
					FileUtil.saveFile(bin, FileUtil.getBaseDir() + localPath + dataName);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				log.info("当前第一次 运行联调无存档！");
				return null;
			}
		}
		// 读取本地存档，经过以上判断，此时本地文件夹中必然存在存档
		LinkerData linkerData = (LinkerData) SaveGame.loadGame(FileUtil.getBaseDir(), localPath, dataName);
		if (!userid.equals(linkerData.getUserid())) {
//			FIXME
//			Pool.getNiftyPool().submit(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						OptionUI.showMessageDialog(Dispatcher.getIns().getNifty(), ConnectionUtil.MISMATCHING_OWN);
//					} catch (Exception e) {
//						log.error(e.getMessage(), e);
//					}
//				}
//			});
			// 从服务器上下载接线存档
			boolean exists = ftpUtils.checkFile(serverName, localPath, dataName);
			if (exists) {
				// 下载存档到本地
				InputStream in = ftpUtils.downloadFile(serverName, localPath, dataName);
				BufferedInputStream bin = new BufferedInputStream(in);
				try {
					FileUtil.saveFile(bin, FileUtil.getBaseDir() + localPath + dataName);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				log.info("当前第一次 运行联调无存档！");
				return null;
			}
		}
		return linkerData;
	}

	/**
	 * 从服务器上下载小组成员的接线存档
	 * @param userid 小组成员编号
	 * @param dataName 接线存档名称
	 * @return 接线存档信息
	 */
	private LinkerData loadServerLinkerData(String userid, String dataName, String serverName) {
		FTPUtils ftpUtils = FTPUtils.getInstance();
		// 获得FTP服务器IP地址
//		Session session = Session.getSession();
//		String serverName = session.get(PLConsts.FTP_SERVER);
		// 拼接文件夹地址
		String path = savePath + userid + File.separatorChar;
		// 从服务器上下载接线存档
		boolean exists = ftpUtils.checkFile(serverName, path, dataName);
		if (!exists) {
			log.warn("未在服务器上找到" + path + dataName);
			return null;
		}
		// 下载存档到本地
		InputStream in = ftpUtils.downloadFile(serverName, path, dataName);
		try {
			LinkerData linkerData = (LinkerData) SaveGame.loadGame(in, app.getAssetManager());
			return linkerData;
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 加载接线
	 */
	private void connectBySaveData(LinkerData linkerData, boolean modifiable) {
		if (linkerData == null) {
			log.warn("接线存档信息linkerData为空！");
			return;
		}
		List<LinkerMsg> msgs = linkerData.getLinkerDatas();
		linkerData.getLinkerMapForSave().clear();
		for (LinkerMsg msg : msgs) {
			onlineOrSaveLinker(msg, modifiable);
		}
	}

	/**
	 * 通过联机接线消息接线处理
	 * @param msg
	 */
	public void connectByOnlineMsg(LinkerMsg msg) {
		if (msg == null) {
			log.warn("联机接线消息为空！");
			return;
		}
		onlineOrSaveLinker(msg, false);
	}

	/**
	 * 联机删线
	 */
	public void onlineDeleteLinker(LinkerMsg msg, byte type) {
		int key = msg.getKey();
		ILinker deletelinker = null;
		if (onlineAndSaves.containsKey(key)) {
			deletelinker = onlineAndSaves.get(key);
		}
		if (deletelinker == null) {
			log.warn("未找到元器件" + msg.getElecCompKey() + "上端子" + msg.getTargetKey() + "的接线对象！");
			return;
		}
		if (type == LinkerMsg.DELETE_LINKER) {
			deleteLinker(deletelinker, false);
		} else if (type == LinkerMsg.DELETE_SINGLE_TARGET) {
			deleteTmpLinker(deletelinker, false);
		}
		onlineAndSaves.remove(deletelinker);
	}

	/**
	 * 根据linkerInfo处理接线并记录
	 */
	private void onlineOrSaveLinker(LinkerMsg msg, boolean modifiable) {
		int key = msg.getKey();
		byte linkType = msg.getLinkType();
		ILinker linker = null;
		if (onlineAndSaves.containsKey(key)) {
			linker = onlineAndSaves.get(key);
		} else if (LinkerMsg.WIRE == linkType) {
			linker = new Wire(1, msg.getColor());
			onlineAndSaves.put(key, linker);
		} else if (LinkerMsg.PIPE == linkType) {
			linker = new Pipe(1, msg.getColor());
			onlineAndSaves.put(key, linker);
		} else if (LinkerMsg.CABLE_JACK == linkType || LinkerMsg.CABLE_TERM == linkType) {
			linker = cableMap.get(msg.getCableKey()).clone();
			if (msg.isElecComp()) {
				Cable cable = ((Cable) linker);
				linker = bindSaveTransEvent(msg.getElecCompKey(), cable, modifiable);
				if (linker != null) {
					onlineAndSaves.put(key, linker);
				}
				return;
			}
			onlineAndSaves.put(key, linker);
		}
		connectByLinkerInfo(msg, linker, modifiable);
	}

	public void onlineMark(LinkerMsg msg) {
		int key = msg.getKey();
		ILinker linker = null;
		if (onlineAndSaves.containsKey(key)) {
			linker = onlineAndSaves.get(key);
		}
		if (linker == null) {
			return;
		}
		linker.setWireNum(msg.getWireMark());
		modifyWireNum(linker, msg.getWireMark());
	}

	/**
	 * 验证是否可以接线
	 * @param target 端子、气口
	 */
	public boolean checkConnect(ILinkTarget target) {
		// 获得当前用户选中导线
		if (currLinker == null) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.NO_lINKER);
			return false;
		}
		if (target instanceof Jack && !(currLinker instanceof Cable)) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.JUST_CABLE);
			return false;
		}
		// 如果当前手中是线缆，但当前接口不符合当前线缆制式
		if ((target instanceof Jack) && (currLinker instanceof Cable)) {
			Jack jack = (Jack) target;
			if (jack.getPlug() != null) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.JACK_ALREADY_IN_USED);
				return false;
			}
			Cable cable = (Cable) currLinker;
			// 如果两个连接头都为null，说明用户选择了线缆还没有连接
			if (cable.getPlug1() == null && cable.getPlug2() == null) {
				if (Util.notEmpty(jack.getPO().getBelongElecComp())) {
					return false;
				}
				return true;
			}
			Format format = jack.getFormat();
			Plug plug1 = cable.getPlug1();
			if (plug1 != null) {
				boolean insert = plug1.isInserted();
				if (!insert) {
					return format.equals(plug1.getFormat());
				}
			}
			Plug plug2 = cable.getPlug2();
			if (plug2 != null) {
				boolean insert = plug2.isInserted();
				if (!insert) {
					if (standardData != null && !checkStandard(target, currLinker)) {
						Dispatcher.getIns().getTip().showTip(ITip.ERROR, "接线不符合标准存档！");
						return false;
					}
					return format.equals(plug2.getFormat());
				} else {
					return false;
				}
			} else if (cable.isSinglePlug()) {
				return false;
			}
		} else if ((target instanceof Terminal) && (currLinker instanceof Cable)) {
			Cable cable = (Cable) currLinker;
			Wire wire = cable.getNowConnectingWire();
			if (wire == null) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.SELECT_ANY_WIRE_IN_CABLE);
				return false;
			}
			if (wire.isBothBinded()) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.WIRE_IN_CABLE_LINKED_ALREADY);
				return false;
			}
			Terminal terminal = (Terminal) target;
			if (terminal.getLinkers().size() == terminal.getNum()) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.TERMINAL_LIMITED);
				return false;
			}
			if (standardData != null && !checkStandard(target, currLinker)) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, "接线不符合标准存档！");
				return false;
			}
			return true;
		}
		if (target instanceof Terminal && !(currLinker instanceof Wire)) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.JUST_WIRE);
			return false;
		}
		if (target instanceof Terminal && (currLinker instanceof Wire)) {
			Terminal terminal = (Terminal) target;
			if (terminal.getLinkers().size() == terminal.getNum()) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.TERMINAL_LIMITED);
				return false;
			}
			if (terminal.getLinkers().contains(currLinker)) {
				Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.SAME_LINKER);
				return false;
			}
		}
		if (target instanceof GasPort && !(currLinker instanceof Pipe)) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.JUST_PIPE);
			return false;
		}
		if (target instanceof GasPort && target.getLinkers().size() != 0) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, ConnectionUtil.GASPORT_LIMITED);
			return false;
		}
		if (standardData != null && !checkStandard(target, currLinker)) {
			Dispatcher.getIns().getTip().showTip(ITip.ERROR, "接线不符合标准存档！");
			return false;
		}
		Dispatcher.getIns().getTip().showTip(ITip.RIGHT, null);
		return true;
	}

	/**
	 * 接线处理
	 * @param wireOrPipe 导线、气管
	 * @param termOrPort 端子、气口
	 */
	private Spatial connect(ILinker wireOrPipe, ILinkTarget termOrPort, boolean readSav) {
		// 旋转端子上包括新增接线头在内的若干个接线头
		Spatial model = null;
		ColorRGBA color = null;
		if (termOrPort instanceof Terminal) {
			model = ConnectionUtil.getWireTerm();
			if (wireOrPipe instanceof Cable) {
				Cable cable = (Cable) wireOrPipe;
				Wire wire = cable.getNowConnectingWire();
				model.setUserData("WireMark", wire.getPO().getMark());
				// 添加已经接上端子的导线
				cable.getBindWires().add(wire);
				// 除去已经连接的导线
				if (!readSav) {
					List<Wire> wires = new ArrayList<Wire>(cable.getMark_wire().values());
					for (Wire w : cable.getBindWires()) {
						wires.remove(w);
					}
					ui.initXianLanPanel(wires, true);
				}
				color = wire.getColor();
			} else {
				color = wireOrPipe.getColor();
				currSaveLinker = null;
			}
		} else if (termOrPort instanceof GasPort) {
			model = ConnectionUtil.getPipeTerm();
			color = wireOrPipe.getColor();
			currSaveLinker = null;
		} else if (termOrPort instanceof Jack) {
			Jack jack = (Jack) termOrPort;
			model = ConnectionUtil.getJackTerm(jack.getFormat());
			Cable cable = (Cable) jack.getLinkers().get(0);
			cable = cable.clone();
			if (readSav) {
				cable = (Cable) wireOrPipe;
			}
			color = cable.getColor();
			currSaveLinker = null;
			// 通过jack获得当前的接线线缆
			if (((Cable) wireOrPipe).getPlug1() == null && ((Cable) wireOrPipe).getPlug2() == null) {
				if (readSav) {
					wireOrPipe = cable;
					currSaveLinker = wireOrPipe;
				} else {
					currLinker = cable;
				}
			}
			// 如果是单连接头线缆，则显示导线面板
			if (cable.isSinglePlug() && !readSav) {
				List<Wire> wires = new ArrayList<Wire>(cable.getMark_wire().values());
				ui.initXianLanPanel(wires, true);
				cable.setNowConnectingWire(wires.get(0).getPO().getMark());
			}
		}
		// 添加新的接线头到端子上
		Node target = (Node) termOrPort.getModel();
		final Node parent = target.getParent();
		Spatial screw = target.getChild("screw");
		if (screw != null) {
			model.setLocalTranslation(JmeUtil.getTransLation(screw, parent));
		} else {
			model.setLocalTranslation(target.getLocalTranslation());
		}

		// 修改连接头颜色
		JmeUtil.changeColor(((Node) model).getChild("model"), color);
		model.setUserData("Color", color);

		// 获得端子模型的父节点
		if (JmeUtil.isJMEThread()) {
			((Node) parent).attachChild(model);
		} else {
			final Spatial finalModel = model;
			try {
				app.enqueue(new Callable<Void>() {
					@Override
					public Void call() {
						// 根据当前选中
						((Node) parent).attachChild(finalModel);
						return null;
					}
				}).get();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		// 绑定端子
		if (readSav) {
			if (wireOrPipe instanceof Cable && termOrPort instanceof Terminal) {
				((Cable) wireOrPipe).bindTerm(termOrPort, model);
				wireOrPipe.getModels().put(model, termOrPort);
				Wire wire = ((Cable) wireOrPipe).getNowConnectingWire();
				modifyWireNum(wire, wire.getWireNum());
			} else {
				wireOrPipe.bind(termOrPort);
				wireOrPipe.getModels().put(model, termOrPort);
				if (wireOrPipe.isBothBinded() && wireOrPipe instanceof Wire) {
					modifyWireNum(wireOrPipe, wireOrPipe.getWireNum());
				}
			}
		} else {
			bindWireOrPipe(termOrPort, model);
		}
		resetLinks(termOrPort);
		return model;
	}

	/**
	 * 单机练习模式下接线
	 * @param termOrPort 端子、气口
	 */
	public LinkerMsg offlineConnect(ILinkTarget termOrPort) {
		// 获得当前用户选中导线
		ILinker wireOrPipe = currLinker;
		Spatial model = connect(wireOrPipe, termOrPort, false);
		// 线缆会在连接时对当前连接对象做一些处理，所以此处要重新获得一次
		if (wireOrPipe instanceof Cable && ((Cable) wireOrPipe).getPlug1() == null && ((Cable) wireOrPipe).getPlug2() == null) {
			wireOrPipe = currLinker;
		}
		initLinkerLine(wireOrPipe, model);
		// 接线头添加鼠标监听事件
		addMouseEvent(model, new LinkerEventAdapter(new LinkerEventHandler(wireOrPipe, this, true)));

		return linkerData.addLinker(wireOrPipe, termOrPort, (String) model.getUserData("WireMark"));
	}

	/**
	 * 联机状态下接线
	 * @param linkerInfo 接线消息
	 * @param wireOrPipe
	 * @param modifiable
	 */
	private void connectByLinkerInfo(LinkerMsg linkerInfo, ILinker wireOrPipe, boolean modifiable) {
		ElecComp elecComp = eleCompMap.get(linkerInfo.getElecCompKey());
		if (elecComp == null) {
			log.info("未找到指定元器件的定义信息：" + linkerInfo.getElecCompKey());
			return;
		}
		ElecCompDef def = elecComp.getDef();
		byte linkType = linkerInfo.getLinkType();
		String targetKey = linkerInfo.getTargetKey();
		ILinkTarget termOrPort = null;
		if (LinkerMsg.WIRE == linkType || LinkerMsg.CABLE_TERM == linkType) {
			termOrPort = def.getTerminalMap().get(targetKey);
		} else if (LinkerMsg.PIPE == linkType) {
			termOrPort = def.getGasPortMap().get(targetKey);
		} else if (LinkerMsg.CABLE_JACK == linkType) {
			termOrPort = def.getJackMap().get(targetKey);
		}
		if (wireOrPipe instanceof Cable && termOrPort instanceof Terminal) {
			((Cable) wireOrPipe).setNowConnectingWire(linkerInfo.getWireMark());
			((Cable) wireOrPipe).getNowConnectingWire().setWireNum(linkerInfo.getNumMark());
		}
		wireOrPipe.setWireNum(linkerInfo.getNumMark());
		Spatial model = connect(wireOrPipe, termOrPort, true);
		if (currSaveLinker != null) {
			wireOrPipe = currSaveLinker;
		}
		// 接线头添加鼠标监听事件
		addMouseEvent(model, new LinkerEventAdapter(new LinkerEventHandler(wireOrPipe, this, modifiable)));
		// 记录准备存档的信息
		if (modifiable) {
			linkerData.addLinker(wireOrPipe, termOrPort, (String) model.getUserData("WireMark"));
		}
	}

	/**
	 * 显示或隐藏连接线
	 * @param visible
	 */
	public void setLineVisible(boolean visible) {
		FilterUtil.getLineRenderFilter().setEnabled(visible);
		FilterUtil.getLineRenderFilter().setFollowMouse(true);
	}

	/**
	 * 显示端子两端的连接线
	 * @param wireOrPipe
	 */
	private void showLineBetweenLinker(ILinker wireOrPipe) {
		Map<Spatial, ILinkTarget> models = wireOrPipe.getModels();
		Set<Spatial> modelsList = models.keySet();
//		if (modelsList.size() != 2) {
//			log.debug("选中一端连接头");
//			return;
//		}
		setLineVisible(true);
		FilterUtil.getLineRenderFilter().setColor(wireOrPipe.getColor());
		List<Spatial> modelsL = new ArrayList<Spatial>();
		modelsL.addAll(modelsList);
		Spatial[] modelsArray = new Spatial[modelsList.size() - 1];
		modelsL.subList(1, modelsL.size()).toArray(modelsArray);
		FilterUtil.getLineRenderFilter().showLineWithOutMouse(modelsL.get(0), modelsArray);
		FilterUtil.getLineRenderFilter().setFollowMouse(false);
	}

	private void refreshLineBetweenLinker(Spatial sp) {
		FilterUtil.getLineRenderFilter().setOriSpatial(sp);
		FilterUtil.getLineRenderFilter().setFollowMouse(true);
	}

	public void initLinkerLine(ILinker linker, Spatial model) {
		// 设置连接线的初始位置
		if (FilterUtil.getLineRenderFilter() != null) {
			FilterUtil.getLineRenderFilter().setColor(linker.getColor());
			FilterUtil.getLineRenderFilter().setOriSpatial(model);
		}
	}

	private Cable bindSaveTransEvent(String elecCompKey, Cable cable, boolean modifiable) {
		ElecComp elecComp = eleCompMap.get(elecCompKey);
		if (elecComp == null) {
			System.err.println("找不到elecCompKey : " + elecCompKey);
			return null;
//			throw new RuntimeException("找不到elecCompKey : " + elecCompKey);
		}
		ElecCompDef def = elecComp.getDef();
		def.buildCompLogic();
		Node elecCompMdl = (Node) parent.getChild(elecComp.getPO().getMdlName());
		JmeUtil.setSpatialHighLight(elecCompMdl, ColorRGBA.BlackNoAlpha);
//		MouseEventState.cleanEvent(elecCompMdl);
		elecCompMdl.setUserData("MouseEvent", null);

		Jack jack = def.getJackMap().values().iterator().next();

		TransducersHandler handler = new TransducersHandler(this, elecComp, jack, modifiable);

		cable.bind(jack);
		cable.setElecComp(true);
		cable.getModels().put(elecCompMdl, jack);
		if (modifiable) {
			linkerData.addLinker(cable, jack, null);
		}

		handler.setCable(cable);
		addMouseEvent(elecCompMdl, new TransducersAdapter(handler));
		return cable;
	}

	/**
	 * @param sp
	 * @param listener
	 */
	private void addMouseEvent(Spatial sp, IMouseEventListener listener) {
		if (eventState == null || sp == null || listener == null) {
			return;
		}
		eventCandidates.add(sp);
		eventState.addCandidate(sp, listener);
	}

	/**
	 * 获得当前用户选择的导线或气管
	 * @return
	 */
	public ILinker getCurrLinker() {
		return currLinker;
	}

	public LinkerData getLinkerData() {
		return linkerData;
	}

	public ILinker getSelected() {
		return selected;
	}

	/**
	 * 设置当前选中接线是否可修改
	 * @param modifiable
	 */
	public void setCurrSelectedModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}

	public void setModifyLinker(ILinker modifyLinker) {
		this.modifyLinker = modifyLinker;
	}

	public boolean isAlone() {
		return alone;
	}

	public void setUI(ConnectionController ui) {
		this.ui = ui;
	}

	public ConnectionController getUI() {
		return ui;
	}

	public void setCableMap(Map<Object, Cable> cableMap) {
		this.cableMap = cableMap;
	}

	public void setEleCompMap(Map<Object, ElecComp> eleCompMap) {
		this.eleCompMap = eleCompMap;
	}

	public void clear() {
		setLineVisible(false);

		onlineAndSaves.clear();

		if (linkerData != null) {
			linkerData.clear();
		}
	}

	public void destroy() {
		clear();
		FilterUtil.removeFilter(FilterUtil.LINERENDER);

		for (Spatial spatial : eventCandidates) {
			eventState.removeCandidate(spatial);
		}
		eventCandidates.clear();

		viewRoot.detachAllChildren();
		port.clearScenes();
		app.getRenderManager().removePostView(port);
	}
}
