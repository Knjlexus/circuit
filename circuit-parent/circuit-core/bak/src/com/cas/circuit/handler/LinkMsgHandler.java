package com.cas.circuit.handler;

import java.util.Collection;

import com.cas.circuit.ConnectionHandler;
import com.cas.circuit.IConnectScreenControl;
import com.cas.circuit.msg.LinkerMsg;
import com.cas.network.handler.BaseHandler;
import com.cas.robot.common.Dispatcher;
import com.cas.util.Util;
import com.jme3.network.Client;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.ScreenController;

/**
 * 连线消息处理类
 * @功能 LinkMsgHandler.java
 * @作者 CWJ
 * @创建日期 2016年5月24日
 * @修改人 CWJ
 */
public class LinkMsgHandler extends BaseHandler {

	/**
	 * 个人用户信息
	 */
	public static final String PERSONAL_ID = "personal_id";
	/**
	 * 小组组长编号
	 */
	public static final String GROUP_LEADER_ID = "group_leader_id";
	
	@Override
	public void execute(HostedConnection source, Message m) {
		LinkerMsg reqMsg = (LinkerMsg) m;
		String myLeader = source.getAttribute(GROUP_LEADER_ID);
		if (Util.isEmpty(myLeader)) {
			throw new RuntimeException("在线连线状态下参数：GROUP_LEADER_ID不可能为空！");
		}
		String userid = source.getAttribute(PERSONAL_ID);
		Collection<HostedConnection> conns = source.getServer().getConnections();
		String leaderid = null;
		String loginid = null;
		for (HostedConnection conn : conns) {
			leaderid = conn.getAttribute(GROUP_LEADER_ID);
			loginid = conn.getAttribute(PERSONAL_ID);
			if (!myLeader.equals(leaderid) || userid.equals(loginid)) {
				continue;
			}
			LinkerMsg respMsg = reqMsg;
			conn.send(respMsg);
		}
	}

	@Override
	public void execute(Client client, Message m) {
		LinkerMsg respMsg = (LinkerMsg) m;
		Nifty nifty = Dispatcher.getIns().getNifty();
		ScreenController screenCtrl = nifty.getCurrentScreen().getScreenController();
		if (screenCtrl instanceof IConnectScreenControl) {
			ConnectionHandler handler = ((IConnectScreenControl) screenCtrl).getConnectionHandler();
			byte type = respMsg.getType();
			if (LinkerMsg.ADD_LINKER == type) {
				handler.connectByOnlineMsg(respMsg);
			} else if (LinkerMsg.DELETE_LINKER == type || LinkerMsg.DELETE_SINGLE_TARGET == type) {
				handler.onlineDeleteLinker(respMsg, type);
			} else if (LinkerMsg.MARK_LINKER == type) {
				handler.onlineMark(respMsg);
			}
		}
	}

}
